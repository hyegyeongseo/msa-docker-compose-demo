## 목적

학습 목적의 실습 프로젝트로, MSA 환경에서의 서비스 간 흐름과 Docker Compose 기반 배포 구조 이해에 중점을 두었습니다.

## 실행

```bash
docker compose up -d
```

## 동작 확인

```bash
# 주문 요청
docker compose exec frontend sh
curl -X POST http://user-service:8080/order -H "Content-Type: application/json" -d '{"username":"user01","product":"Apple Pie"}'
exit

# user-db 확인 (주문 시도 + 완료 이력)
docker compose exec user-db mysql -uroot -ppassword --default-character-set=utf8mb4 user_db -e "SELECT * FROM user_orders;"

# order-db 확인 (주문 완료 기록)
docker compose exec order-db mysql -uroot -ppassword --default-character-set=utf8mb4 order_db -e "SELECT * FROM orders;"

# Redis 캐시 확인
docker compose exec redis redis-cli GET "order-status:user01-Apple Pie"
```