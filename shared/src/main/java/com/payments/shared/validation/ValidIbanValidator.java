package com.payments.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidIbanValidator implements ConstraintValidator<ValidIban, String> {
    private static final String IBAN_CHARACTER_PATTERN = "^[A-Z]{2}[0-9A-Z]{13,31}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.replaceAll("\\s+", "").toUpperCase();
        if (!normalized.matches(IBAN_CHARACTER_PATTERN)) {
            return false;
        }

        return isValidChecksum(normalized);
    }

    private boolean isValidChecksum(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        StringBuilder numeric = new StringBuilder();

        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numeric.append(c);
            } else {
                numeric.append(Character.getNumericValue(c));
            }
        }

        String checkString = numeric.toString();
        int mod = 0;
        for (int i = 0; i < checkString.length(); i++) {
            char digit = checkString.charAt(i);
            mod = (mod * 10 + (digit - '0')) % 97;
        }

        return mod == 1;
    }
}
