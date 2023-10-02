package io.angularpay.menial.ports.outbound;

import io.angularpay.menial.models.SchedulerServiceRequest;
import io.angularpay.menial.models.SchedulerServiceResponse;

import java.util.Map;
import java.util.Optional;

public interface SchedulerServicePort {
    Optional<SchedulerServiceResponse> createScheduledRequest(SchedulerServiceRequest request, Map<String, String> headers);
}
