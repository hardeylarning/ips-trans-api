package com.roq.ita.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankResponse {
    private String code;
    private String longCode;
    private String bankName;
}
