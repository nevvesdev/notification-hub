package br.com.nevvesdev.notificationhub.domain.valueobject;

import br.com.nevvesdev.notificationhub.domain.exception.InvalidRecipientException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Recipient Value Object")
class RecipientTest {

    @Test
    @DisplayName("should create valid recipient from well-formed email")
    void shouldCreateValidRecipient() {
        Recipient recipient = Recipient.of("joao@nevvesdev.com.br");

        assertThat(recipient.value()).isEqualTo("joao@nevvesdev.com.br");
    }

    @Test
    @DisplayName("should normalize email to lowercase")
    void shouldNormalizeToLowercase() {
        Recipient recipient = Recipient.of("JOAO@NEVVESDEV.COM.BR");

        assertThat(recipient.value()).isEqualTo("joao@nevvesdev.com.br");
    }

    @Test
    @DisplayName("should strip whitespace from email")
    void shouldStripWhitespace() {
        Recipient recipient = Recipient.of("  joao@nevvesdev.com.br  ");

        assertThat(recipient.value()).isEqualTo("joao@nevvesdev.com.br");
    }

    @ParameterizedTest
    @DisplayName("should reject invalid email formats")
    @ValueSource(strings = {"not-an-email", "missing@", "@nodomain", "", "  "})
    void shouldRejectInvalidEmails(String invalid) {
        assertThatThrownBy(() -> Recipient.of(invalid))
                .isInstanceOf(InvalidRecipientException.class);
    }

    @Test
    @DisplayName("should reject null email")
    void shouldRejectNull() {
        assertThatThrownBy(() -> new Recipient(null))
                .isInstanceOf(InvalidRecipientException.class);
    }
}