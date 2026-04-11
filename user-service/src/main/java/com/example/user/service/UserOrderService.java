package com.example.user.service;

import com.example.user.dto.UserOrderRequest;
import com.example.user.entity.UserOrder;
import com.example.user.repository.UserOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserOrderService {

    private final UserOrderRepository repository;
    private final RestTemplate restTemplate;

    public String order(UserOrderRequest request) {
        repository.save(UserOrder.builder()
                .username(request.getUsername())
                .product(request.getProduct())
                .status("시도")
                .build());

        String response = restTemplate.postForObject(
                "http://order-service:8080/order",
                request,
                String.class
        );

        repository.save(UserOrder.builder()
                .username(request.getUsername())
                .product(request.getProduct())
                .status("완료")
                .build());

        return response;
    }
}