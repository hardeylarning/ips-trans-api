package com.roq.ita.model.flutter;

import lombok.Data;

@Data
public class FlutterwaveTransferResponse {
    private String status;
    private String message;
    private DataResponse data;
}
