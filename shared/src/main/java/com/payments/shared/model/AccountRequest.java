package com.payments.shared.model;

import com.payments.shared.validation.ValidCurrency;
import jakarta.validation.constraints.NotBlank;

public class AccountRequest {
    @NotBlank
    private String accountNumber;

    @NotBlank
    private String routingNumber;

    @NotBlank
    @ValidCurrency
    private String currency;

    public AccountRequest() {
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
