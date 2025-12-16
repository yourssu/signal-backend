class LogHandlers:
    def __init__(self, config, notifier):
        self.config = config
        self.notifier = notifier
        
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
        ticket_policy_message = f"- 💰 현재 가격 정책: {self.config.ticket_price_policy}"
        ticket_registered_message = f"- 🌱 프로필 등록 완료 첫 구매 고객: {self.config.ticket_price_registered_policy}"
        message = f"🟢 {self.config.environment.upper()} SERVER RESTARTED - 시그널 API \n\n{ticket_policy_message}\n \n{ticket_registered_message}"
        # self.notifier.send_notification(message)

    def create_internal_error_message(self, line):
        """내부 에러 메시지 생성"""
        message = f"🚨ALERT ERROR - {self.config.environment.upper()} SERVER🚨\n{line.replace(self.INTERNAL_ERROR_LOG_PREFIX, '')}"
        self.notifier.send_log_notification(message)
