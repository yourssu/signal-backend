import json
import os
import tempfile
import time


class DurableSlackQueue:
    def __init__(self, path):
        self.path = path
        os.makedirs(os.path.dirname(path), exist_ok=True)

    def enqueue(self, channel, message):
        record = {"channel": channel, "message": message, "queued_at": int(time.time())}
        with open(self.path, "a", encoding="utf-8") as file:
            file.write(json.dumps(record, ensure_ascii=False) + "\n")
            file.flush()
            os.fsync(file.fileno())

    def _records(self):
        if not os.path.exists(self.path):
            return []
        records = []
        with open(self.path, encoding="utf-8") as file:
            for line in file:
                try:
                    record = json.loads(line)
                    if isinstance(record.get("channel"), str) and isinstance(record.get("message"), str):
                        records.append(record)
                except (json.JSONDecodeError, TypeError):
                    continue
        return records

    def pending_count(self):
        return len(self._records())

    def replay(self, sender):
        records = self._records()
        completed = 0
        for record in records:
            if not sender(record["channel"], record["message"]):
                break
            completed += 1
        remaining = records[completed:]
        directory = os.path.dirname(self.path)
        fd, temporary = tempfile.mkstemp(dir=directory, prefix=".slack-queue-")
        try:
            with os.fdopen(fd, "w", encoding="utf-8") as file:
                for record in remaining:
                    file.write(json.dumps(record, ensure_ascii=False) + "\n")
                file.flush()
                os.fsync(file.fileno())
            os.replace(temporary, self.path)
        finally:
            if os.path.exists(temporary):
                os.unlink(temporary)
        return completed, len(remaining)

