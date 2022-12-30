package com.roq.ita.controller;

import com.roq.ita.exception.ErrorMessage;
import com.roq.ita.model.AccountValidationRequest;
import com.roq.ita.model.Transaction;
import com.roq.ita.model.TransferRequest;
import com.roq.ita.repository.TransactionRepository;
import com.roq.ita.service.BankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/v1/core-banking/")
@Slf4j
public class TaskController {
    @Autowired
    BankService bankService;


    @GetMapping(value = "banks")
    public Mono<?> getBanks (@RequestParam(required = false, defaultValue = "FLUTTER_WAVE") String Provider) {
        log.info("Provider:=> {}", Provider);
        return bankService.banks();
    }

    @GetMapping(value = "transaction/{reference}")
    public Mono<?> getTransaction (@PathVariable String reference) {
        return bankService.transactionMono(reference);
    }

    @PostMapping(value = "validateBankAccount")
    public Mono<?> validateAccount (@RequestBody AccountValidationRequest request){
        return bankService.accountValidationResponseMono(request);
    }

    @PostMapping(value = "transfer")
    public Mono<?> transfer (@RequestBody TransferRequest request){
        if (bankService.getTransaction(request.getTransactionReference()).isPresent()){
            return Mono.just(new ErrorMessage("Already Exist", "Transaction is already exist"));
        }
        return bankService.transfer(request);
    }

}
