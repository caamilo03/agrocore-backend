package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface HarvestCropBatchUseCase {

    /**
     * Marca un lote como COSECHADO registrando el peso final y la fecha de
     * finalizacion del cultivo.
     *
     * @param id        id del lote a cosechar
     * @param yieldKg   peso obtenido (> 0)
     * @param endDate   instante de finalizacion (nullable; si es null se usa
     *                  {@code Instant.now()})
     * @return el lote actualizado
     * @throws java.util.NoSuchElementException si el lote no existe
     * @throws co.edu.udea.agrocore.backend.application.exception.InvalidBatchStateException
     *         si el lote no esta en estado ACTIVO (ya cosechado o perdido)
     * @throws IllegalArgumentException si {@code yieldKg} es null o &lt;= 0
     */
    CropBatch harvest(UUID id, BigDecimal yieldKg, Instant endDate);
}
