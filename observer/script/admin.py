import logging
import os
import requests
from slack_bolt import App
from dotenv import load_dotenv

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

load_dotenv()  # .env 파일 환경변수 불러옴

SLACK_TOKEN = os.getenv('SLACK_TOKEN')
SLACK_SIGNING_SECRET = os.getenv('SLACK_SIGNING_SECRET')
SLACK_CHANNEL = os.getenv('SLACK_CHANNEL')
SLACK_ADMIN_CHANNEL = os.getenv('SLACK_ADMIN_CHANNEL')
ADMIN_ACCESS_KEY = os.getenv('ADMIN_ACCESS_KEY')
ENVIRONMENT = os.getenv('ENVIRONMENT', 'dev').lower()
SERVER_PORT = os.getenv('SERVER_PORT', '8080')
PROJECT_NAME = os.getenv('PROJECT_NAME', 'signal-backend')
API_HOST = f'http://{PROJECT_NAME}-spring:{SERVER_PORT}'

# 최대 티켓 개수
max_ticket = 10
# 기본 티켓 개수
default_ticket = 1

app = App(
    token=SLACK_TOKEN,
    signing_secret=SLACK_SIGNING_SECRET
)

@app.command("/t")
def handle_command(ack, command, say, respond):
    ack()

    try:
        if command.get("channel_id") != SLACK_CHANNEL:
            respond("❌ 이 명령은 결제 알림 채널에서만 사용할 수 있습니다.")
            return

        args = command['text'].split()
        user_name = command["user_name"]

        if len(args) < 1:
            respond("❌ 사용법: /t [인증번호] <티켓 개수>")
            return

        verification_code = args[0]
        ticket = int(args[1]) if len(args) > 1 else default_ticket

        if (ticket > max_ticket):
            respond(f"❌ 티켓 개수는 {max_ticket}개를 초과할 수 없습니다.")
            return

        response = reply(verification_code, ticket)

        # 응답 확인
        if response.status_code == 200:
            message = f"✅ 인증 성공! 인증 번호 {verification_code}이 확인되었습니다."
            respond(message)
            say(f"✅ @{user_name} 님이 이용권 발급을 요청했습니다.")
        else:
            respond(f"❌ 인증 실패: {response.text}")

    except ValueError:
        message = "❌ 티켓 번호는 숫자여야 합니다."
        respond(message)
        logger.error(f"{message}")
    except Exception as e:
        message = f"❌ 오류 발생: {str(e)}"
        respond(message)
        logger.error(f"{message}", exc_info=True)


def reply(verification_code, ticket):
    return requests.post(
        f'{API_HOST}/api/viewers',
        json={
            "secretKey": ADMIN_ACCESS_KEY,
            "verificationCode": verification_code,
            "ticket": ticket
        },
        headers={'Content-Type': 'application/json'}
    )


@app.command("/add")
def handle_command(ack, command, say, respond):
    ack()

    try:
        if command.get("channel_id") != SLACK_ADMIN_CHANNEL:
            respond("❌ 이 명령은 관리자 채널에서만 사용할 수 있습니다.")
            return

        args = command['text'].split()

        if len(args) < 1:
            respond("❌ 사용법: /add [식별번호]")
            return

        profile_id = args[0]

        response = reply_add(profile_id)

        # 응답 확인
        if response.status_code == 201:
            message = f"✅ 블랙리스트 등록 성공! 식별 번호 {profile_id}이 블랙리스트에 등록되었습니다.\n사용법: /add [식별 번호] or /delete [식별 번호]"
            say(message)
        else:
            respond(f"❌ 블랙리스트 등록 실패: {response.text}")
    except Exception as e:
        message = f"❌ 오류 발생: {str(e)}"
        respond(message)
        logger.error(f"{message}", exc_info=True)


def reply_add(profile_id):
    return requests.post(
        f'{API_HOST}/api/blacklists',
        json={
            "secretKey": ADMIN_ACCESS_KEY,
            "profileId": profile_id
        },
        headers={'Content-Type': 'application/json'}
    )


@app.command("/delete")
def handle_command(ack, command, say, respond):
    ack()

    try:
        if command.get("channel_id") != SLACK_ADMIN_CHANNEL:
            respond("❌ 이 명령은 관리자 채널에서만 사용할 수 있습니다.")
            return

        args = command['text'].split()

        if len(args) < 1:
            respond("❌ 사용법: /delete [식별번호]")
            return

        profile_id = args[0]

        response = reply_delete(profile_id)

        # 응답 확인
        if response.status_code == 204:
            message = f"📕 블랙리스트 삭제 성공! 식별 번호 {profile_id}이 블랙리스트에서 삭제되었습니다.\n사용법: /add [식별 번호] or /delete [식별 번호]"
            say(message)
        else:
            respond(f"❌ 블랙리스트 삭제 실패: {response.text}")
    except Exception as e:
        message = f"❌ 오류 발생: {str(e)}"
        say(message)
        logger.error(f"{message}", exc_info=True)



