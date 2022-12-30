package com.roq.ita.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;



@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {

    @Id
    private String transactionReference;

    private String amount;
    private String currencyCode;
    private String beneficiaryAccountNumber;
    private String beneficiaryAccountName;
    private String beneficiaryBankCode;
    private LocalDateTime transactionDateTime;
    private String responseMessage;
    private String responseCode;
    private String sessionId;
    private String status;
}
