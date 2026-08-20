import os
import unittest


ROOT = os.path.dirname(__file__)
ADMIN_ONLY_ENVIRONMENT = (
    "SLACK_CHANNEL_PROD",
    "SLACK_CHANNEL_DEV",
    "SLACK_CHANNEL_ADMIN",
    "API_HOST_PROD",
    "API_HOST_DEV",
    "SECRET_KEY_PROD",
    "SECRET_KEY_DEV",
)


class RuntimeSeparationTest(unittest.TestCase):
    def test_docker_runs_spring_observer_and_admin_independently(self):
        with open(os.path.join(ROOT, "..", "app", "Dockerfile"), encoding="utf-8") as file:
            dockerfile = file.read()
        with open(os.path.join(ROOT, "script", "docker-deploy.sh"), encoding="utf-8") as file:
            deploy_script = file.read()
        with open(os.path.join(ROOT, "script", "requirements.txt"), encoding="utf-8") as file:
            requirements = file.read()

        self.assertIn("observer) exec /app/venv/bin/python /app/script/observer.py", dockerfile)
        self.assertIn("admin) exec /app/venv/bin/python /app/script/admin.py", dockerfile)
        self.assertNotIn("wait -n", dockerfile)
        self.assertIn("EXPOSE 3005", dockerfile)
        self.assertIn("-p 127.0.0.1:3005:3005", deploy_script)
        for component in ("spring", "observer", "admin"):
            self.assertIn(f'${{PROJECT_NAME}}-{component}', deploy_script)
            self.assertIn(f"-e COMPONENT={component}", deploy_script)
        self.assertIn("slack-bolt", requirements)
        self.assertIn('-v "$(pwd)/logs:/app/logs"', deploy_script)

    def test_deployment_keeps_only_existing_slack_channel_contract(self):
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
                for variable in ADMIN_ONLY_ENVIRONMENT:
                    self.assertNotIn(variable, content)
                for variable in ("SLACK_TOKEN", "SLACK_SIGNING_SECRET", "SLACK_CHANNEL", "SLACK_ADMIN_CHANNEL", "SLACK_LOG_CHANNEL"):
                    self.assertIn(variable, content)


if __name__ == "__main__":
    unittest.main()
