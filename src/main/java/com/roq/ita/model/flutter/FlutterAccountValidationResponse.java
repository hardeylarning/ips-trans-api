package com.roq.ita.model.flutter;

import lombok.Data;

@Data
public class FlutterAccountValidationResponse {
    private String status;
    private String message;
    private FlutterAccountValidationRequest data;
}
