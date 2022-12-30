package com.roq.ita.model;

import lombok.Data;

import java.util.List;


@Data
public class Bank {
    private String status;
    private String message;
    private List<BankData> data;
}
