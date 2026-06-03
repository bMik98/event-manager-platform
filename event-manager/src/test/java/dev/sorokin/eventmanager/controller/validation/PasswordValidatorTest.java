package dev.sorokin.eventmanager.controller.validation;

import dev.sorokin.eventmanager.config.PasswordPolicyProperties;
import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidatorTest {

    private static final PasswordPolicyProperties STRICT = new PasswordPolicyProperties(
            8, 255, true, true, true, true);
    private static final PasswordPolicyProperties RELAXED = new PasswordPolicyProperties(
            1, 255, false, false, false, false);

    static Stream<Arguments> nullBlankPasswords() {
        return Stream.of(
                Arguments.of("null password is rejected", STRICT, null, false),
                Arguments.of("empty password is rejected", STRICT, "", false),
                Arguments.of("blank password is rejected", STRICT, "   ", false)
        );
    }

    static Stream<Arguments> validPasswords() {
        return Stream.of(
                Arguments.of("password matching all requirements is accepted", STRICT, "Secure1!", true),
                Arguments.of("password accepted when all rules disabled", RELAXED, "alllowercase", true)
        );
    }

    static Stream<Arguments> minLengthPasswords() {
        PasswordPolicyProperties minLen8 = new PasswordPolicyProperties(8, 255, false, false, false, false);
        return Stream.of(
                Arguments.of("password shorter than min length is rejected", minLen8, "short", false),
                Arguments.of("password equal to min length is accepted", minLen8, "exactly8", true)
        );
    }

    static Stream<Arguments> maxLengthPasswords() {
        PasswordPolicyProperties maxLen5 = new PasswordPolicyProperties(1, 5, false, false, false, false);
        return Stream.of(
                Arguments.of("password longer than max length is rejected", maxLen5, "toolongpassword", false),
                Arguments.of("password equal to max length is accepted", maxLen5, "exact", true)
        );
    }

    static Stream<Arguments> uppercasePasswords() {
        PasswordPolicyProperties requireUppercase = new PasswordPolicyProperties(1, 255, true, false, false, false);
        return Stream.of(
                Arguments.of("password without uppercase is rejected when required", requireUppercase, "nouppercase1!", false),
                Arguments.of("password without uppercase is accepted when not required", RELAXED, "nouppercase", true)
        );
    }

    static Stream<Arguments> lowercasePasswords() {
        PasswordPolicyProperties requireLowercase = new PasswordPolicyProperties(1, 255, false, true, false, false);
        return Stream.of(
                Arguments.of("password without lowercase is rejected when required", requireLowercase, "NOLOWERCASE1!", false),
                Arguments.of("password without lowercase is accepted when not required", RELAXED, "NOLOWERCASE", true)
        );
    }

    static Stream<Arguments> digitPasswords() {
        PasswordPolicyProperties requireDigit = new PasswordPolicyProperties(1, 255, false, false, true, false);
        return Stream.of(
                Arguments.of("password without digit is rejected when required", requireDigit, "NoDigitHere!", false),
                Arguments.of("password without digit is accepted when not required", RELAXED, "NoDigitHere", true)
        );
    }

    static Stream<Arguments> specialCharPasswords() {
        PasswordPolicyProperties requireSpecialChar = new PasswordPolicyProperties(1, 255, false, false, false, true);
        return Stream.of(
                Arguments.of("password without special char is rejected when required", requireSpecialChar, "NoSpecial1", false),
                Arguments.of("password with ! is accepted", requireSpecialChar, "Pass1!", true),
                Arguments.of("password with @ is accepted", requireSpecialChar, "Pass1@", true),
                Arguments.of("password with # is accepted", requireSpecialChar, "Pass1#", true),
                Arguments.of("password with $ is accepted", requireSpecialChar, "Pass1$", true),
                Arguments.of("password with _ is accepted", requireSpecialChar, "Pass1_", true),
                Arguments.of("password without special char is accepted when not required", RELAXED, "NoSpecialChar1", true)
        );
    }

    static Stream<Arguments> multipleViolationsPasswords() {
        return Stream.of(
                Arguments.of("too short, no uppercase, no digit, no special char", STRICT, "abc", false),
                Arguments.of("no uppercase, no digit, no special char", STRICT, "alllower", false),
                Arguments.of("no digit, no special char", STRICT, "AllLower", false)
        );
    }

    static Stream<Arguments> passwordValidationTestCases() {
        return Stream.of(
                nullBlankPasswords(),
                validPasswords(),
                minLengthPasswords(),
                maxLengthPasswords(),
                uppercasePasswords(),
                lowercasePasswords(),
                digitPasswords(),
                specialCharPasswords(),
                multipleViolationsPasswords()
        ).flatMap(Function.identity());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("passwordValidationTestCases")
    void passwordValidation(String testName, PasswordPolicyProperties policy, String password, boolean expected) {
        ConstraintValidatorContext context = new StubConstraintValidatorContext();
        PasswordValidator validator = new PasswordValidator(policy);
        assertThat(validator.isValid(password, context)).isEqualTo(expected);
    }

    // --- stubs ---

    private static class StubConstraintValidatorContext
            implements ConstraintValidatorContext, ConstraintValidatorContext.ConstraintViolationBuilder {

        @Override
        public void disableDefaultConstraintViolation() {
            // do nothing
        }

        @Override
        public String getDefaultConstraintMessageTemplate() {
            return "";
        }

        @Override
        public ClockProvider getClockProvider() {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
            return this;
        }

        @Override
        public ConstraintValidatorContext addConstraintViolation() {
            return this;
        }

        @Override
        public LeafNodeBuilderCustomizableContext addBeanNode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public NodeBuilderDefinedContext addNode(String name) {
            return null;
        }

        @Override
        public NodeBuilderCustomizableContext addPropertyNode(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public NodeBuilderDefinedContext addParameterNode(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ContainerElementNodeBuilderCustomizableContext addContainerElementNode(
                String name, Class<?> containerType, Integer typeArgumentIndex) {
            throw new UnsupportedOperationException();
        }
    }
}
