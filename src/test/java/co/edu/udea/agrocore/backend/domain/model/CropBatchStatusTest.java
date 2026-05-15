package co.edu.udea.agrocore.backend.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CropBatchStatusTest {

    @Test
    void fromString_parsesCanonicalUpperCase() {
        assertThat(CropBatchStatus.fromString("ACTIVO")).isEqualTo(CropBatchStatus.ACTIVO);
        assertThat(CropBatchStatus.fromString("COSECHADO")).isEqualTo(CropBatchStatus.COSECHADO);
        assertThat(CropBatchStatus.fromString("PERDIDO")).isEqualTo(CropBatchStatus.PERDIDO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"activo", "Activo", "aCTivo", " ACTIVO ", "\tACTIVO\n"})
    void fromString_isCaseInsensitiveAndTrimsWhitespace(String input) {
        assertThat(CropBatchStatus.fromString(input)).isEqualTo(CropBatchStatus.ACTIVO);
    }

    @Test
    void fromString_throwsForNullOrBlank() {
        assertThatThrownBy(() -> CropBatchStatus.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ACTIVO, COSECHADO, PERDIDO");
        assertThatThrownBy(() -> CropBatchStatus.fromString(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CropBatchStatus.fromString("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fromString_throwsForUnknownValue() {
        assertThatThrownBy(() -> CropBatchStatus.fromString("INACTIVE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INACTIVE")
                .hasMessageContaining("ACTIVO, COSECHADO, PERDIDO");
    }

    @Test
    void enumNameMatchesPersistedValue() {
        // Garantia de compatibilidad con la BD: las filas existentes dicen
        // "ACTIVO" y Hibernate con @Enumerated(STRING) hace Enum.valueOf,
        // que coincide con name() exactamente.
        assertThat(CropBatchStatus.ACTIVO.name()).isEqualTo("ACTIVO");
        assertThat(CropBatchStatus.COSECHADO.name()).isEqualTo("COSECHADO");
        assertThat(CropBatchStatus.PERDIDO.name()).isEqualTo("PERDIDO");
    }
}
