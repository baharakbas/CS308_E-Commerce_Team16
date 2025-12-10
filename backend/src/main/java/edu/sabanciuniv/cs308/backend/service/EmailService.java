package edu.sabanciuniv.cs308.backend.service;

public interface EmailService {

    /**
     * Sends an invoice email to the given recipient. Implementations may no-op when
     * a mail server is not configured but should never throw to the caller.
     *
     * @param to        recipient email
     * @param subject   subject line
     * @param body      plain text body
     * @param pdfBytes  invoice PDF bytes
     * @param filename  attachment file name
     */
    void sendInvoiceEmail(String to, String subject, String body, byte[] pdfBytes, String filename);
}

