import json
import os
import sys
import tempfile
import unittest
from unittest.mock import Mock

SCRIPT_DIR = os.path.join(os.path.dirname(__file__), "script")
sys.path.insert(0, SCRIPT_DIR)

from durable_queue import DurableSlackQueue
from log_cursor import DurableLogCursor
from restart_budget import RestartBudget


class ReliabilityTest(unittest.TestCase):
    def test_offset_survives_restart_and_rotation(self):
        with tempfile.TemporaryDirectory() as directory:
            path = os.path.join(directory, "events.log")
            state = os.path.join(directory, "offset.json")
            with open(path, "w", encoding="utf-8") as file:
                file.write("one\ntwo\n")

            seen = []
            cursor = DurableLogCursor(path, state)
            cursor.poll(seen.append)
            cursor.close()
            self.assertEqual(seen, ["one\n", "two\n"])

            os.rename(path, path + ".1")
            with open(path + ".1", "a", encoding="utf-8") as file:
                file.write("three\n")
            with open(path, "w", encoding="utf-8") as file:
                file.write("four\n")

            cursor = DurableLogCursor(path, state)
            cursor.poll(seen.append)
            cursor.close()
            self.assertEqual(seen, ["one\n", "two\n", "three\n", "four\n"])

    def test_truncate_restarts_at_zero(self):
        with tempfile.TemporaryDirectory() as directory:
            path = os.path.join(directory, "app.log")
            state = os.path.join(directory, "offset.json")
            with open(path, "w", encoding="utf-8") as file:
                file.write("before\n")
            cursor = DurableLogCursor(path, state)
            cursor.poll(lambda _: None)
            cursor.close()
            with open(path, "w", encoding="utf-8") as file:
                file.write("after\n")
            seen = []
            cursor = DurableLogCursor(path, state)
            cursor.poll(seen.append)
            cursor.close()
            self.assertEqual(seen, ["after\n"])

    def test_first_production_start_does_not_replay_historical_lines(self):
        with tempfile.TemporaryDirectory() as directory:
            path = os.path.join(directory, "app.log")
            state = os.path.join(directory, "offset.json")
            with open(path, "w", encoding="utf-8") as file:
                file.write("historical\n")
            cursor = DurableLogCursor(path, state, start_at_end_if_missing=True)
            seen = []
            cursor.poll(seen.append)
            with open(path, "a", encoding="utf-8") as file:
                file.write("new\n")
            cursor.poll(seen.append)
            self.assertEqual(seen, ["new\n"])

    def test_failed_slack_is_durable_and_replayed(self):
        with tempfile.TemporaryDirectory() as directory:
            path = os.path.join(directory, "slack-queue.jsonl")
            queue = DurableSlackQueue(path)
            queue.enqueue("channel", "message")
            self.assertEqual(queue.pending_count(), 1)
            sender = Mock(return_value=True)
            self.assertEqual(queue.replay(sender), (1, 0))
            sender.assert_called_once_with("channel", "message")
            self.assertEqual(DurableSlackQueue(path).pending_count(), 0)

    def test_restart_budget_stops_after_one_restart_and_resets_after_stability(self):
        budget = RestartBudget(failure_threshold=3, stable_seconds=60)
        self.assertEqual([budget.record_failure(0) for _ in range(2)], ["WAIT", "WAIT"])
        self.assertEqual(budget.record_failure(0), "RESTART")
        self.assertEqual([budget.record_failure(1) for _ in range(2)], ["WAIT", "WAIT"])
        self.assertEqual(budget.record_failure(1), "MANUAL")
        budget.record_success(100)
        self.assertEqual(budget.record_failure(161), "WAIT")
        self.assertEqual(budget.restarts, 0)


if __name__ == "__main__":
    unittest.main()
