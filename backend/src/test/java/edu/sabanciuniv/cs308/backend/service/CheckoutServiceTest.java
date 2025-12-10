package edu.sabanciuniv.cs308.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.sabanciuniv.cs308.backend.dto.CheckoutPrefillDTO;
import edu.sabanciuniv.cs308.backend.dto.OrderDetailDTO;
import edu.sabanciuniv.cs308.backend.entity.OrderEntity;
import edu.sabanciuniv.cs308.backend.entity.OrderItem;
import edu.sabanciuniv.cs308.backend.entity.PaymentMethod;
import edu.sabanciuniv.cs308.backend.entity.ProductEntity;
import edu.sabanciuniv.cs308.backend.entity.UserEntity;
import edu.sabanciuniv.cs308.backend.enums.OrderStatus;
import edu.sabanciuniv.cs308.backend.repository.OrderRepository;
import edu.sabanciuniv.cs308.backend.repository.ProductRepository;
import edu.sabanciuniv.cs308.backend.repository.UserRepository;
import edu.sabanciuniv.cs308.backend.request.CheckoutRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InvoiceService invoiceService;

    @InjectMocks
    private CheckoutService checkoutService;

    private UserEntity user;
    private OrderEntity cart;

    @BeforeEach
    void setup() {
        user = new UserEntity();
        user.setId("user-1");
        user.setEmailAddress("user@example.com");
        user.setName("Test User");

        OrderItem item = new OrderItem();
        item.setProductId("prod-1");
        item.setSku("SKU-1");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.TEN);
        item.setLineTotal(BigDecimal.TEN);

        cart = new OrderEntity();
        cart.setId("cart-1");
        cart.setStatus(OrderStatus.CART.name());
        cart.setItems(new ArrayList<>(List.of(item)));
    }

    @Test
    void checkoutTriggersInvoiceGeneration() {
        when(userRepository.findByEmailAddress("user@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById("cart-1")).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductEntity product = new ProductEntity();
        ProductEntity.Variant variant = new ProductEntity.Variant();
        variant.setSku("SKU-1");
        variant.setStock(5);
        product.setVariants(List.of(variant));
        when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckoutRequest request = new CheckoutRequest();
        request.setCartId("cart-1");
        request.setShippingFullName("Test User");
        request.setShippingLine1("Line 1");
        request.setShippingCity("City");
        request.setShippingState("State");
        request.setShippingCountry("Country");
        request.setShippingZipCode("12345");
        request.setShippingPhoneNumber("5551234");
        request.setUseShippingAsBilling(true);
        request.setCardBrand("VISA");
        request.setCardLast4("1234");
        request.setCardExpMonth(12);
        request.setCardExpYear(2030);

        OrderDetailDTO result = checkoutService.checkout("user@example.com", request);

        assertThat(result).isNotNull();
        verify(invoiceService, times(1)).generateAndSendInvoice(any(OrderEntity.class));
    }

    @Test
    void getCheckoutPrefillReturnsSavedData() {
        UserEntity.Address address = new UserEntity.Address();
        address.setId("addr-1");
        address.setLabel("Home");
        address.setFullName("Test User");
        address.setLine1("Line 1");
        address.setCity("City");
        address.setState("State");
        address.setCountry("Country");
        address.setZipCode("12345");
        address.setDefault(true);
        address.setPhoneNumber("5551234");
        user.getAddresses().add(address);

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId("pm-1");
        paymentMethod.setBrand("VISA");
        paymentMethod.setLast4("1234");
        paymentMethod.setExpMonth(12);
        paymentMethod.setExpYear(2030);
        paymentMethod.setHolderName("Test User");
        paymentMethod.setDefault(true);
        paymentMethod.setNickname("Primary");
        user.getPaymentMethods().add(paymentMethod);

        when(userRepository.findByEmailAddress("user@example.com")).thenReturn(Optional.of(user));

        CheckoutPrefillDTO dto = checkoutService.getCheckoutPrefill("user@example.com");

        assertThat(dto.getUserId()).isEqualTo("user-1");
        assertThat(dto.getAddresses()).hasSize(1);
        assertThat(dto.getPaymentMethods()).hasSize(1);
        assertThat(dto.getAddresses().get(0).getLabel()).isEqualTo("Home");
        assertThat(dto.getPaymentMethods().get(0).getLast4()).isEqualTo("1234");
    }
}

