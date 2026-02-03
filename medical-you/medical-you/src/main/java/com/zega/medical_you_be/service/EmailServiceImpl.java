package com.zega.medical_you_be.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of EmailService using Spring Mail and Thymeleaf templates.
 * All email sending is done asynchronously to not block the main thread.
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${password.reset.token.expiration}")
    private long passwordResetExpiration;

    @Value("${email.verification.token.expiration}")
    private long emailVerificationExpiration;

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String name, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", frontendUrl + "/reset-password?token=" + resetToken);
            context.setVariable("expirationMinutes", passwordResetExpiration / 60000);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("password-reset-email", context);

            sendHtmlEmail(to, "Reset Your Password - Medical You", htmlContent);

            LOGGER.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Override
    @Async
    public void sendEmailVerificationEmail(String to, String name, String verificationToken) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationLink", frontendUrl + "/verify-email?token=" + verificationToken);
            context.setVariable("expirationHours", emailVerificationExpiration / 3600000);
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email-verification", context);

            sendHtmlEmail(to, "Verify Your Email - Medical You", htmlContent);

            LOGGER.info("Email verification sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String name) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("loginLink", frontendUrl + "/login");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("welcome-email", context);

            sendHtmlEmail(to, "Welcome to Medical You!", htmlContent);

            LOGGER.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send welcome email to: {}", to, e);
            // Don't throw - welcome email is not critical
        }
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(String to, String name) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("supportEmail", "support@medicalyou.com");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("password-changed-email", context);

            sendHtmlEmail(to, "Your Password Has Been Changed - Medical You", htmlContent);

            LOGGER.info("Password changed email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send password changed email to: {}", to, e);
            // Don't throw - notification email is not critical
        }
    }

    // ==================== APPOINTMENT EMAILS ====================

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    @Override
    @Async
    public void sendAppointmentConfirmationEmail(String to, String patientName, String doctorName,
                                                  String doctorSpecialization, LocalDateTime appointmentDateTime,
                                                  String reason) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("doctorSpecialization", doctorSpecialization);
            context.setVariable("appointmentDate", appointmentDateTime.format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointmentDateTime.format(TIME_FORMATTER));
            context.setVariable("reason", reason != null ? reason : "General Consultation");
            context.setVariable("dashboardLink", frontendUrl + "/patient-profile");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("appointment-confirmation", context);

            sendHtmlEmail(to, "Appointment Confirmed - Medical You", htmlContent);

            LOGGER.info("Appointment confirmation email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send appointment confirmation email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendAppointmentReminderEmail(String to, String patientName, String doctorName,
                                              String doctorSpecialization, LocalDateTime appointmentDateTime) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("doctorSpecialization", doctorSpecialization);
            context.setVariable("appointmentDate", appointmentDateTime.format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointmentDateTime.format(TIME_FORMATTER));
            context.setVariable("dashboardLink", frontendUrl + "/patient-profile");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("appointment-reminder", context);

            sendHtmlEmail(to, "Appointment Reminder - Tomorrow! - Medical You", htmlContent);

            LOGGER.info("Appointment reminder email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send appointment reminder email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendAppointmentCancelledEmail(String to, String patientName, String doctorName,
                                               LocalDateTime appointmentDateTime, String cancellationReason) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("appointmentDate", appointmentDateTime.format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointmentDateTime.format(TIME_FORMATTER));
            context.setVariable("cancellationReason", cancellationReason != null ? cancellationReason : "No reason provided");
            context.setVariable("rebookLink", frontendUrl + "/book-appointment");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("appointment-cancelled", context);

            sendHtmlEmail(to, "Appointment Cancelled - Medical You", htmlContent);

            LOGGER.info("Appointment cancelled email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send appointment cancelled email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendAppointmentRescheduledEmail(String to, String patientName, String doctorName,
                                                 LocalDateTime oldDateTime, LocalDateTime newDateTime) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("oldDate", oldDateTime.format(DATE_FORMATTER));
            context.setVariable("oldTime", oldDateTime.format(TIME_FORMATTER));
            context.setVariable("newDate", newDateTime.format(DATE_FORMATTER));
            context.setVariable("newTime", newDateTime.format(TIME_FORMATTER));
            context.setVariable("dashboardLink", frontendUrl + "/patient-profile");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("appointment-rescheduled", context);

            sendHtmlEmail(to, "Appointment Rescheduled - Medical You", htmlContent);

            LOGGER.info("Appointment rescheduled email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send appointment rescheduled email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendDoctorNewAppointmentEmail(String to, String doctorName, String patientName,
                                               LocalDateTime appointmentDateTime, String reason) {
        try {
            Context context = new Context();
            context.setVariable("doctorName", doctorName);
            context.setVariable("patientName", patientName);
            context.setVariable("appointmentDate", appointmentDateTime.format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointmentDateTime.format(TIME_FORMATTER));
            context.setVariable("reason", reason != null ? reason : "General Consultation");
            context.setVariable("dashboardLink", frontendUrl + "/doctor-dashboard");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("doctor-new-appointment", context);

            sendHtmlEmail(to, "New Appointment Booked - Medical You", htmlContent);

            LOGGER.info("Doctor new appointment email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send doctor new appointment email to: {}", to, e);
        }
    }

    // ==================== CHAT MESSAGE EMAILS ====================

    @Override
    @Async
    public void sendNewChatMessageEmail(String to, String recipientName, String senderName,
                                         String subject, String messagePreview) {
        try {
            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("senderName", senderName);
            context.setVariable("subject", subject);
            context.setVariable("messagePreview", messagePreview != null && messagePreview.length() > 200
                    ? messagePreview.substring(0, 200) + "..." : messagePreview);
            context.setVariable("chatLink", frontendUrl + "/ask-doctor");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("chat-message-notification", context);
            sendHtmlEmail(to, "New Message: " + subject + " - Medical You", htmlContent);

            LOGGER.info("Chat notification email sent to: {}", to);
        } catch (Exception e) {
            LOGGER.error("Failed to send chat notification email to: {}", to, e);
        }
    }

    /**
     * Helper method to send HTML emails.
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom(fromEmail, fromName);
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback to simple from address
            helper.setFrom(fromEmail);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
