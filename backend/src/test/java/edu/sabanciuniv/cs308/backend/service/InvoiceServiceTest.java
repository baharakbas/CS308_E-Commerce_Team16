package edu.sabanciuniv.cs308.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.sabanciuniv.cs308.backend.entity.AddressSnapshot;
import edu.sabanciuniv.cs308.backend.entity.Money;
import edu.sabanciuniv.cs308.backend.entity.OrderEntity;
import edu.sabanciuniv.cs308.backend.entity.OrderItem;
import edu.sabanciuniv.cs308.backend.entity.UserEntity;
import edu.sabanciuniv.cs308.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InvoiceService invoiceService;

    private OrderEntity order;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId("user-1");
        user.setName("Test User");
        user.setEmailAddress("user@example.com");

        AddressSnapshot shipping = new AddressSnapshot();
        shipping.setFullName("Test User");
        shipping.setLine1("Line 1");
        shipping.setCity("Istanbul");
        shipping.setState("TR");
        shipping.setCountry("Turkey");
        shipping.setZipCode("34000");

        Money totals = new Money();
        totals.setSubtotal(BigDecimal.valueOf(100));
        totals.setTax(BigDecimal.valueOf(20));
        totals.setShipping(BigDecimal.ZERO);
        totals.setGrandTotal(BigDecimal.valueOf(120));

        OrderItem item = new OrderItem();
        item.setName("Dress");
        item.setSku("SKU-1");
        item.setQuantity(2);
        item.setLineTotal(BigDecimal.valueOf(100));

        order = new OrderEntity();
        order.setId("order-1");
        order.setUserId("user-1");
        order.setItems(List.of(item));
        order.setTotals(totals);
        order.setShippingAddressSnapshot(shipping);
        order.setBillingAddressSnapshot(shipping);
        order.setPaymentMethodRef("VISA ****1234");
        order.setCreatedAt(Instant.parse("2025-01-01T10:15:30Z"));
    }

    @Test
    void generateAndSendInvoiceCreatesPdfAndDelegatesToEmailService() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        invoiceService.generateAndSendInvoice(order);

        ArgumentCaptor<byte[]> pdfCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(emailService).sendInvoiceEmail(
                eq("user@example.com"),
                contains("order-1"),
                contains("TIDL"),
                pdfCaptor.capture(),
                eq("invoice-order-1.pdf")
        );

        byte[] pdfBytes = pdfCaptor.getValue();
        assertThat(pdfBytes).isNotNull().isNotEmpty();
    }

    @Test
    void throwsWhenUserMissing() {
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.generateAndSendInvoice(order))
                .isInstanceOf(IllegalStateException.class);

        verify(emailService, never()).sendInvoiceEmail(any(), any(), any(), any(), any());
    }
}

