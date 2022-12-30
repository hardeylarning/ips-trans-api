package com.roq.ita.model;

import lombok.Data;

import java.util.Date;

@Data
public class TransferResponse {
    private String amount;
    private String currencyCode;
    private String beneficiaryAccountNumber;
    private String beneficiaryAccountName;
    private String beneficiaryBankCode;
    private String transactionReference;
    private Date transactionDateTime;
    private String responseMessage;
    private String responseCode;
    private String sessionId;
    private String status;
}
