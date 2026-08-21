import os
import stat
import subprocess
import tempfile
import unittest


SCRIPT = os.path.join(os.path.dirname(__file__), "script", "supervise.sh")


class SupervisorScenarioTest(unittest.TestCase):
    def test_restart_once_then_requires_manual_action(self):
        with tempfile.TemporaryDirectory() as directory:
            bin_dir = os.path.join(directory, "bin")
            os.makedirs(bin_dir)
            env_file = os.path.join(directory, ".env")
            with open(env_file, "w", encoding="utf-8") as file:
                file.write("PROJECT_NAME=test\nENVIRONMENT=prod\nSLACK_TOKEN=test-token\nSLACK_LOG_CHANNEL=test-channel\n")
            self._executable(bin_dir, "docker", '#!/bin/bash\nif [ "$1" = inspect ]; then echo unhealthy; else echo "$@" >> "$RESTART_LOG"; fi\n')
            self._executable(bin_dir, "curl", '#!/bin/bash\necho \'{"ok":true}\'\n')
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
            self.assertTrue(os.path.exists(os.path.join(directory, "logs", "state", "spring-manual-alerted")))
            self.assertTrue(os.path.exists(os.path.join(directory, "logs", "state", "observer-manual-alerted")))
            self.assertTrue(os.path.exists(os.path.join(directory, "logs", "state", "admin-manual-alerted")))

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
                '#!/bin/bash\ncount=$(cat "$CURL_COUNT" 2>/dev/null || echo 0)\ncount=$((count + 1))\necho "$count" > "$CURL_COUNT"\nif [ "$count" -le 3 ]; then echo \'{"ok":true}\'; elif [ "$count" -le 6 ]; then exit 1; else echo \'{"ok":true}\'; fi\n',
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
            marker = os.path.join(directory, "logs", "state", "spring-manual-alerted")
            self.assertFalse(os.path.exists(marker))

            subprocess.run(["bash", SCRIPT], cwd=directory, env=environment, check=True)
            self.assertTrue(os.path.exists(marker))

    @staticmethod
    def _executable(directory, name, content):
        path = os.path.join(directory, name)
        with open(path, "w", encoding="utf-8") as file:
            file.write(content)
        os.chmod(path, os.stat(path).st_mode | stat.S_IXUSR)


if __name__ == "__main__":
    unittest.main()
