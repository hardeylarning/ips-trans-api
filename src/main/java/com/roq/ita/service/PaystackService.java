package com.roq.ita.service;

import com.roq.ita.exception.BadRequestException;
import com.roq.ita.exception.InternalServerException;
import com.roq.ita.model.*;
import com.roq.ita.model.flutter.*;
import com.roq.ita.model.paystack.*;
import com.roq.ita.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class PaystackService {
   private static final String BANKS_URL = "bank?currency=NGN";
   private static final String ACCT_VAL_URL = "bank/resolve";

   private static final String TRANSFER_URL = "transfer";

    private static final String RECIPIENT_URL = "transferrecipient";

   @Autowired
   @Qualifier("paystackClient")
   private WebClient webClient;

   @Autowired
   TransactionRepository transactionRepository;

   public Mono<?> psBanks() {
       return webClient.get()
               .uri(BANKS_URL)
               .retrieve()
               .onStatus(HttpStatus::is4xxClientError, this::handle4xxErrorResponse)
               .onStatus(HttpStatus::is5xxServerError, this::handle5xxErrorResponse)
               .bodyToMono(Bank.class)
               .flatMap(rs -> {
                   List<BankResponse> bankResponses = new ArrayList<>();
                   List<BankData> data = rs.getData();
                   data.forEach(bankData -> {
                       BankResponse response = new BankResponse();
                       response.setBankName(bankData.name());
                       response.setCode(bankData.code());
                       response.setLongCode(bankData.longcode());
                       bankResponses.add(response);
                   });
                   return Mono.just(bankResponses);
               });
   }

   public Mono<AccountValidationResponse> verifyAccount(AccountValidationRequest request) {
       MultiValueMap<String, String> queryMap = new LinkedMultiValueMap<>();
       queryMap.add("account_number", request.getAccountNumber());
       queryMap.add("bank_code", request.getCode());

      return webClient.get().uri(builder -> builder.path(ACCT_VAL_URL).queryParams(queryMap).build())
              .retrieve()
              .onStatus(HttpStatus::is4xxClientError, this::handle4xxErrorResponse)
              .onStatus(HttpStatus::is5xxServerError, this::handle5xxErrorResponse)
              .bodyToMono(FlutterAccountValidationResponse.class)
              .flatMap(rs -> {
                  AccountValidationResponse response = new AccountValidationResponse();
                  FlutterAccountValidationRequest data = rs.getData();
                  response.setAccountNumber(data.getAccount_number());
                  response.setBankCode(request.getCode());
                  response.setAccountName(data.getAccount_name());
                  response.setBankName("");
                  return Mono.just(response);
              });
   }

   public String recipientGenerator(TransferRequest request) {
       TransferRecipient recipient = new TransferRecipient();
       recipient.setType("nuban");
       recipient.setName(request.getBeneficiaryAccountName());
       recipient.setBank_code(request.getBeneficiaryBankCode());
       recipient.setCurrency(recipient.getCurrency());
       recipient.setAccount_number(request.getBeneficiaryAccountNumber());

       TransferRecipientResponse block = webClient.post().uri(RECIPIENT_URL)
               .bodyValue(recipient)
               .retrieve()
               .onStatus(HttpStatus::is4xxClientError, this::handle4xxErrorResponse)
               .onStatus(HttpStatus::is5xxServerError, this::handle5xxErrorResponse)
               .bodyToMono(TransferRecipientResponse.class)
               .block();

       return block.getData().recipient_code() == null ? "" : block.getData().recipient_code();
   }

    public Mono<Transaction> psTransfer(TransferRequest request) {
       String recipient = recipientGenerator(request);
       if (recipient.equals("")){
           return Mono.error(new BadRequestException("Recipient not yet generated"));
       }
       PaystackTransferRequest transferRequest = new PaystackTransferRequest();
       transferRequest.setRecipient(recipient);
       transferRequest.setReference(request.getTransactionReference());
       transferRequest.setSource("balance");
       transferRequest.setReason(request.getNarration());
       transferRequest.setAmount(request.getAmount());

         Transaction response = new Transaction();
        return webClient.post().uri(TRANSFER_URL)
                .bodyValue(transferRequest)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, this::handle4xxErrorResponse)
                .onStatus(HttpStatus::is5xxServerError, this::handle5xxErrorResponse)
                .bodyToMono(PaystackTransferResponse.class)
                .flatMap(ts -> {
                    TransferData data = ts.getData();
                    response.setAmount(String.valueOf(data.amount()));
                    response.setBeneficiaryAccountName(request.getBeneficiaryAccountName());
                    response.setBeneficiaryAccountNumber(request.getBeneficiaryAccountNumber());
                    response.setBeneficiaryBankCode(request.getBeneficiaryBankCode());
                    response.setTransactionReference(data.reference());
                    response.setTransactionDateTime(LocalDateTime.now());
                    response.setCurrencyCode(data.currency());
                    response.setSessionId(data.reference() + "||" + data.id());
                    response.setStatus(data.status().toUpperCase());

                    checkStatus(response, data);
                    Transaction transaction = transactionRepository.save(response);
                    return Mono.just(transaction);
                });

    }

    private void checkStatus(Transaction transaction, TransferData data) {
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

    public Mono<BadRequestException> handle4xxErrorResponse(ClientResponse clientResponse) {

      Mono<Message> errorResponse = clientResponse.bodyToMono(Message.class);
      return errorResponse.flatMap((message) -> {
//         ErrorMessage errorMessage = new ErrorMessage(clientResponse.statusCode(), message.getDescription());
         log.error("ErrorResponse Code is 4XX " + clientResponse.rawStatusCode() + " and the exception message is : " + message);
         return Mono.error(new BadRequestException(message.message()));
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
