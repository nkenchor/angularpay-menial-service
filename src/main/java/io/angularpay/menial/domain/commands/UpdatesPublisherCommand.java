package io.angularpay.menial.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.menial.adapters.outbound.RedisAdapter;
import io.angularpay.menial.domain.MenialRequest;

import java.util.Objects;

public interface UpdatesPublisherCommand<T extends MenialRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToUpdatesMessage(MenialRequest menialRequest) throws JsonProcessingException;

    default void publishUpdates(T t) {
        MenialRequest menialRequest = t.getMenialRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(menialRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToUpdatesMessage(menialRequest);
                redisAdapter.publishUpdates(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
