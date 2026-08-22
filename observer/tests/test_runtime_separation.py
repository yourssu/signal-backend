import os
import unittest


ROOT = os.path.join(os.path.dirname(__file__), "..")
class RuntimeSeparationTest(unittest.TestCase):
    def test_docker_runs_spring_observer_and_admin_per_environment(self):
        with open(os.path.join(ROOT, "..", "app", "Dockerfile"), encoding="utf-8") as file:
            dockerfile = file.read()
        with open(os.path.join(ROOT, "script", "docker-deploy.sh"), encoding="utf-8") as file:
            deploy_script = file.read()
        with open(os.path.join(ROOT, "script", "requirements.txt"), encoding="utf-8") as file:
            requirements = file.read()

        self.assertIn('observer) exec /app/venv/bin/python /app/script/observer.py', dockerfile)
        self.assertIn('admin) exec /app/venv/bin/python /app/script/admin.py', dockerfile)
        self.assertIn('-e COMPONENT=spring', deploy_script)
        self.assertIn('-e COMPONENT=observer', deploy_script)
        self.assertIn('-e COMPONENT=admin', deploy_script)
        self.assertTrue(deploy_script.startswith("#!/bin/bash\nset -euo pipefail\n"))
        self.assertNotIn('if [ "$ENVIRONMENT" = "prod" ]', deploy_script)
        self.assertIn('docker network create "$NETWORK_NAME"', deploy_script)
        self.assertGreaterEqual(deploy_script.count('--network "$NETWORK_NAME"'), 3)
        self.assertIn('Restart=always', deploy_script)
        self.assertIn('systemctl enable "$SUPERVISOR_SERVICE"', deploy_script)
        self.assertIn('--health-retries 3', deploy_script)
        self.assertNotIn('wait -n $SPRING_PID $OBSERVER_PID', dockerfile)
        self.assertIn("EXPOSE 3005", dockerfile)
        self.assertIn("-p 127.0.0.1:3005:3005", deploy_script)
        self.assertIn("slack-bolt", requirements)
        self.assertIn('-v "$(pwd)/logs:/app/logs"', deploy_script)

    def test_deployment_keeps_existing_shared_channel_contract(self):
        paths = [
            os.path.join(ROOT, ".env.example"),
            *[
                os.path.join(ROOT, "..", ".github", "workflows", workflow)
                for workflow in ("dev.yml", "prod.yml", "deploy-only.yml")
            ],
        ]

        for path in paths:
            with self.subTest(path=path), open(path, encoding="utf-8") as file:
                content = file.read()
                for variable in ("SLACK_TOKEN", "SLACK_SIGNING_SECRET", "SLACK_CHANNEL", "SLACK_ADMIN_CHANNEL", "SLACK_LOG_CHANNEL"):
                    self.assertIn(variable, content)

        for workflow in ("prod.yml", "deploy-only.yml"):
            with open(os.path.join(ROOT, "..", ".github", "workflows", workflow), encoding="utf-8") as file:
                content = file.read()
                for variable in ("SLACK_CHANNEL_DEV", "API_HOST_DEV", "SECRET_KEY_DEV"):
                    self.assertNotIn(variable, content)


if __name__ == "__main__":
    unittest.main()
