import os
import stat
import subprocess
import tempfile
import unittest


SCRIPT = os.path.join(os.path.dirname(__file__), "..", "script", "docker-deploy.sh")


class DockerDeployTest(unittest.TestCase):
    def test_container_start_failure_stops_deployment_without_success_message(self):
        with tempfile.TemporaryDirectory() as directory:
            bin_directory = os.path.join(directory, "bin")
            os.makedirs(bin_directory)
            with open(os.path.join(directory, ".env"), "w", encoding="utf-8") as file:
                file.write(
                    "PROJECT_NAME=signal-test\n"
                    "ECR_REGISTRY=public.ecr.aws/test\n"
                    "SERVER_PORT=9012\n"
                    "ENVIRONMENT=dev\n"
                )
            self._executable(bin_directory, "aws", "#!/bin/bash\necho token\n")
            self._executable(
                bin_directory,
                "docker",
                "#!/bin/bash\n"
                "case \"$1\" in\n"
                "  login) cat >/dev/null; exit 0 ;;\n"
                "  pull) exit 0 ;;\n"
                "  ps) exit 0 ;;\n"
                "  images) echo 'REPOSITORY TAG IMAGE ID CREATED AT'; exit 0 ;;\n"
                "  network) exit 0 ;;\n"
                "  run) exit 42 ;;\n"
                "  *) exit 0 ;;\n"
                "esac\n",
            )
            environment = {**os.environ, "PATH": bin_directory + os.pathsep + os.environ["PATH"]}

            result = subprocess.run(
                ["bash", SCRIPT],
                cwd=directory,
                env=environment,
                capture_output=True,
                text=True,
            )

            self.assertEqual(result.returncode, 42)
            self.assertNotIn("Deployment completed successfully!", result.stdout)

    @staticmethod
    def _executable(directory, name, content):
        path = os.path.join(directory, name)
        with open(path, "w", encoding="utf-8") as file:
            file.write(content)
        os.chmod(path, os.stat(path).st_mode | stat.S_IXUSR)


if __name__ == "__main__":
    unittest.main()
