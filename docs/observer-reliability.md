# Observer reliability 운영

배포 스크립트는 같은 이미지에서 Spring(`COMPONENT=spring`)과 observer(`COMPONENT=observer`)를 별도 컨테이너로 실행한다. PROD에서는 기존 Slack 명령 라우터인 admin(`COMPONENT=admin`)도 독립 컨테이너로 실행한다. Docker healthcheck가 3회 연속 실패하면 systemd가 관리하는 `supervise.sh`가 해당 컨테이너만 한 번 재시작하고, 재차 unhealthy가 되면 자동 복구를 멈춘다. 5분(정상 healthcheck 10회) 동안 안정적으로 동작하면 restart budget이 초기화된다.

로그 볼륨의 `state/`에는 이벤트/app offset, Slack 실패 큐, 컴포넌트별 restart budget이 저장된다. 이 디렉터리를 삭제하면 이어읽기와 재시작 예산도 초기화되므로 장애 대응 중 임의 삭제하지 않는다. rotation archive는 재시작 후 inode를 찾아 남은 내용을 읽을 수 있도록 압축하지 않는다.

배포 후 다음을 확인한다.

- `${PROJECT_NAME}-spring`, `${PROJECT_NAME}-observer`가 각각 `healthy`인지 확인한다.
- `${PROJECT_NAME}-supervisor.service`가 `active (running)`인지 확인한다. systemd가 supervisor 종료와 EC2 재부팅 후 자동으로 다시 실행한다. EC2 자체 장애와 Slack 장애는 같은 서버가 알릴 수 없으므로 Slack과 독립된 외부 dead-man monitor가 별도로 필요하다.
- observer 컨테이너만 중지해 observer만 한 번 재시작되고 Spring 컨테이너 ID/시작 시간이 바뀌지 않는지 확인한다. 다시 health 실패를 만들면 `MANUAL ACTION REQUIRED` 이후 반복 재시작하지 않는지 확인한다.
- `logs/state/slack-queue.jsonl`에 실패 건이 남고 Slack 복구 후 0건이 되는지 확인한다.

Admin은 PROD에서 한 개만 실행하며 PROD/DEV 결제 채널 ID에 따라 기존처럼 대상 API와 관리자 키를 선택한다. `/add`, `/delete`는 기존 관리자 채널에서 PROD API를 호출한다. Admin도 Spring, observer와 같은 3회 실패/1회 자동 재시작 정책을 적용한다.
