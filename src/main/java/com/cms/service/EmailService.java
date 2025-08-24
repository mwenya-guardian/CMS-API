package com.cms.service;


import com.cms.dto.EmailAttachment;
import com.cms.dto.SendResult;
import java.io.IOException;
import jakarta.mail.MessagingException;


import java.util.List;

public interface EmailService {
    void sendPlain(String to, String subject, String text)throws MessagingException, IOException;
    void sendHtml(String to, String subject, String html) throws MessagingException, IOException;
    void sendWithAttachments(String to,
                                   String subject,
                                   String html,
                                   List<EmailAttachment> attachments) throws MessagingException, IOException;
}

