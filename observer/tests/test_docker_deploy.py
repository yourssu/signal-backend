import os
import stat
import subprocess
import tempfile
import unittest


SCRIPT = os.path.join(os.path.dirname(__file__), "..", "script", "docker-deploy.sh")


class DockerDeployTest(unittest.TestCase):
    def test_new_stack_failure_restores_previous_image_once(self):
        with tempfile.TemporaryDirectory() as directory:
            bin_directory = os.path.join(directory, "bin")
            os.makedirs(bin_directory)
            with open(os.path.join(directory, ".env"), "w", encoding="utf-8") as file:
                file.write(
                    "PROJECT_NAME=signal-test\n"
                    "ECR_REGISTRY=public.ecr.aws/test\n"
                    "SERVER_PORT=9012\n"
                    "ENVIRONMENT=dev\n"
                    "SLACK_TOKEN=test-token\n"
                    "SLACK_ADMIN_CHANNEL=admin-channel\n"
                    "SLACK_LOG_CHANNEL=test-channel\n"
                    "DB_URL=jdbc:test\n"
                    "DB_USERNAME=test\n"
                    "DB_PASSWORD=test\n"
                    "ADMIN_ACCESS_KEY=test\n"
                    "CONTACT_SECRET_KEY=test\n"
                    "JWT_SECRET=test\n"
                    "OPENAI_URL=https://example.com\n"
                    "OPENAI_API_KEY=test\n"
                    "OPENAI_MODEL=test\n"
                )
            self._executable(bin_directory, "aws", "#!/bin/bash\necho token\n")
            self._executable(bin_directory, "flock", "#!/bin/bash\nexit 0\n")
            self._executable(bin_directory, "sudo", "#!/bin/bash\nexit 0\n")
            self._executable(
                bin_directory,
                "curl",
                "#!/bin/bash\nprintf '%s\\n' \"$*\" >> \"$CURL_LOG\"\necho '{\"ok\":true}'\n",
            )
            self._executable(bin_directory, "logger", "#!/bin/bash\nexit 0\n")
            self._executable(
                bin_directory,
                "docker",
                "#!/bin/bash\n"
                "echo \"$*\" >> \"$DOCKER_LOG\"\n"
                "if [ \"$1\" = login ]; then cat >/dev/null; exit 0; fi\n"
                "if [ \"$1 $2\" = 'image inspect' ]; then echo sha256:new; exit 0; fi\n"
                "if [ \"$1\" = inspect ] && echo \"$*\" | grep -q '{{.Image}}'; then echo sha256:old; exit 0; fi\n"
                "if [ \"$1\" = inspect ]; then echo healthy; exit 0; fi\n"
                "if [ \"$1\" = run ] && echo \"$*\" | grep -q sha256:new; then exit 42; fi\n"
                "exit 0\n",
            )
            docker_log = os.path.join(directory, "docker.log")
            curl_log = os.path.join(directory, "curl.log")
            environment = {
                **os.environ,
                "PATH": bin_directory + os.pathsep + os.environ["PATH"],
                "DOCKER_LOG": docker_log,
                "CURL_LOG": curl_log,
                "DEPLOY_HEALTH_TIMEOUT": "0",
            }

            result = subprocess.run(
                ["bash", SCRIPT],
                cwd=directory,
                env=environment,
                capture_output=True,
                text=True,
            )

            self.assertEqual(result.returncode, 1)
            self.assertNotIn("Deployment completed successfully!", result.stdout)
            with open(docker_log, encoding="utf-8") as file:
                docker_calls = file.read()
            self.assertIn("sha256:new", docker_calls)
            self.assertIn("sha256:old", docker_calls)
            self.assertIn("previous image restored", result.stderr)
            with open(curl_log, encoding="utf-8") as file:
                slack_payload = file.read()
            self.assertNotIn(r"\\n", slack_payload)
            self.assertIn(r"\n```", slack_payload)

    @staticmethod
    def _executable(directory, name, content):
        path = os.path.join(directory, name)
        with open(path, "w", encoding="utf-8") as file:
            file.write(content)
        os.chmod(path, os.stat(path).st_mode | stat.S_IXUSR)


if __name__ == "__main__":
    unittest.main()
