package edu.sabanciuniv.cs308.backend.service;

import edu.sabanciuniv.cs308.backend.dto.InvoiceDTO;
import edu.sabanciuniv.cs308.backend.entity.AddressSnapshot;
import edu.sabanciuniv.cs308.backend.entity.Money;
import edu.sabanciuniv.cs308.backend.entity.OrderEntity;
import edu.sabanciuniv.cs308.backend.entity.OrderItem;
import edu.sabanciuniv.cs308.backend.entity.UserEntity;
import edu.sabanciuniv.cs308.backend.repository.UserRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final UserRepository userRepository;
    private final EmailService emailService;

    public void generateAndSendInvoice(OrderEntity order) {
        UserEntity user = resolveUserOrNull(order);
        if (user == null) {
            log.warn("Order {} has no associated user. Skipping invoice email.", order != null ? order.getId() : "N/A");
            return;
        }

        byte[] pdfBytes = createInvoicePdf(order, user);
        String subject = "Your TIDL invoice #" + order.getId();
        String greetingName = Optional.ofNullable(user.getName()).orElse(user.getEmailAddress());
        String body = """
                Hi %s,

                Thank you for shopping with TIDL. Your invoice for order #%s is attached as a PDF.
                Keep this email for your records.

                - TIDL Support
                """.formatted(greetingName, order.getId());

        emailService.sendInvoiceEmail(
                user.getEmailAddress(),
                subject,
                body,
                pdfBytes,
                "invoice-" + order.getId() + ".pdf"
        );
    }

    /**
     * Builds the invoice PDF bytes for the given order and user. Throws IllegalArgumentException
     * if the order or user is missing.
     */
    public byte[] generateInvoicePdf(OrderEntity order, UserEntity user) {
        if (order == null || user == null) {
            throw new IllegalArgumentException("Order and user must be provided for invoice generation");
        }
        return createInvoicePdf(order, user);
    }

    public byte[] generateInvoicePdf(OrderEntity order) {
        UserEntity user = resolveUser(order);
        return createInvoicePdf(order, user);
    }

    public byte[] buildInvoiceFromOrder(String orderId, UserEntity user, OrderEntity order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null");
        }
        UserEntity resolvedUser = user != null ? user : resolveUserOrNull(order);
        if (resolvedUser == null) {
            throw new IllegalStateException("User not found for invoice generation");
        }
        log.debug("Building invoice PDF for order {}", orderId != null ? orderId : order.getId());
        return createInvoicePdf(order, resolvedUser);
    }

    public void sendInvoiceEmail(InvoiceDTO invoiceDTO) {
        if (invoiceDTO == null) {
            return;
        }
        byte[] pdfBytes = invoiceDTO.getPdfBytes();
        String recipient = invoiceDTO.getRecipientEmail();
        if (pdfBytes == null || pdfBytes.length == 0 || recipient == null || recipient.isBlank()) {
            log.warn("Invoice email skipped due to missing recipient or PDF.");
            return;
        }
        String subject = Optional.ofNullable(invoiceDTO.getSubject()).orElse("Your TIDL invoice");
        String body = Optional.ofNullable(invoiceDTO.getBody()).orElse("Invoice attached.");
        String filename = Optional.ofNullable(invoiceDTO.getFilename()).orElse("invoice.pdf");

        emailService.sendInvoiceEmail(recipient, subject, body, pdfBytes, filename);
    }

    private byte[] createInvoicePdf(OrderEntity order, UserEntity user) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setLeading(15f);
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                content.newLineAtOffset(50, page.getMediaBox().getHeight() - 50);
                content.showText("TIDL Invoice");

                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLine();
                writeLine(content, "Order ID: " + safe(order.getId()));
                writeLine(content, "Order Date: " + formatInstant(order.getCreatedAt()));
                writeLine(content, "Customer: " + safe(user.getName()) + " <" + safe(user.getEmailAddress()) + ">");
                writeLine(content, "");

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                writeLine(content, "Shipping Address");
                content.setFont(PDType1Font.HELVETICA, 12);
                writeAddress(content, order.getShippingAddressSnapshot());
                writeLine(content, "");

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                writeLine(content, "Billing Address");
                content.setFont(PDType1Font.HELVETICA, 12);
                writeAddress(content, order.getBillingAddressSnapshot());
                writeLine(content, "");

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                writeLine(content, "Items");
                content.setFont(PDType1Font.HELVETICA, 12);
                List<OrderItem> items = Optional.ofNullable(order.getItems()).orElse(List.of());
                if (items.isEmpty()) {
                    writeLine(content, "- No items recorded");
                } else {
                    for (OrderItem item : items) {
                        String itemLine = "- %s x%d (%s) = %s"
                                .formatted(
                                        safe(item.getName()),
                                        item.getQuantity(),
                                        safe(item.getSku()),
                                        formatMoney(item.getLineTotal())
                                );
                        writeLine(content, itemLine);
                    }
                }
                writeLine(content, "");

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                writeLine(content, "Totals");
                content.setFont(PDType1Font.HELVETICA, 12);
                Money totals = order.getTotals();
                writeLine(content, "Subtotal: " + formatMoney(totals != null ? totals.getSubtotal() : null));
                writeLine(content, "Tax: " + formatMoney(totals != null ? totals.getTax() : null));
                writeLine(content, "Shipping: " + formatMoney(totals != null ? totals.getShipping() : null));
                writeLine(content, "Grand Total: " + formatMoney(totals != null ? totals.getGrandTotal() : null));

                content.newLine();
                writeLine(content, "Payment Method: " + safe(order.getPaymentMethodRef()));
                content.endText();
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                document.save(baos);
                return baos.toByteArray();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate invoice PDF", ex);
        }
    }

    private void writeAddress(PDPageContentStream content, AddressSnapshot address) throws IOException {
        if (address == null) {
            writeLine(content, "- Not provided");
            return;
        }
        writeLine(content, safe(address.getFullName()));
        writeLine(content, safe(address.getLine1()));
        if (address.getLine2() != null && !address.getLine2().isBlank()) {
            writeLine(content, safe(address.getLine2()));
        }
        writeLine(content, safe(address.getCity()) + ", " + safe(address.getState()));
        writeLine(content, safe(address.getCountry()) + " " + safe(address.getZipCode()));
        if (address.getPhoneNumber() != null) {
            writeLine(content, "Phone: " + safe(address.getPhoneNumber()));
        }
    }

    private void writeLine(PDPageContentStream content, String text) throws IOException {
        content.showText(safe(text));
        content.newLine();
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\r\\n]+", " ").trim();
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DATE_FORMATTER.format(instant);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString() + " TRY";
    }

    private UserEntity resolveUser(OrderEntity order) {
        if (order == null || order.getUserId() == null) {
            throw new IllegalArgumentException("Order or order.userId is null");
        }
        return userRepository.findById(order.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for invoice generation"));
    }

    private UserEntity resolveUserOrNull(OrderEntity order) {
        if (order == null || order.getUserId() == null) {
            return null;
        }
        return userRepository.findById(order.getUserId()).orElse(null);
    }
}

