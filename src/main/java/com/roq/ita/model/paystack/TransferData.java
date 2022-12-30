package com.roq.ita.model.paystack;

public record TransferData(long id,
                           String reference,
                           String recipient,
                           String amount,
                           String source,
                           String reason,
                           String status,
                           String transfer_code,
                           String currency,
                           String integration) {
}
