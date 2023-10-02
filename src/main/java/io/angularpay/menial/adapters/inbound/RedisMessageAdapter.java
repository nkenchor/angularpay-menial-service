package io.angularpay.menial.adapters.inbound;

import io.angularpay.menial.ports.inbound.InboundMessagingPort;
import io.angularpay.menial.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.menial.models.platform.PlatformConfigurationIdentifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.angularpay.menial.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}
