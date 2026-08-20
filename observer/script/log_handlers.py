import time
from datetime import datetime, timezone


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
        message = (
            f"🟢 [{self.config.environment.upper()}] Spring API 기동 완료\n"
            f"• 시간: {datetime.now(timezone.utc).isoformat()}\n"
            "• 상태: 요청 수신 가능"
        )
        self.notifier.send_log_notification(message)

    def create_internal_error_message(self, line):
        """내부 에러 메시지 생성"""
        detail = line.replace(self.INTERNAL_ERROR_LOG_PREFIX, '')
        cause = detail.splitlines()[0][:200]
        now = time.monotonic()
        if now - self.last_error_alert.get(cause, 0) < 300:
            return
        self.last_error_alert[cause] = now
        message = (
            f"🔴 [{self.config.environment.upper()}] Spring API 내부 오류\n"
            f"• 오류: {detail}\n"
            "• 확인: 동일 오류는 5분간 중복 알림을 제한합니다."
        )
        self.notifier.send_log_notification(message)
