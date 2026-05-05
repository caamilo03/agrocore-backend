package co.edu.udea.agrocore.backend.infrastructure.config;

import co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging.TelemetryRedisStreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;

/**
 * Configura el StreamMessageListenerContainer que escucha el stream de
 * telemetria, garantiza que el consumer group exista y delega cada record
 * al TelemetryRedisStreamConsumer.
 *
 * El stream se crea automaticamente al primer XADD del productor; el grupo
 * lo creamos al arrancar (idempotente).
 */
@Configuration
public class RedisStreamConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamConfig.class);

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamListenerContainer(
            RedisConnectionFactory connectionFactory,
            AgrocoreRedisProperties properties
    ) {
        var options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions.builder()
                .pollTimeout(Duration.ofMillis(properties.streams().pollTimeoutMs()))
                .build();
        return StreamMessageListenerContainer.create(connectionFactory, options);
    }

    @Bean
    public SmartLifecycle telemetryStreamSubscription(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> container,
            StringRedisTemplate redisTemplate,
            TelemetryRedisStreamConsumer consumer,
            AgrocoreRedisProperties properties
    ) {
        return new SmartLifecycle() {
            private volatile boolean running = false;
            private Subscription subscription;

            @Override
            public void start() {
                String stream = properties.streams().telemetry();
                String group = properties.streams().consumerGroup();
                String name = properties.streams().consumerName();

                ensureGroup(redisTemplate, stream, group);

                subscription = container.receive(
                        Consumer.from(group, name),
                        StreamOffset.create(stream, ReadOffset.lastConsumed()),
                        consumer
                );
                container.start();
                running = true;
                log.info("Suscripcion a stream Redis '{}' iniciada (group={}, consumer={})",
                        stream, group, name);
            }

            @Override
            public void stop() {
                if (subscription != null) {
                    subscription.cancel();
                }
                container.stop();
                running = false;
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public int getPhase() {
                return Integer.MAX_VALUE - 100;
            }
        };
    }

    private void ensureGroup(StringRedisTemplate redis, String stream, String group) {
        try {
            redis.opsForStream().createGroup(stream, ReadOffset.from("0"), group);
            log.info("Consumer group '{}' creado sobre stream '{}'", group, stream);
        } catch (Exception e) {
            // BUSYGROUP: el grupo ya existe -> idempotente, ignorar.
            log.debug("Consumer group '{}' ya existia (o stream no resoluble aun): {}",
                    group, e.getMessage());
        }
    }
}
