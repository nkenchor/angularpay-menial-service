package io.angularpay.menial.ports.inbound;

import io.angularpay.menial.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}
