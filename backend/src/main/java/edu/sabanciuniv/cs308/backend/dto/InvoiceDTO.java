package edu.sabanciuniv.cs308.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceDTO {

    private String orderId;
    private String userEmail;
    private String userName;

    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal grandTotal;

    private List<LineItem> items;

    @Data
    public static class LineItem {
        private String productId;
        private String productName;
        private String sku;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
