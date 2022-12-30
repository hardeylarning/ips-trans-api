package com.roq.ita.model.paystack;

import lombok.Data;

@Data
public class PaystackTransferResponse {
    private String status;
    private String message;
    private TransferData data;
}
