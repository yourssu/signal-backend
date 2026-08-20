import os
import unittest


ROOT = os.path.dirname(__file__)
ADMIN_ONLY_ENVIRONMENT = (
    "SLACK_SIGNING_SECRET",
    "SLACK_CHANNEL_PROD",
    "SLACK_CHANNEL_DEV",
    "SLACK_CHANNEL_ADMIN",
    "API_HOST_PROD",
    "API_HOST_DEV",
    "SECRET_KEY_PROD",
    "SECRET_KEY_DEV",
)


class RuntimeSeparationTest(unittest.TestCase):
    def test_docker_runs_only_spring_and_log_observer(self):
        with open(os.path.join(ROOT, "..", "app", "Dockerfile"), encoding="utf-8") as file:
            dockerfile = file.read()
        with open(os.path.join(ROOT, "script", "docker-deploy.sh"), encoding="utf-8") as file:
            deploy_script = file.read()
        with open(os.path.join(ROOT, "script", "requirements.txt"), encoding="utf-8") as file:
            requirements = file.read()

        self.assertIn("python /app/script/observer.py &", dockerfile)
        self.assertIn("wait -n $SPRING_PID $OBSERVER_PID", dockerfile)
        self.assertNotIn("admin.py", dockerfile)
        self.assertNotIn("ADMIN_PID", dockerfile)
        self.assertNotIn("EXPOSE 3005", dockerfile)
        self.assertNotIn("-p 3005:3005", deploy_script)
        self.assertNotIn("slack-bolt", requirements)
        self.assertIn("-v $(pwd)/logs:/app/logs", deploy_script)

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
                for variable in ("SLACK_TOKEN", "SLACK_CHANNEL", "SLACK_ADMIN_CHANNEL", "SLACK_LOG_CHANNEL"):
                    self.assertIn(variable, content)


if __name__ == "__main__":
    unittest.main()
