package com.roq.ita.controller;

import com.roq.ita.model.JwtResponse;
import com.roq.ita.model.Login;
import com.roq.ita.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    JwtUtil jwtTokenUtil;

    @PostMapping("authenticate")
    public ResponseEntity<?> createToken(@RequestBody Login request) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        }
        catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }


//    public Mono<Bank> bankMono () {
//        return bankService.banks();
//    }
//
//    public Mono<?> bankMo () {
//        return bankService.bankDataFlux();
//    }
//
//    public Mono<?> account (@RequestBody AccountValidationRequest request){
//        return bankService.accountValidationResponseMono(request);
//    }
//
//    public Mono<?> account (@RequestBody TransferRequest request){
//        FlutterwaveTransferRequest transferRequest = new FlutterwaveTransferRequest();
//
//        transferRequest.setAccount_bank(request.getBeneficiaryBankCode());
//        transferRequest.setAccount_number(request.getBeneficiaryAccountNumber());
//        transferRequest.setAmount(Integer.parseInt(request.getAmount()));
//        transferRequest.setReference(request.getTransactionReference());
//        transferRequest.setNarration(request.getNarration());
//        transferRequest.setCallback_url(request.getCallbackUrl());
//        transferRequest.setCurrency(request.getCurrencyCode());
//        return bankService.transferAccount(transferRequest);
//    }
//
//    public Mono<?> accountTest (@RequestBody TransferRequest request){
//        Mono<TransferResponse> transfer = bankService.transfer(request);
//        return transfer;
//    }




}
