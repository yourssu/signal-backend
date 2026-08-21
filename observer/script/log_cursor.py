import glob
import json
import os
import tempfile


class DurableLogCursor:
    def __init__(self, path, state_path, start_at_end_if_missing=False):
        self.path = path
        self.state_path = state_path
        self.corrupted_reason = None
        self.state = self._load()
        if self.state is None and self.corrupted_reason is None and start_at_end_if_missing and os.path.exists(path):
            stat = os.stat(path)
            self.state = {"device": stat.st_dev, "inode": stat.st_ino, "offset": stat.st_size}
            self._save()
        self.file = None

    def _load(self):
        try:
            with open(self.state_path, encoding="utf-8") as file:
                value = json.load(file)
            return {"device": int(value["device"]), "inode": int(value["inode"]), "offset": int(value["offset"])}
        except FileNotFoundError:
            return None
        except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError):
            self.corrupted_reason = "invalid_state"
            return None

    def _save(self):
        os.makedirs(os.path.dirname(self.state_path), exist_ok=True)
        fd, temporary = tempfile.mkstemp(dir=os.path.dirname(self.state_path), prefix=".offset-")
        try:
            with os.fdopen(fd, "w", encoding="utf-8") as file:
                json.dump(self.state, file)
                file.flush()
                os.fsync(file.fileno())
            os.replace(temporary, self.state_path)
        finally:
            if os.path.exists(temporary):
                os.unlink(temporary)

    def _matching_path(self):
        if self.state is None:
            return self.path
        root = os.path.dirname(os.path.dirname(self.path)) if os.path.basename(os.path.dirname(self.path)) == "events" else os.path.dirname(self.path)
        for candidate in [self.path, *glob.glob(os.path.join(root, "**", "*"), recursive=True)]:
            try:
                stat = os.stat(candidate)
                if (stat.st_dev, stat.st_ino) == (self.state["device"], self.state["inode"]):
                    return candidate
            except OSError:
                pass
        return self.path

    def poll(self, handler):
        candidate = self._matching_path()
        if not os.path.exists(candidate):
            return 0
        stat = os.stat(candidate)
        same_file = self.state and (stat.st_dev, stat.st_ino) == (self.state["device"], self.state["inode"])
        if same_file and stat.st_size < self.state["offset"]:
            self.corrupted_reason = "offset_beyond_file"
        offset = self.state["offset"] if same_file and stat.st_size >= self.state["offset"] else 0
        processed = self._consume(candidate, offset, handler)
        if candidate != self.path and os.path.exists(self.path):
            processed += self._consume(self.path, 0, handler)
        return processed

    def _consume(self, path, offset, handler):
        count = 0
        with open(path, "r", encoding="utf-8", errors="replace") as file:
            file.seek(offset)
            while True:
                position = file.tell()
                line = file.readline()
                if not line or not line.endswith("\n"):
                    file.seek(position)
                    break
                handler(line)
                stat = os.fstat(file.fileno())
                self.state = {"device": stat.st_dev, "inode": stat.st_ino, "offset": file.tell()}
                self._save()
                count += 1
        return count

    def close(self):
        return None
