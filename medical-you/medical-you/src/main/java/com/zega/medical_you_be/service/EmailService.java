package com.zega.medical_you_be.service;

/**
 * Service interface for sending emails.
 * Handles password reset, email verification, and notification emails.
 */
public interface EmailService {

    /**
     * Send password reset email with reset link.
     *
     * @param to recipient email address
     * @param name recipient name
     * @param resetToken the password reset token
     */
    void sendPasswordResetEmail(String to, String name, String resetToken);

    /**
     * Send email verification email with verification link.
     *
     * @param to recipient email address
     * @param name recipient name
     * @param verificationToken the email verification token
     */
    void sendEmailVerificationEmail(String to, String name, String verificationToken);

    /**
     * Send welcome email after successful email verification.
     *
     * @param to recipient email address
     * @param name recipient name
     */
    void sendWelcomeEmail(String to, String name);

    /**
     * Send password changed confirmation email.
     *
     * @param to recipient email address
     * @param name recipient name
     */
    void sendPasswordChangedEmail(String to, String name);

    // ==================== APPOINTMENT EMAILS ====================

    /**
     * Send appointment confirmation email to patient.
     *
     * @param to patient email
     * @param patientName patient name
     * @param doctorName doctor name
     * @param doctorSpecialization doctor's specialization
     * @param appointmentDateTime appointment date and time
     * @param reason appointment reason
     */
    void sendAppointmentConfirmationEmail(String to, String patientName, String doctorName,
                                          String doctorSpecialization, java.time.LocalDateTime appointmentDateTime,
                                          String reason);

    /**
     * Send appointment reminder email (24h before).
     *
     * @param to patient email
     * @param patientName patient name
     * @param doctorName doctor name
     * @param doctorSpecialization doctor's specialization
     * @param appointmentDateTime appointment date and time
     */
    void sendAppointmentReminderEmail(String to, String patientName, String doctorName,
                                      String doctorSpecialization, java.time.LocalDateTime appointmentDateTime);

    /**
     * Send appointment cancelled email.
     *
     * @param to patient email
     * @param patientName patient name
     * @param doctorName doctor name
     * @param appointmentDateTime original appointment date and time
     * @param cancellationReason reason for cancellation
     */
    void sendAppointmentCancelledEmail(String to, String patientName, String doctorName,
                                       java.time.LocalDateTime appointmentDateTime, String cancellationReason);

    /**
     * Send appointment rescheduled email.
     *
     * @param to patient email
     * @param patientName patient name
     * @param doctorName doctor name
     * @param oldDateTime original appointment date and time
     * @param newDateTime new appointment date and time
     */
    void sendAppointmentRescheduledEmail(String to, String patientName, String doctorName,
                                         java.time.LocalDateTime oldDateTime, java.time.LocalDateTime newDateTime);

    /**
     * Send notification to doctor about new appointment.
     *
     * @param to doctor email
     * @param doctorName doctor name
     * @param patientName patient name
     * @param appointmentDateTime appointment date and time
     * @param reason appointment reason
     */
    void sendDoctorNewAppointmentEmail(String to, String doctorName, String patientName,
                                       java.time.LocalDateTime appointmentDateTime, String reason);
}
