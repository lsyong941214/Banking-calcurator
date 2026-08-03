#!/usr/bin/env bash
# 포트폴리오 데모용: loan-calcurator(화면) + ngrok 터널을 한 번에 띄운다.
# loan-calcurator는 정적 화면만 서빙하고, 실제 이자계산/상환스케줄/코드값조회/원장조회는
# 브라우저에서 각 백엔드 서비스(Render 배포본 또는 localhost)를 직접 호출한다. 이 화면을
# ngrok으로 노출하면 hostname이 localhost가 아니게 되어 자동으로 Render 프로덕션 백엔드
# 4종(engine/schedule/code/ledger)을 호출하므로, 백엔드까지 별도로 터널링할 필요는 없다.
# 사용법: ./scripts/start-demo.sh   (repo 어느 위치에서 실행해도 무방)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_DIR="$SCRIPT_DIR/../loan-calcurator"
PORT=8080

if lsof -i ":$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "이미 $PORT 포트에 서버가 떠 있습니다. 재사용합니다."
else
  echo "Spring Boot 서버 시작 중..."
  (cd "$SERVICE_DIR" && nohup ./mvnw -q spring-boot:run > /tmp/loan-calcurator.log 2>&1 &)

  echo -n "서버 기동 대기"
  for i in $(seq 1 60); do
    if curl -s -o /dev/null "http://localhost:$PORT/"; then
      echo " 완료"
      break
    fi
    echo -n "."
    sleep 2
  done
fi

if pgrep -f "ngrok http $PORT" >/dev/null 2>&1; then
  echo "ngrok이 이미 실행 중입니다."
else
  echo "ngrok 터널 시작 중..."
  nohup ngrok http "$PORT" --log=stdout > /tmp/ngrok.log 2>&1 &
  sleep 3
fi

echo ""
echo "공개 URL:"
curl -s http://127.0.0.1:4040/api/tunnels | python3 -c "import json,sys; print(json.load(sys.stdin)['tunnels'][0]['public_url'])" 2>/dev/null || echo "  (아직 준비 중 - 잠시 후 http://127.0.0.1:4040 에서 확인)"
