package com.cms.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.cms.dto.EmailAttachment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Primary
public class GmailSmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public GmailSmtpEmailService(JavaMailSender mailSender,
                                 @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendPlain(String to, String subject, String text) throws MessagingException {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, false);
        mailSender.send(mime);
    }

    @Override
    public void sendHtml(String to, String subject, String html) throws MessagingException {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(mime);
    }

    // mark as @Async if you enable async and don't want to block request threads
    @Override
    @Async
    public void sendWithAttachments(String to, String subject, String html, List<EmailAttachment> attachments) throws MessagingException, IOException {
        MimeMessage mime = mailSender.createMimeMessage();
        // multipart = true enables attachments
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        if (attachments != null) {
            for (EmailAttachment att : attachments) {
                InputStreamSource source = new InputStreamResource(new ByteArrayInputStream(att.data()));
                // addAttachment(filename, inputStreamSource, contentType)
                helper.addAttachment(att.filename(), source, att.contentType());
            }
        }
        mailSender.send(mime);
    }
}