def reply_delete(profile_id):
    return requests.delete(
        f'{API_HOST}/api/blacklists/{profile_id}',
        params={"secretKey": ADMIN_ACCESS_KEY},
    )


@app.command("/dev")
def handle_dev_command(ack, command, say, respond):
    ack()

    if ENVIRONMENT != "dev":
        respond("❌ /dev 명령은 DEV 환경에서만 사용할 수 있습니다.")
        return

    args = command.get("text", "").split()
    if not args:
        respond("❌ 사용법: /dev t [인증번호] <개수> | /dev add [식별번호] | /dev delete [식별번호]")
        return

    subcommand = args[0].lower()
    try:
        if subcommand == "t":
            if command.get("channel_id") != SLACK_CHANNEL:
                respond("❌ 이 명령은 DEV 결제 알림 채널에서만 사용할 수 있습니다.")
                return
            if len(args) < 2:
                respond("❌ 사용법: /dev t [인증번호] <티켓 개수>")
                return
            verification_code = args[1]
            ticket = int(args[2]) if len(args) > 2 else default_ticket
            if ticket > max_ticket:
                respond(f"❌ 티켓 개수는 {max_ticket}개를 초과할 수 없습니다.")
                return
            response = reply(verification_code, ticket)
            if response.status_code == 200:
                respond(f"✅ DEV 인증 성공! 인증 번호 {verification_code}이 확인되었습니다.")
                say(f"✅ @{command['user_name']} 님이 DEV 이용권 발급을 요청했습니다.")
            else:
                respond(f"❌ DEV 인증 실패: {response.text}")
            return

        if subcommand in ("add", "delete"):
            if command.get("channel_id") != SLACK_ADMIN_CHANNEL:
                respond("❌ 이 명령은 관리자 채널에서만 사용할 수 있습니다.")
                return
            if len(args) < 2:
                respond(f"❌ 사용법: /dev {subcommand} [식별번호]")
                return
            profile_id = args[1]
            response = reply_add(profile_id) if subcommand == "add" else reply_delete(profile_id)
            expected_status = 201 if subcommand == "add" else 204
            if response.status_code == expected_status:
                action = "등록" if subcommand == "add" else "삭제"
                say(f"✅ DEV 블랙리스트 {action} 성공! 식별 번호 {profile_id}")
            else:
                respond(f"❌ DEV 블랙리스트 처리 실패: {response.text}")
            return

        respond("❌ 지원하지 않는 DEV 명령입니다. 사용법: /dev t|add|delete ...")
    except ValueError:
        respond("❌ 티켓 개수는 숫자여야 합니다.")
    except Exception as e:
        respond(f"❌ 오류 발생: {str(e)}")
        logger.error("DEV 명령 처리 중 오류 발생", exc_info=True)


@app.command("/report")
def handle_report_command(ack, command, say, respond):
    ack()

    try:
        args = command['text'].split()
        if len(args) != 1 or not args[0].isdigit() or int(args[0]) <= 0:
            respond("❌ 사용법: /report [신고 ID]")
            return

        report_id = args[0]
        response = reply_report(report_id, command)
        if response.status_code == 200:
            say(f"✅ 신고 승인 성공! 신고 ID {report_id}의 블랙리스트 처리와 티켓 보상이 완료되었습니다.")
        else:
            respond(f"❌ 신고 승인 실패: {response.text}")
    except Exception as e:
        message = f"❌ 오류 발생: {str(e)}"
        respond(message)
        logger.error(f"{message}", exc_info=True)


def reply_report(report_id, command):
    api_url = API_HOST_PROD if command["channel_id"] == SLACK_CHANNEL_ADMIN else API_HOST_DEV
    secret_key = SECRET_KEY_PROD if command["channel_id"] == SLACK_CHANNEL_ADMIN else SECRET_KEY_DEV
    return requests.post(
        f'{api_url}/api/reports/{report_id}/approve',
        json={"secretKey": secret_key},
        headers={'Content-Type': 'application/json'}
    )


def start_app(port):
    """애플리케이션 시작"""
    try:
        logger.info(f"슬랙 봇 서버를 포트 {port}에서 시작합니다...")
        app.start(port=port)
    except Exception as e:
        logger.error(f"서버 시작 중 오류 발생: {str(e)}", exc_info=True)
        exit(1)

if __name__ == "__main__":
    start_app(port=3005)
