package edu.sabanciuniv.cs308.backend.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CheckoutPrefillDTO {
    private String userId;
    private String name;
    private String emailAddress;
    private String phoneNumber;
    private List<AddressDTO> addresses = new ArrayList<>();
    private List<PaymentMethodDTO> paymentMethods = new ArrayList<>();

    @Data
    public static class AddressDTO {
        private String id;
        private String label;
        private String fullName;
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String country;
        private String zipCode;
        private boolean isDefault;
        private String phoneNumber;
    }

    @Data
    public static class PaymentMethodDTO {
        private String id;
        private String brand;
        private String last4;
        private Integer expMonth;
        private Integer expYear;
        private String holderName;
        private boolean isDefault;
        private String nickname;
    }
}

