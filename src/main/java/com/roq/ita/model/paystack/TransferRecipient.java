package com.roq.ita.model.paystack;

import lombok.Data;

@Data
public class TransferRecipient {
    private String type;
    private String name;
    private String account_number;
    private String bank_code;
    private String currency;

}
