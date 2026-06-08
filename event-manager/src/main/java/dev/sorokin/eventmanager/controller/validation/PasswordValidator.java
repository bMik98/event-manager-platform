package dev.sorokin.eventmanager.controller.validation;

import dev.sorokin.eventmanager.config.PasswordPolicyProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Set<Character> SPECIAL_CHARS = Set.of(
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')',
            '_', '+', '-', '=', '[', ']', '{', '}', '|', ';',
            '\'', ':', '"', ',', '.', '/', '<', '>', '?'
    );

    private final PasswordPolicyProperties policy;

    private static void check(List<String> violations, boolean condition, String message) {
        if (condition) {
            violations.add(message);
        }
    }

    private static boolean noneMatch(String password, Predicate<Character> predicate) {
        return password.chars().noneMatch(c -> predicate.test((char) c));
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false;
        }
        List<String> violations = collectViolations(password);
        if (!violations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            violations.forEach(
                    msg -> context.buildConstraintViolationWithTemplate(msg).addConstraintViolation()
            );
            return false;
        }
        return true;
    }

    private List<String> collectViolations(String password) {
        List<String> violations = new ArrayList<>();
        check(violations, password.length() < policy.minLength(),
                "Password must be at least " + policy.minLength() + " characters long");
        check(violations, password.length() > policy.maxLength(),
                "Password must be at most " + policy.maxLength() + " characters long");
        check(violations, policy.requireUppercase() && noneMatch(password, Character::isUpperCase),
                "Password must contain at least one uppercase letter");
        check(violations, policy.requireLowercase() && noneMatch(password, Character::isLowerCase),
                "Password must contain at least one lowercase letter");
        check(violations, policy.requireDigit() && noneMatch(password, Character::isDigit),
                "Password must contain at least one digit");
        check(violations, policy.requireSpecialChar() && noneMatch(password, SPECIAL_CHARS::contains),
                "Password must contain at least one special character");
        return violations;
    }
}
