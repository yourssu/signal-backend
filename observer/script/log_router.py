import os


NOTIFICATION_EVENT_FILE = "notification-events.log"


def handlers_for_path(file_path, log_handlers, signal_handlers):
    if file_path.endswith(".gz"):
        return None
    if os.path.basename(file_path) == NOTIFICATION_EVENT_FILE:
        return signal_handlers
    if file_path.endswith(".log"):
        return log_handlers
    return None
