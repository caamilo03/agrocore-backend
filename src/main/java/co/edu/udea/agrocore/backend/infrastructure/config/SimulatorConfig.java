package co.edu.udea.agrocore.backend.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.util.Random;

/**
 * Configuracion del simulador de telemetria.
 *
 * Solo se carga cuando agrocore.simulator.enabled=true (default true en
 * application.yml). Activa el scheduler de Spring y expone Clock/Random
 * como beans para que el servicio sea facilmente testeable con valores
 * deterministicos.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(
        prefix = "agrocore.simulator",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SimulatorConfig {

    @Bean
    public Clock simulatorClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public Random simulatorRandom() {
        return new Random();
    }
}
