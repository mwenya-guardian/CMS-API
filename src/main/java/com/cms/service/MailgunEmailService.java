package com.cms.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Base64;

import lombok.AllArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cms.dto.EmailAttachment;

@Service
//@AllArgsConstructor
public class MailgunEmailService implements EmailService {

    @Value("${mailgun.api.key}")
    private final String apiKey;
    @Value("${mailgun.domain}")
    private final String domain;
    @Value("${mailgun.base-url:https://api.mailgun.net/v3}")
    private final String baseUrl; // e.g. https://api.mailgun.net/v3

    public MailgunEmailService(@Value("${mailgun.api.key}") String apiKey,
                                  @Value("${mailgun.domain}") String domain,
                                  @Value("${mailgun.base-url:https://api.mailgun.net/v3}") String baseUrl) {
        this.apiKey = apiKey;
        this.domain = domain;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;

    }

    @Override
    public void sendPlain(String to, String subject, String text) {
        try {
            sendMultipart(to, subject, text, null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        try {
            sendMultipart(to, subject, html, null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void sendWithAttachments(String to, String subject, String html, List<EmailAttachment> attachments) throws IOException {
        sendMultipart(to, subject, html, attachments);
    }

    private void sendMultipart(String to, String subject, String htmlOrText, List<EmailAttachment> attachments) throws IOException {
        String url = baseUrl + "/" + domain + "/messages";
        HttpPost post = new HttpPost(url);

        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        post.addHeader("Authorization", "Basic " + encodedAuth);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("from", "no-reply@" + domain);
        builder.addTextBody("to", to);
        builder.addTextBody("subject", subject);

        // Mailgun accepts both "text" and "html"
        builder.addTextBody("html", htmlOrText, ContentType.create("text/html", StandardCharsets.UTF_8));

        if (attachments != null) {
            for (EmailAttachment att : attachments) {
                // "attachment" is the field name Mailgun expects for attachments
                builder.addBinaryBody("attachment", att.data(), ContentType.create(att.contentType()), att.filename());
            }
        }

        HttpEntity multipart = builder.build();
        post.setEntity(multipart);

        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post)) {
            int status = response.getCode();
            if (status >= 200 && status < 300) {
                // success
                return;
            } else {
                String body = response.getEntity() != null ? new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8) : "";
                throw new IOException("Mailgun API error: status=" + status + " body=" + body);
            }
        }
    }
}
