import json
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
SLACK_CHANNEL_PROD = os.getenv('SLACK_CHANNEL_PROD')
SLACK_CHANNEL_DEV = os.getenv('SLACK_CHANNEL_DEV')
SLACK_CHANNEL_ADMIN = os.getenv('SLACK_CHANNEL_ADMIN')
API_HOST_PROD = os.getenv('API_HOST_PROD')
API_HOST_DEV = os.getenv('API_HOST_DEV')
SECRET_KEY_PROD = os.getenv('SECRET_KEY_PROD')
SECRET_KEY_DEV = os.getenv('SECRET_KEY_DEV')
ADMIN_PASS_CODE = os.getenv('PASSCODE')

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

        response = reply(verification_code, ticket, command)

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


def reply(verification_code, ticket, command):
    api_url = API_HOST_PROD if command["channel_id"] == SLACK_CHANNEL_PROD else API_HOST_DEV
    secret_key = SECRET_KEY_PROD if command["channel_id"] == SLACK_CHANNEL_PROD else SECRET_KEY_DEV
    return requests.post(
        f'{api_url}/api/viewers',
        json={
            "secretKey": secret_key,
            "verificationCode": verification_code,
            "ticket": ticket
        },
        headers={'Content-Type': 'application/json'}
    )


@app.command("/add")
def handle_command(ack, command, say, respond):
    ack()

    try:
        args = command['text'].split()

        if len(args) < 1:
            respond("❌ 사용법: /add [식별번호]")
            return

        profile_id = args[0]

        response = reply_add(profile_id, command)

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


def reply_add(profile_id, command):
    api_url = API_HOST_PROD if command["channel_id"] == SLACK_CHANNEL_ADMIN else API_HOST_DEV
    secret_key = SECRET_KEY_PROD if command["channel_id"] == SLACK_CHANNEL_ADMIN else SECRET_KEY_DEV
    return requests.post(
        f'{api_url}/api/blacklists',
        json={
            "secretKey": secret_key,
            "profileId": profile_id
        },
        headers={'Content-Type': 'application/json'}
    )


@app.command("/delete")
def handle_command(ack, command, say, respond):
    ack()

    try:
        args = command['text'].split()

        if len(args) < 1:
            respond("❌ 사용법: /delete [식별번호]")
            return

        profile_id = args[0]

        response = reply_delete(profile_id, command)

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



def reply_delete(profile_id, command):
    api_url = API_HOST_PROD if command["channel_id"] == SLACK_CHANNEL_ADMIN else API_HOST_DEV
    secret_key = SECRET_KEY_PROD if command["channel_id"] == SLACK_CHANNEL_ADMIN else SECRET_KEY_DEV
    return requests.delete(f'{api_url}/api/blacklists/{profile_id}?secretKey={secret_key}')


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
