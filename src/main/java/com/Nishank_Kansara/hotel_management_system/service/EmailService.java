package com.Nishank_Kansara.hotel_management_system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    // 🔐 OTP Password Reset Email
    public void sendSimpleMessage(String to, String subject, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #4f46e5;">🔐 JHotel - Password Reset</h2>
                    <p>Hi there,</p>
                    <p>We received a request to reset your password. Use the OTP below to continue:</p>
                    <div style="margin: 20px 0; padding: 10px; background-color: #f3f4f6; border-left: 4px solid #4f46e5;">
                        <h3 style="margin: 0; font-size: 24px; color: #111827;">%s</h3>
                    </div>
                    <p>If you didn't request this, please ignore this email.</p>
                    <p>Thanks,<br/>JHotel Team</p>
                </div>
                """.formatted(otp);

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    // ✅ Booking Confirmation Email
    public void sendBookingConfirmationEmail(String to, String checkIn, String checkOut, String roomType, double totalAmount,String ConfirmationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            String subject = "✅ Booking Confirmed - JHotel";
            String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #10b981;">✅ Booking Confirmation</h2>
                    <p>Dear Guest,</p>
                    <p>Thank you for choosing JHotel! Your booking has been successfully confirmed with the following details:</p>
                    <ul style="margin: 20px 0; padding: 0 0 0 20px;">
                        <li><strong>Room Type:</strong> %s</li>
                        <li><strong>Check-In Date:</strong> %s</li>
                        <li><strong>Check-Out Date:</strong> %s</li>
                        <li><strong>Total Amount Paid:</strong> ₹%.2f</li>
                    </ul>
                    <p>We look forward to hosting you. For any queries, feel free to contact us.</p>
                    <p>Warm regards,<br/>JHotel Team</p>
                </div>
                """.formatted(roomType, checkIn, checkOut, totalAmount);

            helper.setFrom(sender);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send booking confirmation email: " + e.getMessage(), e);
        }
    }
}
