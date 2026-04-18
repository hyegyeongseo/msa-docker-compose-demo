package com.example.user.service;

import com.example.user.dto.OrderEvent;
import com.example.user.dto.UserOrderRequest;
import com.example.user.entity.UserOrder;
import com.example.user.repository.UserOrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final UserOrderRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public String order(UserOrderRequest request) {

        UserOrder userOrder = UserOrder.builder()
                .username(request.getUsername())
                .product(request.getProduct())
                .status("REQUESTED")
                .build();
        repository.save(userOrder);

        String eventId = UUID.randomUUID().toString();
        OrderEvent event = OrderEvent.builder()
                .eventId(eventId)
                .username(request.getUsername())
                .product(request.getProduct())
                .status("REQUESTED")
                .build();

        try {
            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send("order-events", eventId, json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("❌ Kafka 전송 실패: eventId={}", eventId, ex);
                        } else {
                            log.info("✅ Kafka 전송 성공: eventId={}", eventId);
                        }
                    });

        } catch (JsonProcessingException e) {
            log.error("❌ 이벤트 직렬화 실패", e);
            return "주문 처리 중 오류가 발생했습니다.";
        }

        // DB 저장과 Kafka 발행 간 정합성 문제 존재
        return request.getUsername() + "님의 주문이 접수되었습니다. (eventId: " + eventId + ")";
    }
}