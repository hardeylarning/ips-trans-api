package com.roq.ita.model.paystack;

import lombok.Data;

@Data
public class PaystackTransferRequest {
    private String source;
    private String amount;
    private String reference;
    private String recipient;
    private String reason;
}
