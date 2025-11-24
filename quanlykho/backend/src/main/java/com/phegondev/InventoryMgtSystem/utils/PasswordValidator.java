package com.phegondev.InventoryMgtSystem.utils;

import com.phegondev.InventoryMgtSystem.exceptions.ValidationException;

import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=[\\]{};':\"\\\\|,.<>/?]).{8,}$");

    private PasswordValidator() {
    }

    public static void validate(String password) {
        if (password == null || !STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new ValidationException("Mat khau moi phai tu 8 ky tu, co chu hoa, so va ky tu dac biet");
        }
    }
}
