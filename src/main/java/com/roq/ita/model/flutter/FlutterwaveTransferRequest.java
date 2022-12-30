package com.roq.ita.model.flutter;

import lombok.Data;

@Data
public class FlutterwaveTransferRequest {
    private int amount;
    private String account_bank;
    private String account_number;
    private String narration;
    private String currency;
    private String reference;
    private String callback_url;

}
