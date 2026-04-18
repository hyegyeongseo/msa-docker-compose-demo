package com.example.order.consumer;

import com.example.order.dto.OrderEvent;
import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * User Service가 "order-events" 토픽에 발행한 주문 이벤트를 수신.
     * 주문을 처리하고 결과를 "order-result" 토픽에 발행.
     */
    @KafkaListener(topics = "order-events", groupId = "order-service-group")
    public void consumeOrderEvent(String message) {
        OrderEvent event;
        try {
            event = objectMapper.readValue(message, OrderEvent.class);
        } catch (Exception e) {
            log.error("❌ 이벤트 역직렬화 실패", e);
            return;
        }

        log.info("주문 이벤트 수신: eventId={}, user={}, product={}",
                event.getEventId(), event.getUsername(), event.getProduct());

        String resultStatus;
        try {
            // 1. 주문 DB에 저장
            Order order = Order.builder()
                    .username(event.getUsername())
                    .product(event.getProduct())
                    .status("완료")
                    .build();
            orderRepository.save(order);

            // 2. Redis에 상태 캐싱
            String key = "order-status:" + event.getUsername() + "-" + event.getProduct();
            redisTemplate.opsForValue().set(key, "COMPLETE");

            resultStatus = "COMPLETED";
            log.info("✅ 주문 처리 완료: eventId={}", event.getEventId());

        } catch (Exception e) {
            resultStatus = "FAILED";
            log.error("❌ 주문 처리 실패: eventId={}", event.getEventId(), e);
        }

        // 3. 처리 결과를 "order-result" 토픽에 발행
        OrderEvent resultEvent = OrderEvent.builder()
                .eventId(event.getEventId())
                .username(event.getUsername())
                .product(event.getProduct())
                .status(resultStatus)
                .build();

        try {
            String json = objectMapper.writeValueAsString(resultEvent);
            kafkaTemplate.send("order-result", event.getEventId(), json);
            log.info("주문 결과 발행: eventId={}, status={}", event.getEventId(), resultStatus);
        } catch (JsonProcessingException e) {
            log.error("결과 이벤트 직렬화 실패", e);
        }
    }
}
