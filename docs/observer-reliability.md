# Observer reliability 운영

배포 스크립트는 DEV와 PROD 각각에서 같은 이미지로 Spring(`COMPONENT=spring`), observer(`COMPONENT=observer`), admin(`COMPONENT=admin`)을 별도 컨테이너로 실행한다. 각 admin은 Docker 내부 네트워크로 같은 환경의 Spring만 호출한다. 운영 명령은 기존 `/t`, `/add`, `/delete`를 사용하고 DEV 검증은 `/dev t`, `/dev add`, `/dev delete`를 사용한다. Docker healthcheck가 3회 연속 실패하면 systemd가 관리하는 `supervise.sh`가 해당 컨테이너만 한 번 재시작하고, 재차 unhealthy가 되면 자동 복구를 멈춘다. 5분(정상 healthcheck 10회) 동안 안정적으로 동작하면 restart budget이 초기화된다.

로그 볼륨의 `state/`에는 이벤트/app offset, Slack 실패 큐, 컴포넌트별 restart budget이 저장된다. 이 디렉터리를 삭제하면 이어읽기와 재시작 예산도 초기화되므로 장애 대응 중 임의 삭제하지 않는다. rotation archive는 재시작 후 inode를 찾아 남은 내용을 읽을 수 있도록 압축하지 않는다.

배포 후 다음을 확인한다.

- `${PROJECT_NAME}-spring`, `${PROJECT_NAME}-observer`, `${PROJECT_NAME}-admin`이 각각 `healthy`인지 확인한다.
- `${PROJECT_NAME}-supervisor.service`가 `active (running)`인지 확인한다. systemd가 supervisor 종료와 EC2 재부팅 후 자동으로 다시 실행한다. 이번 감시 범위는 EC2 내부 컴포넌트이며 외부 dead-man monitor는 사용하지 않는다.
- observer 컨테이너만 중지해 observer만 한 번 재시작되고 Spring 컨테이너 ID/시작 시간이 바뀌지 않는지 확인한다. 다시 health 실패를 만들면 `MANUAL ACTION REQUIRED` 이후 반복 재시작하지 않는지 확인한다.
- `logs/state/slack-queue.jsonl`에 실패 건이 남고 Slack 복구 후 0건이 되는지 확인한다.

Admin은 DEV와 PROD에 각각 실행하며 Docker 내부 네트워크의 같은 환경 Spring만 호출한다. 기존 `/t`, `/add`, `/delete`의 Slack Request URL은 PROD로, `/dev`의 Request URL은 DEV로 연결한다. Admin도 Spring, observer와 같은 3회 실패/1회 자동 재시작 정책을 적용한다.
