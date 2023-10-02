package io.angularpay.menial.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.menial.adapters.outbound.RedisAdapter;
import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.models.UserNotificationBuilderParameters;
import io.angularpay.menial.models.UserNotificationType;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public interface UserNotificationsPublisherCommand<T extends MenialRequestSupplier> {

    RedisAdapter getRedisAdapter();
    UserNotificationType getUserNotificationType(T commandResponse);
    List<String> getAudience(T commandResponse);
    String convertToUserNotificationsMessage(UserNotificationBuilderParameters<T, MenialRequest> parameters) throws JsonProcessingException;

    default void publishUserNotification(T commandResponse) {
        MenialRequest request = commandResponse.getMenialRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        UserNotificationType type = this.getUserNotificationType(commandResponse);
        List<String> audience = this.getAudience(commandResponse);

        if (Objects.nonNull(request) && Objects.nonNull(redisAdapter)
        && Objects.nonNull(type) && !CollectionUtils.isEmpty(audience)) {
            audience.stream().parallel().forEach(userReference-> {
                try {
                    UserNotificationBuilderParameters<T, MenialRequest> parameters = UserNotificationBuilderParameters.<T, MenialRequest>builder()
                            .userReference(userReference)
                            .request(request)
                            .commandResponse(commandResponse)
                            .type(type)
                            .build();
                    String message = this.convertToUserNotificationsMessage(parameters);
                    redisAdapter.publishUserNotification(message);
                } catch (JsonProcessingException exception) {
                    throw new RuntimeException(exception);
                }
            });
        }
    }
}
