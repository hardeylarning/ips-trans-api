package com.roq.ita.controller;

import com.roq.ita.exception.ErrorMessage;
import com.roq.ita.model.AccountValidationRequest;
import com.roq.ita.model.TransferRequest;
import com.roq.ita.service.FlutterwaveService;
import com.roq.ita.service.PaystackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/core-banking/")
@Slf4j
public class TaskController {
    @Autowired
    FlutterwaveService flutterwaveService;

    @Autowired
    PaystackService paystackService;


    @GetMapping(value = "banks")
    public Mono<?> getBanks (@RequestParam(required = false, defaultValue = "FLUTTER_WAVE") String Provider) {
        log.info("Provider:=> {}", Provider);
        if (Provider.equalsIgnoreCase("paystack")) {
            return paystackService.psBanks();
        }
        return flutterwaveService.banks();
    }

    @GetMapping(value = "transaction/{reference}")
    public Mono<?> getTransaction (@PathVariable String reference) {
        return flutterwaveService.transactionMono(reference);
    }

    @PostMapping(value = "validateBankAccount")
    public Mono<?> validateAccount (@RequestBody AccountValidationRequest request,
                                    @RequestParam(required = false, defaultValue = "FLUTTER_WAVE") String Provider){
        if (Provider.equalsIgnoreCase("paystack")) {
            return paystackService.verifyAccount(request);
        }
        return flutterwaveService.accountValidationResponseMono(request);
    }

    @PostMapping(value = "transfer")
    public Mono<?> transfer (@RequestBody TransferRequest request,
                             @RequestParam(required = false, defaultValue = "FLUTTER_WAVE") String Provider){
        if (flutterwaveService.getTransaction(request.getTransactionReference()).isPresent()){
            return Mono.just(new ErrorMessage("Already Exist", "Transaction is already exist"));
        }

        if (Provider.equalsIgnoreCase("paystack")) {
            return paystackService.psTransfer(request);
        }
        return flutterwaveService.transfer(request);
    }

    @PostMapping(value = "recipient")
    public ResponseEntity<?> test (@RequestBody TransferRequest request){
        return ResponseEntity.ok(paystackService.recipientGenerator(request));
    }

}
