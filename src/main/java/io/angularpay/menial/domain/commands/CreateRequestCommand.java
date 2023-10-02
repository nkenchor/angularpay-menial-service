package io.angularpay.menial.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.menial.adapters.outbound.MongoAdapter;
import io.angularpay.menial.adapters.outbound.RedisAdapter;
import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.Role;
import io.angularpay.menial.domain.ServiceClient;
import io.angularpay.menial.exceptions.ErrorObject;
import io.angularpay.menial.helpers.CommandHelper;
import io.angularpay.menial.models.CreateRequestCommandRequest;
import io.angularpay.menial.models.GenericCommandResponse;
import io.angularpay.menial.models.GenericReferenceResponse;
import io.angularpay.menial.models.ResourceReferenceResponse;
import io.angularpay.menial.validation.DefaultConstraintValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static io.angularpay.menial.helpers.ObjectFactory.pmtRequestWithDefaults;

@Slf4j
@Service
public class CreateRequestCommand extends AbstractCommand<CreateRequestCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public CreateRequestCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("CreateRequestCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(CreateRequestCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected GenericCommandResponse handle(CreateRequestCommandRequest request) {
        MenialRequest menialRequestWithDefaults = pmtRequestWithDefaults();
        MenialRequest withOtherDetails = menialRequestWithDefaults.toBuilder()
                .amount(request.getCreateRequest().getAmount())
                .summary(request.getCreateRequest().getSummary())
                .serviceClient(ServiceClient.builder()
                        .userReference(request.getAuthenticatedUser().getUserReference())
                        .build())
                .build();
        MenialRequest response = this.mongoAdapter.createRequest(withOtherDetails);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .menialRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(CreateRequestCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public String convertToUpdatesMessage(MenialRequest menialRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(menialRequest);
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }

    @Override
    public ResourceReferenceResponse map(GenericCommandResponse genericCommandResponse) {
        return new ResourceReferenceResponse(genericCommandResponse.getRequestReference());
    }
}
