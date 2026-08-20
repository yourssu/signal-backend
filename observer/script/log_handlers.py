import time
from datetime import datetime
import re


class LogHandlers:
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier
        self.last_error_alert = {}
        
        # 로그 패턴 정의
        self.SERVER_RESTART = 'INFO org.springframework.boot.web.embedded.tomcat.TomcatWebServer - Tomcat started on port'
        self.INTERNAL_ERROR_LOG_PREFIX = 'ERROR com.yourssu.signal.handler.InternalServerErrorControllerAdvice -'
        
        # 핸들러 매핑 (로그 전용)
        self.handlers = {
            self.SERVER_RESTART: self.create_server_restart_message,
            self.INTERNAL_ERROR_LOG_PREFIX: self.create_internal_error_message,
        }
    
    def create_server_restart_message(self, line):
        """서버 재시작 메시지 생성"""
        project_name = getattr(self.config, "project_name", "signal-backend")
        message = (
            f"🟢 [{self.config.environment.upper()}] Spring API 기동 완료\n"
            "```\n"
            f"• 시간: {datetime.now().astimezone().isoformat(timespec='seconds')}\n"
            f"• 컨테이너: {project_name}-spring\n"
            "• 상태: 요청 수신 가능\n"
            "```"
        )
        self.notifier.send_log_notification(message)

    def create_internal_error_message(self, line):
        """내부 에러 메시지 생성"""
        project_name = getattr(self.config, "project_name", "signal-backend")
        detail = line.replace(self.INTERNAL_ERROR_LOG_PREFIX, '')
        cause = detail.splitlines()[0][:200]
        now = time.monotonic()
        if now - self.last_error_alert.get(cause, 0) < 300:
            return
        self.last_error_alert[cause] = now
        timestamp_match = re.match(r"(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})", line)
        trace_match = re.search(r"\[traceId=([^\]]+)]", line)
        occurred_at = timestamp_match.group(1) if timestamp_match else datetime.now().astimezone().isoformat(timespec="seconds")
        trace_id = trace_match.group(1) if trace_match else "none"
        lookup = (
            f"docker logs --since 15m {project_name}-spring 2>&1 | grep 'traceId={trace_id}'"
            if trace_id != "none"
            else f"grep -F '{occurred_at}' logs/app.log"
        )
        message = (
            f"🔴 [{self.config.environment.upper()}] Spring API 내부 오류\n"
            "```\n"
            f"• 발생 시간: {occurred_at}\n"
            f"• traceId: {trace_id}\n"
            "• 로그: logs/app.log\n"
            f"• 오류: {detail}\n"
            f"• 검색: {lookup}\n"
            "• 알림 정책: 동일 오류는 5분간 중복 제한\n"
            "```"
        )
        self.notifier.send_log_notification(message)
