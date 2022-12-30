package com.roq.ita.model;

import lombok.Data;

@Data
public class TransferRequest {
    private String amount;
    private String currencyCode;
    private String narration;
    private String beneficiaryAccountNumber;
    private String beneficiaryAccountName;
    private String beneficiaryBankCode;
    private String transactionReference;
    private int maxRetryAttempt;
    private String callbackUrl;
}
