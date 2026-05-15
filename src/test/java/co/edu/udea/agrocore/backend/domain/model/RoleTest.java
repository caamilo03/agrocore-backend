package co.edu.udea.agrocore.backend.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleTest {

    @Test
    void fromString_parsesAllCanonicalValues() {
        assertThat(Role.fromString("ADMIN")).isEqualTo(Role.ADMIN);
        assertThat(Role.fromString("OPERADOR")).isEqualTo(Role.OPERADOR);
        assertThat(Role.fromString("OBSERVADOR")).isEqualTo(Role.OBSERVADOR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"admin", "Admin", "aDmIn", " ADMIN ", "\tADMIN\n"})
    void fromString_isCaseInsensitiveAndTrimsWhitespace(String input) {
        assertThat(Role.fromString(input)).isEqualTo(Role.ADMIN);
    }

    @Test
    void fromString_throwsForNullOrBlank() {
        assertThatThrownBy(() -> Role.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN, OPERADOR, OBSERVADOR");
        assertThatThrownBy(() -> Role.fromString(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Role.fromString("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromString_throwsForUnknownValue() {
        assertThatThrownBy(() -> Role.fromString("SUPERUSER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPERUSER")
                .hasMessageContaining("ADMIN, OPERADOR, OBSERVADOR");
    }

    @Test
    void enumNameMatchesPersistedValue() {
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
        assertThat(Role.OPERADOR.name()).isEqualTo("OPERADOR");
        assertThat(Role.OBSERVADOR.name()).isEqualTo("OBSERVADOR");
    }
}
