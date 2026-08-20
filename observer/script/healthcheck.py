import json
import os
import sys
import time
import urllib.request
import socket


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


def admin_healthy(port=3005):
    try:
        with socket.create_connection(("127.0.0.1", port), timeout=5):
            return True
    except OSError:
        return False


if __name__ == "__main__":
    component = sys.argv[1]
    checks = {
        "spring": lambda: spring_healthy(os.getenv("SERVER_PORT", "8080")),
        "observer": observer_healthy,
        "admin": admin_healthy,
    }
    healthy = checks.get(component, lambda: False)()
    raise SystemExit(0 if healthy else 1)
