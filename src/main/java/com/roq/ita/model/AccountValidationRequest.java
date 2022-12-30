package com.roq.ita.model;

import lombok.Data;

@Data
public class AccountValidationRequest {
    private String accountNumber;
    private String code;
}
