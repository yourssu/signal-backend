import json
import os
import sys
import tempfile
import unittest
from unittest.mock import patch

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "..", "script")
sys.path.insert(0, SCRIPT_DIR)

from healthcheck import observer_healthy


class ObserverHealthcheckTest(unittest.TestCase):
    def test_default_age_allows_one_slack_request_timeout_but_not_stale_process(self):
        with tempfile.TemporaryDirectory() as directory:
            path = os.path.join(directory, "observer-health.json")

            with open(path, "w", encoding="utf-8") as file:
                json.dump({"checked_at": 81}, file)
            with patch("healthcheck.time.time", return_value=100):
                self.assertTrue(observer_healthy(path))

            with open(path, "w", encoding="utf-8") as file:
                json.dump({"checked_at": 79}, file)
            with patch("healthcheck.time.time", return_value=100):
                self.assertFalse(observer_healthy(path))


if __name__ == "__main__":
    unittest.main()
