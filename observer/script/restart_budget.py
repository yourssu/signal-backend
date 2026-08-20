class RestartBudget:
    def __init__(self, failure_threshold=3, stable_seconds=300):
        self.failure_threshold = failure_threshold
        self.stable_seconds = stable_seconds
        self.failures = 0
        self.restarts = 0
        self.healthy_since = None

    def record_failure(self, now):
        if self.healthy_since is not None and now - self.healthy_since >= self.stable_seconds:
            self.restarts = 0
        self.healthy_since = None
        self.failures += 1
        if self.failures < self.failure_threshold:
            return "WAIT"
        self.failures = 0
        if self.restarts == 0:
            self.restarts = 1
            return "RESTART"
        return "MANUAL"

    def record_success(self, now):
        self.failures = 0
        if self.healthy_since is None:
            self.healthy_since = now
        elif now - self.healthy_since >= self.stable_seconds:
            self.restarts = 0

