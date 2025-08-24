package com.cms.config;

import com.mailgun.client.MailgunClient;
import com.mailgun.api.v3.MailgunMessagesApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailgunConfig {

    @Value("${mailgun.api.key}")
    private String apiKey;

    @Value("${mailgun.base-url:}")
    private String baseUrl;

    @Bean
    public MailgunMessagesApi mailgunMessagesApi() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return MailgunClient.config(apiKey).createApi(MailgunMessagesApi.class);
        } else {
            return MailgunClient.config(baseUrl, apiKey).createApi(MailgunMessagesApi.class);
        }
    }
}

