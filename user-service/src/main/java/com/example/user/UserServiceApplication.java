package com.example.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class UserServiceApplication { // 1. 클래스 이름 확인

    public static void main(String[] args) {
        // 2. 여기 괄호 안의 이름을 위 클래스 이름과 똑같이 맞춰야 합니다!
        SpringApplication.run(UserServiceApplication.class, args); 
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}