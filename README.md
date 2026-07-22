# Banking-calcurator
상환 시뮬레이터, 스케줄, 이자계산 로직 등

## 접속 주소

- **Render (상시)**: https://banking-calcurator.onrender.com
- **ngrok (로컬 PC 실행 중일 때만)**: https://whimsical-unadvised-divisibly.ngrok-free.dev

## 로컬 데모 실행

```
~/Desktop/Valueup2026/int_calc/scripts/start-demo.sh
```

서버(8081) + ngrok 터널을 한 번에 띄운다. `loan-engine-service`를 커밋하면
`.git/hooks/post-commit`이 자동으로 `scripts/rebuild-and-restart.sh`를 백그라운드
실행해서, 로컬에서 돌고 있는 서버를 최신 코드로 재빌드/재시작한다 (ngrok URL은
그대로 유지되고 뒤에서 서비스되는 내용만 바뀐다).
