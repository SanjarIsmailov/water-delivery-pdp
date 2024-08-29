package com.example.demo;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class WaterDeliveryPdpApplication {

    @Value("${bot.token}")
    private String token;

    public static void main(String[] args) {
        SpringApplication.run(WaterDeliveryPdpApplication.class, args);
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }



}
