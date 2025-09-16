package com.cms.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cms.dto.EmailAttachment;
import com.cms.service.EmailService;
import com.cms.service.GmailSmtpEmailService;
import jakarta.mail.MessagingException;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class MailController {

    private final GmailSmtpEmailService emailService;

    @PostMapping("/send-with-attachments")
    public ResponseEntity<String> sendWithAttachments(@RequestParam("to") String to,
                                                      @RequestParam("subject") String subject,
                                                      @RequestParam("html") String html,
                                                      @RequestParam(value = "files", required = false) MultipartFile[] files) {
        try {
            List<EmailAttachment> attachments = new ArrayList<>();
            if (files != null) {
                for (MultipartFile mf : files) {
                    attachments.add(new EmailAttachment(mf.getOriginalFilename(), mf.getBytes(), mf.getContentType() == null ? "application/octet-stream" : mf.getContentType(), false));
                }
            }
            emailService.sendWithAttachments(to, subject, html, attachments);
            return ResponseEntity.ok("sent");
        } catch (MessagingException | IOException ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }
}

