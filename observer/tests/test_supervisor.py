import os
import stat
import subprocess
import tempfile
import unittest


SCRIPT = os.path.join(os.path.dirname(__file__), "..", "script", "supervise.sh")


class SupervisorScenarioTest(unittest.TestCase):
    def test_memory_alert_requires_pressure_and_recovers_after_consecutive_normal_checks(self):
        with tempfile.TemporaryDirectory() as directory:
            bin_dir = os.path.join(directory, "bin")
            os.makedirs(bin_dir)
            env_file = os.path.join(directory, ".env")
            meminfo = os.path.join(directory, "meminfo")
            pressure = os.path.join(directory, "memory-pressure")
            vmstat = os.path.join(directory, "vmstat")
            slack_log = os.path.join(directory, "slack-payloads")
            with open(env_file, "w", encoding="utf-8") as file:
                file.write("PROJECT_NAME=test\nENVIRONMENT=dev\nSLACK_TOKEN=test-token\nSLACK_LOG_CHANNEL=test-channel\n")
            with open(vmstat, "w", encoding="utf-8") as file:
                file.write("oom_kill 0\n")
            self._executable(
                bin_dir,
                "docker",
                '#!/bin/bash\nif [ "$1" = stats ]; then printf "20.00%%\\ttest-spring\\t350MiB / 2GiB\\n"; '
                'elif [ "$1" = inspect ]; then echo healthy; fi\n',
            )
            self._executable(
                bin_dir,
                "curl",
                '#!/bin/bash\nwhile [ "$#" -gt 0 ]; do\n  if [ "$1" = --data ]; then\n    shift\n'
                '    python3 -c \'import json,sys; print(json.loads(sys.argv[1])["text"])\' "$1" >> "$SLACK_PAYLOAD_LOG"\n'
                '    break\n  fi\n  shift\ndone\necho \'{"ok":true,"ts":"123.456"}\'\n',
            )
            self._executable(bin_dir, "logger", "#!/bin/bash\nexit 0\n")
            environment = {
                **os.environ,
                "PATH": bin_dir + os.pathsep + os.environ["PATH"],
                "SUPERVISOR_ENV_FILE": env_file,
                "SUPERVISOR_ONCE": "1",
                "MEMINFO_FILE": meminfo,
                "MEMORY_PRESSURE_FILE": pressure,
                "VMSTAT_FILE": vmstat,
                "MEMORY_ALERT_INTERVAL_COUNT": "2",
                "MEMORY_RECOVERY_INTERVAL_COUNT": "2",
                "SLACK_PAYLOAD_LOG": slack_log,
            }

            self._write_memory_state(meminfo, pressure, 90_000, "1.25")
            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)
            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)

            with open(slack_log, encoding="utf-8") as file:
                alerts = file.read()
            self.assertIn("EC2 메모리 압력 지속", alerts)
            self.assertIn("PSI full", alerts)
            self.assertNotIn("메모리 압력 해소", alerts)

            self._write_memory_state(meminfo, pressure, 200_000, "0.00")
            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)
            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)

            with open(slack_log, encoding="utf-8") as file:
                alerts = file.read()
            self.assertEqual(alerts.count("EC2 메모리 압력 지속"), 1)
            self.assertEqual(alerts.count("EC2 메모리 압력 해소"), 1)

    def test_restart_once_then_requires_manual_action(self):
        with tempfile.TemporaryDirectory() as directory:
            bin_dir = os.path.join(directory, "bin")
            os.makedirs(bin_dir)
            env_file = os.path.join(directory, ".env")
            with open(env_file, "w", encoding="utf-8") as file:
                file.write("PROJECT_NAME=test\nENVIRONMENT=prod\nSLACK_TOKEN=test-token\nSLACK_LOG_CHANNEL=test-channel\n")
            self._executable(bin_dir, "docker", '#!/bin/bash\nif [ "$1" = inspect ]; then echo unhealthy; else echo "$@" >> "$RESTART_LOG"; fi\n')
            self._executable(bin_dir, "curl", '#!/bin/bash\necho \'{"ok":true,"ts":"123.456"}\'\n')
            self._executable(bin_dir, "logger", "#!/bin/bash\nexit 0\n")
            restart_log = os.path.join(directory, "restarts")
            environment = {
                **os.environ,
                "PATH": bin_dir + os.pathsep + os.environ["PATH"],
                "SUPERVISOR_ENV_FILE": env_file,
                "SUPERVISOR_ONCE": "1",
                "RESTART_LOG": restart_log,
            }

            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)
            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)

            with open(restart_log, encoding="utf-8") as file:
                restarts = file.read().splitlines()
            self.assertEqual(restarts, ["restart test-spring", "restart test-observer", "restart test-admin"])
            self.assertTrue(os.path.exists(os.path.join(directory, "logs", "state", "runtime-manual-alerted")))

    def test_manual_alert_marker_is_created_only_after_slack_delivery_succeeds(self):
        with tempfile.TemporaryDirectory() as directory:
            bin_dir = os.path.join(directory, "bin")
            os.makedirs(bin_dir)
            env_file = os.path.join(directory, ".env")
            with open(env_file, "w", encoding="utf-8") as file:
                file.write("PROJECT_NAME=test\nENVIRONMENT=dev\nSLACK_TOKEN=test-token\nSLACK_LOG_CHANNEL=test-channel\n")
            self._executable(bin_dir, "docker", '#!/bin/bash\nif [ "$1" = inspect ]; then echo unhealthy; else exit 0; fi\n')
            self._executable(
                bin_dir,
                "curl",
                '#!/bin/bash\ncount=$(cat "$CURL_COUNT" 2>/dev/null || echo 0)\ncount=$((count + 1))\necho "$count" > "$CURL_COUNT"\nif [ "$count" -eq 1 ] || [ "$count" -ge 3 ]; then echo \'{"ok":true,"ts":"123.456"}\'; else exit 1; fi\n',
            )
            self._executable(bin_dir, "logger", "#!/bin/bash\nexit 0\n")
            environment = {
                **os.environ,
                "PATH": bin_dir + os.pathsep + os.environ["PATH"],
                "SUPERVISOR_ENV_FILE": env_file,
                "SUPERVISOR_ONCE": "1",
                "CURL_COUNT": os.path.join(directory, "curl-count"),
            }

            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)
            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)
            marker = os.path.join(directory, "logs", "state", "runtime-manual-alerted")
            self.assertFalse(os.path.exists(marker))

            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)
            self.assertTrue(os.path.exists(marker))

    @staticmethod
    def _executable(directory, name, content):
        path = os.path.join(directory, name)
        with open(path, "w", encoding="utf-8") as file:
            file.write(content)
        os.chmod(path, os.stat(path).st_mode | stat.S_IXUSR)

    @staticmethod
    def _write_memory_state(meminfo, pressure, available_kb, psi_full_avg60):
        with open(meminfo, "w", encoding="utf-8") as file:
            file.write(f"MemAvailable: {available_kb} kB\n")
        with open(pressure, "w", encoding="utf-8") as file:
            file.write(f"some avg10=0.00 avg60=0.00 avg300=0.00 total=0\nfull avg10=0.00 avg60={psi_full_avg60} avg300=0.00 total=0\n")


if __name__ == "__main__":
    unittest.main()
