package com.roq.ita.model.flutter;

public record DataResponse(
        long id,
        String account_number,
        String bank_code,
        String full_name,
        String created_at,
        String currency,
        String debit_currency,
        int amount,
        double fee,
        String status,
        String reference,
        String narration,
        String bank_name
) {
}
