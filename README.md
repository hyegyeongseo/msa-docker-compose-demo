## 목적

MSA 환경에서의 서비스 흐름 체험용 간이 프로젝트입니다.
프로덕션 수준의 구현이 아닌 아키텍처 패턴 학습 목적으로 작성했습니다.

## 실행
docker compose up -d

## 동작 확인

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