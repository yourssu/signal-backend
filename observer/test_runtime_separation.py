import os
import ast
import unittest


class RuntimeSeparationTest(unittest.TestCase):
    def test_admin_commands_run_independently_from_log_observer(self):
        dockerfile = os.path.join(os.path.dirname(__file__), "..", "app", "Dockerfile")
        with open(dockerfile, encoding="utf-8") as file:
            content = file.read()

        self.assertIn("python /app/script/observer.py &", content)
        self.assertIn("python /app/script/admin.py &", content)
        self.assertNotIn("admin.py |", content)
        self.assertIn("wait -n $SPRING_PID $OBSERVER_PID $ADMIN_PID", content)
        self.assertIn("umask 027", content)

        deploy_script = os.path.join(os.path.dirname(__file__), "script", "docker-deploy.sh")
        with open(deploy_script, encoding="utf-8") as file:
            deploy_content = file.read()
        self.assertIn("-p 3005:3005", deploy_content)
        self.assertIn("-v $(pwd)/logs:/app/logs", deploy_content)

        required_admin_environment = (
            "SLACK_SIGNING_SECRET",
            "SLACK_CHANNEL_PROD",
            "SLACK_CHANNEL_DEV",
            "SLACK_CHANNEL_ADMIN",
            "API_HOST_PROD",
            "API_HOST_DEV",
            "SECRET_KEY_PROD",
            "SECRET_KEY_DEV",
        )
        workflows = ("dev.yml", "prod.yml", "deploy-only.yml")
        workflow_directory = os.path.join(os.path.dirname(__file__), "..", ".github", "workflows")
        for workflow in workflows:
            with open(os.path.join(workflow_directory, workflow), encoding="utf-8") as file:
                workflow_content = file.read()
            for variable in required_admin_environment:
                self.assertIn(f'echo "{variable}=${variable}" >> .env', workflow_content)

    def test_admin_commands_keep_their_slash_command_contract_without_log_dependencies(self):
        admin_path = os.path.join(os.path.dirname(__file__), "script", "admin.py")
        with open(admin_path, encoding="utf-8") as file:
            tree = ast.parse(file.read())

        imports = {
            alias.name
            for node in ast.walk(tree)
            if isinstance(node, (ast.Import, ast.ImportFrom))
            for alias in node.names
        }
        commands = {
            decorator.args[0].value
            for node in ast.walk(tree)
            if isinstance(node, ast.FunctionDef)
            for decorator in node.decorator_list
            if isinstance(decorator, ast.Call)
            and isinstance(decorator.func, ast.Attribute)
            and decorator.func.attr == "command"
        }

        self.assertEqual(commands, {"/t", "/add", "/delete"})
        self.assertTrue({"observer", "log_router", "signal_handler"}.isdisjoint(imports))


if __name__ == "__main__":
    unittest.main()
