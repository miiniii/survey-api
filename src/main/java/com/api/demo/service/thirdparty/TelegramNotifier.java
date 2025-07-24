package com.api.demo.service.thirdparty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramNotifier {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    public void sendMessage(String message) {
        String url = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host("api.telegram.org")
                .path("/bot" + botToken + "/sendMessage")
                .queryParam("chat_id", chatId)
                .queryParam("text", message)
                .build()
                .toUriString();

        try {
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("텔레그램 메시지 전송 실패: {}", e.getMessage());
        }
    }
}