#!/usr/bin/env bash
# 포트폴리오 데모용: loan-engine-service + ngrok 터널을 한 번에 띄운다.
# 사용법: ./scripts/start-demo.sh   (repo 어느 위치에서 실행해도 무방)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_DIR="$SCRIPT_DIR/../loan-engine-service"
PORT=8081

if lsof -i ":$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "이미 $PORT 포트에 서버가 떠 있습니다. 재사용합니다."
else
  echo "Spring Boot 서버 시작 중..."
  (cd "$SERVICE_DIR" && nohup ./mvnw -q spring-boot:run > /tmp/loan-engine.log 2>&1 &)

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
