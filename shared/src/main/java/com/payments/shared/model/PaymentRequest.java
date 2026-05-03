package com.payments.shared.model;

import com.payments.shared.validation.ValidCurrency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PaymentRequest {
    @NotBlank
    private String instructionIdentification;

    @NotBlank
    private String endToEndIdentification;

    @NotNull
    private Amount instructedAmount;

    @NotNull
    private AccountReference debtorAccount;

    @NotNull
    private AccountReference creditorAccount;

    private String remittanceInformation;
    private String consentId;
    private String beneficiaryId;

    public PaymentRequest() {
    }

    public String getInstructionIdentification() {
        return instructionIdentification;
    }

    public void setInstructionIdentification(String instructionIdentification) {
        this.instructionIdentification = instructionIdentification;
    }

    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

    public void setEndToEndIdentification(String endToEndIdentification) {
        this.endToEndIdentification = endToEndIdentification;
    }

    public Amount getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(Amount instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public AccountReference getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(AccountReference debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public AccountReference getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(AccountReference creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getRemittanceInformation() {
        return remittanceInformation;
    }

    public void setRemittanceInformation(String remittanceInformation) {
        this.remittanceInformation = remittanceInformation;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(String beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public static class Amount {
        @NotBlank
        private String amount;

        @NotBlank
        @ValidCurrency
        private String currency;

        public Amount() {
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    public static class AccountReference {
        @NotBlank
        private String schemeName;

        @NotBlank
        private String identification;

        @NotBlank
        private String name;

        private String secondaryIdentification;

        public AccountReference() {
        }

        public String getSchemeName() {
            return schemeName;
        }

        public void setSchemeName(String schemeName) {
            this.schemeName = schemeName;
        }

        public String getIdentification() {
            return identification;
        }

        public void setIdentification(String identification) {
            this.identification = identification;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSecondaryIdentification() {
            return secondaryIdentification;
        }

        public void setSecondaryIdentification(String secondaryIdentification) {
            this.secondaryIdentification = secondaryIdentification;
        }
    }
}
