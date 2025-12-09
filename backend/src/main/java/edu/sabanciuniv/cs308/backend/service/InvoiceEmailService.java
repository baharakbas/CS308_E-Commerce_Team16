package edu.sabanciuniv.cs308.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class InvoiceEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceEmailService.class);
    private static final String DEFAULT_FROM = "no-reply@tidl.local";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    public InvoiceEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    @Override
    public void sendInvoiceEmail(String to, String subject, String body, byte[] pdfBytes, String filename) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.info("Mail sender not configured. Invoice email to {} skipped. Subject: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(DEFAULT_FROM);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.addAttachment(filename, new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            log.info("Invoice email sent to {}", to);
        } catch (MailException | MessagingException ex) {
            log.warn("Failed to send invoice email to {}. Falling back to log output.", to, ex);
            log.debug("Invoice body for {}: {}", to, body);
        }
    }
}

