package edu.sabanciuniv.cs308.backend.controller;

import edu.sabanciuniv.cs308.backend.dto.InvoiceDTO;
import edu.sabanciuniv.cs308.backend.service.InvoiceService;
import edu.sabanciuniv.cs308.backend.dto.OrderDetailDTO;
import edu.sabanciuniv.cs308.backend.entity.OrderEntity;
import edu.sabanciuniv.cs308.backend.entity.UserEntity;
import edu.sabanciuniv.cs308.backend.repository.OrderRepository;
import edu.sabanciuniv.cs308.backend.repository.UserRepository;
import edu.sabanciuniv.cs308.backend.service.OrderMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;


    public OrderController(OrderRepository orderRepository,
                           UserRepository userRepository,
                           InvoiceService invoiceService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.invoiceService = invoiceService;
    }

    // GET /api/orders?me=true&page=0&size=10
    @GetMapping
    public ResponseEntity<?> list(Authentication auth,
                                  @RequestParam(required = false, defaultValue = "false") boolean me,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        if (!me) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only me=true is supported for now"));
        }

        String email = auth.getName();
        UserEntity user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<OrderEntity> p = orderRepository.findByUserId(user.getId(), PageRequest.of(page, size));

        return ResponseEntity.ok(Map.of(
                "page", p.getNumber(),
                "size", p.getSize(),
                "totalElements", p.getTotalElements(),
                "content", p.getContent().stream().map(OrderMapper::toSummary).toList()
        ));
    }

    // GET /api/orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<?> detail(Authentication auth, @PathVariable String orderId) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        String email = auth.getName();
        UserEntity user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Forbidden"));
        }

        OrderDetailDTO dto = OrderMapper.toDetail(order);
        return ResponseEntity.ok(dto);
    }

        // POST /api/orders/checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(Authentication auth,
                                      @RequestBody Map<String, Object> body) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        String email = auth.getName();
        UserEntity user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Frontend’in beklediği temp order id
        String generatedOrderId = "TEMP-" + System.currentTimeMillis();

        // 🔥 1) Body içinden cartId al
        Object cartIdObj = body.get("cartId");
        if (cartIdObj instanceof String cartId && !cartId.isBlank()) {
            // Bu cart aslında OrderEntity (status=CART) olarak tutuluyor.
            orderRepository.findById(cartId).ifPresent(cartOrder -> {
                // 🔥 2) Cart’tan invoice datasını üret
                InvoiceDTO invoice = invoiceService.buildInvoiceFromOrder(
                        generatedOrderId,
                        user,
                        cartOrder
                );
                // 🔥 3) “Mail gönderimi”ni tetikle (şimdilik simülasyon)
                invoiceService.sendInvoiceEmail(invoice);
            });
        }

        return ResponseEntity.ok(Map.of(
                "orderId", generatedOrderId,
                "userId", user.getId(),
                "message", "Checkout successfully"
        ));
    }

}
