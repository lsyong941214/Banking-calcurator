#!/usr/bin/env bash
# loan-engine-service를 재빌드하고, 로컬에서 돌고 있는 인스턴스를 재시작한다.
# ngrok은 포트만 보고 있으므로 별도 재시작 없이 새 프로세스로 자동 연결된다.
# post-commit 훅(scripts/git-hooks/post-commit)에서 자동 호출된다.
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVICE_DIR="$SCRIPT_DIR/../loan-engine-service"
PORT=8081

echo "[rebuild-and-restart] 빌드 중..."
(cd "$SERVICE_DIR" && ./mvnw -q -B clean package -DskipTests)

echo "[rebuild-and-restart] 기존 서버 종료..."
pkill -f "com.example.engine.LoanEngineServiceApplication" 2>/dev/null || true
pkill -f "loan-engine-service-.*\.jar" 2>/dev/null || true
sleep 1

JAR="$(ls "$SERVICE_DIR"/target/*.jar | head -1)"
echo "[rebuild-and-restart] 서버 재시작: $(basename "$JAR")"
nohup java -jar "$JAR" > /tmp/loan-engine.log 2>&1 &

echo -n "[rebuild-and-restart] 기동 대기"
for i in $(seq 1 60); do
  if curl -s -o /dev/null "http://localhost:$PORT/"; then
    echo " 완료"
    break
  fi
  echo -n "."
  sleep 2
done

if ! pgrep -f "ngrok http $PORT" >/dev/null 2>&1; then
  echo "[rebuild-and-restart] ngrok이 꺼져 있어 다시 시작합니다."
  nohup ngrok http "$PORT" --log=stdout > /tmp/ngrok.log 2>&1 &
  sleep 3
fi

echo "[rebuild-and-restart] 완료."
