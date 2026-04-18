package com.example.user.consumer;

import com.example.user.dto.OrderEvent;
import com.example.user.entity.UserOrder;
import com.example.user.repository.UserOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderResultConsumer {

    private final UserOrderRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Order Service가 주문 처리 후 "order-result" 토픽에 발행한 결과를 수신.
     * 수신한 결과에 따라 user_orders 테이블의 상태를 업데이트.
     */
    @KafkaListener(topics = "order-result", groupId = "user-service-group")
    public void consumeOrderResult(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);

            // 결과 상태에 따라 DB 업데이트
            String status = "COMPLETED".equals(event.getStatus()) ? "완료" : "실패";

            repository.save(UserOrder.builder()
                    .username(event.getUsername())
                    .product(event.getProduct())
                    .status(status)
                    .build());

            log.info("📬 주문 결과 수신: eventId={}, status={}, user={}, product={}",
                    event.getEventId(), status, event.getUsername(), event.getProduct());

        } catch (Exception e) {
            log.error("❌ 주문 결과 처리 실패", e);
        }
    }
}
