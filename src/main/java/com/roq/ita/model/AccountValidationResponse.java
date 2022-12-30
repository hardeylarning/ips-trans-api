package com.roq.ita.model;

import lombok.Data;

@Data
public class AccountValidationResponse {
    private String accountNumber;
    private String accountName;
    private String bankCode;
    private String bankName;
}
