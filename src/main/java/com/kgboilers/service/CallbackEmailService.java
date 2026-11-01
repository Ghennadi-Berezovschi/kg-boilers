package com.kgboilers.service;

import com.kgboilers.config.properties.CompanyProperties;
import com.kgboilers.config.properties.ContactProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
public class CallbackEmailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final ContactProperties contactProperties;
    private final CompanyProperties companyProperties;

    public CallbackEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                ContactProperties contactProperties, CompanyProperties companyProperties) {
        this.mailSenderProvider = mailSenderProvider;
        this.contactProperties = contactProperties;
        this.companyProperties = companyProperties;
    }

    @Async("leadEmailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendEmails(CallbackRequestedEvent request) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Callback request email was skipped because JavaMailSender is not configured");
            return;
        }

        if (hasText(contactProperties.getEmail())) {
            sendSafely(mailSender,
                    contactProperties.getEmail(),
                    "New callback request",
                    buildBusinessEmailBody(request));
        }

        if (hasText(request.email())) {
            sendSafely(mailSender,
                    request.email(),
                    "Your " + companyProperties.getName() + " callback request",
                    buildClientEmailBody(request));
        }
    }

    private String buildBusinessEmailBody(CallbackRequestedEvent request) {
        return """
                New callback request received.

                Customer details:
                Name: %s
                Phone: %s
                Email: %s

                Request details:
                Service: %s
                Message: %s
                """.formatted(
                clean(request.name()),
                clean(request.phone()),
                displayOrDash(request.email()),
                displayOrDash(request.service()),
                displayOrDash(request.message())
        );
    }

    private String buildClientEmailBody(CallbackRequestedEvent request) {
        return """
                Hello %s,

                Thank you for contacting %s.
                We received your callback request and will call you back soon.

                Your request:
                Phone: %s
                Service: %s

                Kind regards,
                %s
                """.formatted(
                clean(request.name()),
                companyProperties.getName(),
                clean(request.phone()),
                displayOrDash(request.service()),
                companyProperties.getName()
        );
    }

    private void sendSafely(JavaMailSender mailSender, String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Failed to send callback request email to {}", to, ex);
        }
    }

    private String displayOrDash(String value) {
        return hasText(value) ? value.trim() : "-";
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
