import json
import os
import sys
import time
import urllib.request


def observer_healthy(path="/app/logs/state/observer-health.json", max_age=10):
    try:
        with open(path, encoding="utf-8") as file:
            health = json.load(file)
        return time.time() - int(health["checked_at"]) <= max_age
    except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError):
        return False


def spring_healthy(port):
    try:
        with urllib.request.urlopen(f"http://127.0.0.1:{port}/actuator/health", timeout=5) as response:
            return response.status == 200 and json.load(response).get("status") == "UP"
    except Exception:
        return False


if __name__ == "__main__":
    component = sys.argv[1]
    healthy = observer_healthy() if component == "observer" else spring_healthy(os.getenv("SERVER_PORT", "8080"))
    raise SystemExit(0 if healthy else 1)
