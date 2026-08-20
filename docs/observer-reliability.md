# Observer reliability 운영

배포 스크립트는 같은 이미지에서 Spring(`COMPONENT=spring`)과 observer(`COMPONENT=observer`)를 별도 컨테이너로 실행한다. Docker healthcheck가 3회 연속 실패하면 호스트의 `supervise.sh`가 해당 컨테이너만 한 번 재시작하고, 재차 unhealthy가 되면 자동 복구를 멈춘다. 5분(정상 healthcheck 10회) 동안 안정적으로 동작하면 restart budget이 초기화된다.

로그 볼륨의 `state/`에는 이벤트/app offset, Slack 실패 큐, 컴포넌트별 restart budget이 저장된다. 이 디렉터리를 삭제하면 이어읽기와 재시작 예산도 초기화되므로 장애 대응 중 임의 삭제하지 않는다. rotation archive는 재시작 후 inode를 찾아 남은 내용을 읽을 수 있도록 압축하지 않는다.

배포 후 다음을 확인한다.

- `${PROJECT_NAME}-spring`, `${PROJECT_NAME}-observer`가 각각 `healthy`인지 확인한다.
- `supervisor.pid`의 프로세스와 `logs/supervisor.log`가 살아 있는지 외부 dead-man monitor에서 확인한다. Slack 자체 장애도 탐지해야 하므로 이 검사는 Slack과 독립된 기존 서버/호스트 모니터에 등록해야 한다.
- observer 컨테이너만 중지해 observer만 한 번 재시작되고 Spring 컨테이너 ID/시작 시간이 바뀌지 않는지 확인한다. 다시 health 실패를 만들면 `MANUAL ACTION REQUIRED` 이후 반복 재시작하지 않는지 확인한다.
- `logs/state/slack-queue.jsonl`에 실패 건이 남고 Slack 복구 후 0건이 되는지 확인한다.

admin 런타임은 #153 기준 자동 실행이 제거되어 있고, 승인된 `SLACK_SIGNING_SECRET`도 없다. 따라서 이 PR은 insecure admin 서버를 기동하거나 새 환경변수를 추가하지 않으며 `/t`, `/add`, `/delete` 구현을 변경하지 않는다. signing secret과 독립 배포 경로가 승인되는 후속 통합에서 `ADMIN STARTED`, admin healthcheck 및 동일 restart budget 계약을 연결해야 한다.
