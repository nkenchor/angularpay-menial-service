package io.angularpay.menial.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.menial.adapters.outbound.RedisAdapter;
import io.angularpay.menial.domain.MenialRequest;

import java.util.Objects;

public interface TTLPublisherCommand<T extends MenialRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToTTLMessage(MenialRequest menialRequest, T t) throws JsonProcessingException;

    default void publishTTL(T t) {
        MenialRequest menialRequest = t.getMenialRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(menialRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToTTLMessage(menialRequest, t);
                redisAdapter.publishTTL(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
