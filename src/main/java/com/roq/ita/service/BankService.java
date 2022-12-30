package com.roq.ita.service;

import com.roq.ita.exception.CustomException;
import com.roq.ita.exception.InternalServerException;
import com.roq.ita.model.*;
import com.roq.ita.model.flutter.*;
import com.roq.ita.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

record Message(String status, String message) {}

@Service
@Slf4j
public class BankService {
   private static final String BANKS_URL = "banks/NG";
   public static final String ACCT_VAL_URL = "accounts/resolve";

   public static final String TRANSFER_URL = "transfers";

   @Autowired
   private WebClient webClient;

   @Autowired
   TransactionRepository transactionRepository;

   public Mono<?> banks() {
       return webClient.get()
               .uri(BANKS_URL)
               .retrieve()
               .onStatus(HttpStatus::is4xxClientError, this::handle4xxErrorResponse)
               .onStatus(HttpStatus::is5xxServerError, this::handle5xxErrorResponse)
               .bodyToMono(Bank.class);
   }

   public Mono<AccountValidationResponse> accountValidationResponseMono(AccountValidationRequest request) {
       FlutterAccountValidationRequest validationRequest = new FlutterAccountValidationRequest();
       validationRequest.setAccount_bank(request.getCode());
       validationRequest.setAccount_number(request.getAccountNumber());

      return webClient.post().uri(ACCT_VAL_URL)
              .bodyValue(validationRequest)
              .retrieve()
              .onStatus(HttpStatus::is4xxClientError, this::handle4xxErrorResponse)
              .onStatus(HttpStatus::is5xxServerError, this::handle5xxErrorResponse)
              .bodyToMono(FlutterAccountValidationResponse.class)
              .flatMap(rs -> {
                  AccountValidationResponse response = new AccountValidationResponse();
                  FlutterAccountValidationRequest data = rs.getData();
                  response.setAccountNumber(data.getAccount_number());
                  response.setBankCode(request.getCode());
                  response.setBankName("Access Bank");
                  return Mono.just(response);
              });
   }

    public Mono<Transaction> transfer(TransferRequest request) {
        FlutterwaveTransferRequest transferRequest = new FlutterwaveTransferRequest();
        transferRequest.setAccount_bank(request.getBeneficiaryBankCode());
        transferRequest.setAccount_number(request.getBeneficiaryAccountNumber());
        transferRequest.setAmount(Integer.parseInt(request.getAmount()));
        transferRequest.setReference(request.getTransactionReference());
        transferRequest.setNarration(request.getNarration());
        transferRequest.setCallback_url(request.getCallbackUrl());
        transferRequest.setCurrency(request.getCurrencyCode());

         Transaction response = new Transaction();
        return webClient.post().uri(TRANSFER_URL)
                .bodyValue(transferRequest)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, this::handle4xxErrorResponse)
                .onStatus(HttpStatus::is5xxServerError, this::handle5xxErrorResponse)
                .bodyToMono(FlutterwaveTransferResponse.class)
                .flatMap(ts -> {
                    DataResponse data = ts.getData();
                    response.setAmount(String.valueOf(data.amount()));
                    response.setBeneficiaryAccountName(data.full_name());
                    response.setBeneficiaryAccountNumber(data.account_number());
                    response.setBeneficiaryBankCode(data.bank_code());
                    response.setTransactionReference(data.reference());
                    response.setTransactionDateTime(LocalDateTime.now());
                    response.setCurrencyCode(data.currency());
                    response.setSessionId(data.reference() + "||" + data.id());
                    response.setStatus(data.status().toUpperCase());

                    checkStatus(response, data);
                    Transaction transaction = transactionRepository.save(response);
                    return Mono.just(transaction);
                })
                .retryWhen(Retry.backoff(request.getMaxRetryAttempt(), Duration.ofSeconds(5)));

    }

    public Mono<Transaction> transactionMono(String reference) {
        Optional<Transaction> transactionOptional = getTransaction(reference);
        if (transactionOptional.isEmpty()) {
            return Mono.error(new CustomException("Transaction not found"));
        }
        Transaction transaction = transactionOptional.get();
        String sessionId = transaction.getSessionId();
        String  id = sessionId.substring(sessionId.lastIndexOf("|")+1);
        log.info("ID:=> from database: {}", id);
        return webClient.get()
                .uri(TRANSFER_URL+"/"+id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, this::handle4xxErrorResponse)
                .onStatus(HttpStatus::is5xxServerError, this::handle5xxErrorResponse)
                .onStatus(HttpStatus::isError, this::handle5xxErrorResponse)
                .bodyToMono(FlutterwaveTransferResponse.class)
                .flatMap(rs -> {
                    DataResponse data = rs.getData();
                    if (data.status().equalsIgnoreCase(transaction.getStatus())){
                        return Mono.just(transaction);
                    }
                    transaction.setStatus(data.status());
                    checkStatus(transaction, data);
                    transactionRepository.save(transaction);
                    return Mono.just(transaction);
                });
    }

    private void checkStatus(Transaction transaction, DataResponse data) {
        if (data.status().equalsIgnoreCase("NEW")){
            transaction.setStatus("CREATED");
            transaction.setResponseCode("01");
            transaction.setResponseMessage("Transaction has initiated");
        }

        else if (data.status().equalsIgnoreCase("pending")){
            transaction.setResponseCode("02");
            transaction.setResponseMessage("Transaction is currently being processed");
        }

        else if (data.status().equalsIgnoreCase("Failed")){
            transaction.setResponseCode("99");
            transaction.setResponseMessage("Transaction not successful");
        }

        else if (data.status().equalsIgnoreCase("Success")){
            transaction.setResponseCode("00");
            transaction.setResponseMessage("Transaction is successful");
        }

        else {
            transaction.setStatus("RETRY");
            transaction.setResponseCode("03");
            transaction.setResponseMessage("Retry Transaction");
        }
    }

    public Mono<CustomException> handle4xxErrorResponse(ClientResponse clientResponse) {

      Mono<Message> errorResponse = clientResponse.bodyToMono(Message.class);
      return errorResponse.flatMap((message) -> {
//         ErrorMessage errorMessage = new ErrorMessage(clientResponse.statusCode(), message.getDescription());
         log.error("ErrorResponse Code is 4XX " + clientResponse.rawStatusCode() + " and the exception message is : " + message);
         return Mono.error(new CustomException(message.message()));
      });
   }

   public Mono<InternalServerException> handle5xxErrorResponse(ClientResponse clientResponse) {
      Mono<Message> errorResponse = clientResponse.bodyToMono(Message.class);
      return errorResponse.flatMap((message) -> {
//         ErrorMessage errorMessage = new ErrorMessage(clientResponse.statusCode(), message.getDescription());
         log.error("ErrorResponse Code is " + clientResponse.rawStatusCode() + " and the exception message is : " + message);
         return Mono.error(new InternalServerException(message.message()));
      });
   }

   public Optional<Transaction> getTransaction(String reference) {
       return transactionRepository.findById(reference);
   }


}
