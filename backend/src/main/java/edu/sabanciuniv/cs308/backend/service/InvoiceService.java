package edu.sabanciuniv.cs308.backend.service;

import edu.sabanciuniv.cs308.backend.dto.InvoiceDTO;
import edu.sabanciuniv.cs308.backend.entity.OrderEntity;
import edu.sabanciuniv.cs308.backend.entity.OrderItem;
import edu.sabanciuniv.cs308.backend.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class InvoiceService {

    // OrderEntity + User bilgisi -> InvoiceDTO
    public InvoiceDTO buildInvoiceFromOrder(String externalOrderId,
                                            UserEntity user,
                                            OrderEntity order) {

        InvoiceDTO dto = new InvoiceDTO();

        dto.setOrderId(externalOrderId != null ? externalOrderId : order.getId());
        dto.setUserEmail(user.getEmailAddress());
        dto.setUserName(user.getName());

        List<InvoiceDTO.LineItem> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                InvoiceDTO.LineItem li = new InvoiceDTO.LineItem();
                li.setProductId(item.getProductId());
                li.setProductName(item.getName());
                li.setSku(item.getSku());
                li.setQuantity(item.getQuantity());
                li.setUnitPrice(item.getUnitPrice());
                li.setLineTotal(item.getLineTotal());
                items.add(li);
            }
        }
        dto.setItems(items);

        BigDecimal subtotal = items.stream()
                .map(InvoiceDTO.LineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setSubtotal(subtotal);
        dto.setShipping(BigDecimal.ZERO);
        dto.setGrandTotal(subtotal);

        return dto;
    }

    // Şimdilik mail gönderimini simüle ediyoruz (PDF vs yok)
    public void sendInvoiceEmail(InvoiceDTO invoice) {
        System.out.println("==== INVOICE EMAIL SIMULATION ====");
        System.out.println("To      : " + invoice.getUserEmail());
        System.out.println("Subject : Invoice " + invoice.getOrderId());
        System.out.println("User    : " + invoice.getUserName());
        System.out.println("Subtotal: " + invoice.getSubtotal());
        System.out.println("Shipping: " + invoice.getShipping());
        System.out.println("Total   : " + invoice.getGrandTotal());
        System.out.println("Items:");
        if (invoice.getItems() != null) {
            for (InvoiceDTO.LineItem li : invoice.getItems()) {
                System.out.println(" - " + li.getProductName()
                        + " (" + li.getSku() + ") x" + li.getQuantity()
                        + " -> " + li.getLineTotal());
            }
        }
        System.out.println("==================================");
    }
}
