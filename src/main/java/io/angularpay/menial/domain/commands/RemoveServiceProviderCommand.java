package io.angularpay.menial.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.menial.adapters.outbound.MongoAdapter;
import io.angularpay.menial.adapters.outbound.RedisAdapter;
import io.angularpay.menial.domain.DeletedBy;
import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.Role;
import io.angularpay.menial.domain.ServiceProvider;
import io.angularpay.menial.exceptions.ErrorObject;
import io.angularpay.menial.helpers.CommandHelper;
import io.angularpay.menial.models.*;
import io.angularpay.menial.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.menial.domain.DeletedBy.INVESTOR;
import static io.angularpay.menial.domain.DeletedBy.TTL_SERVICE;
import static io.angularpay.menial.helpers.CommandHelper.*;
import static io.angularpay.menial.helpers.Helper.getAllParties;
import static io.angularpay.menial.helpers.Helper.getAllPartiesExceptActor;
import static io.angularpay.menial.models.UserNotificationType.INVESTOR_DELETED_BY_SELF;
import static io.angularpay.menial.models.UserNotificationType.INVESTOR_DELETED_BY_TTL;

@Service
public class RemoveServiceProviderCommand extends AbstractCommand<RemoveServiceProviderCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public RemoveServiceProviderCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter) {
        super("RemoveServiceProviderCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(RemoveServiceProviderCommandRequest request) {
        switch (request.getDeletedBy()) {
            case PLATFORM:
            case TTL_SERVICE:
                return request.getAuthenticatedUser().getUserReference();
            default:
                return this.commandHelper.getInvestmentOwner(request.getRequestReference(), request.getInvestmentReference());
        }
    }

    @Override
    protected GenericCommandResponse handle(RemoveServiceProviderCommandRequest request) {
        MenialRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String investmentReference = request.getInvestmentReference();
        validRequestStatusAndInvestmentExists(found, investmentReference);
        Supplier<GenericCommandResponse> supplier = () -> removeInvestor(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse removeInvestor(RemoveServiceProviderCommandRequest request) {
        MenialRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        found.getServiceProviders().forEach(x-> {
            if (request.getInvestmentReference().equalsIgnoreCase(x.getReference())) {
                validateInvestmentStatusOrThrow(x);
                x.setDeleted(true);
                x.setDeletedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
                x.setDeletedBy(request.getDeletedBy());
            }
        });
        MenialRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .menialRequest(response)
                .itemReference(request.getInvestmentReference())
                .build();
    }

    @Override
    protected List<ErrorObject> validate(RemoveServiceProviderCommandRequest request) {
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
    public UserNotificationType getUserNotificationType(GenericCommandResponse commandResponse) {
        DeletedBy deletedBy = commandResponse.getMenialRequest().getServiceProviders().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(commandResponse.getItemReference()))
                .findFirst()
                .map(ServiceProvider::getDeletedBy)
                .orElse(TTL_SERVICE);
        return deletedBy == INVESTOR ? INVESTOR_DELETED_BY_SELF : INVESTOR_DELETED_BY_TTL;
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return this.getUserNotificationType(commandResponse) == INVESTOR_DELETED_BY_SELF ?
                getAllPartiesExceptActor(commandResponse.getMenialRequest(), commandResponse.getItemReference()) :
                getAllParties(commandResponse.getMenialRequest());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, MenialRequest> parameters) throws JsonProcessingException {
        String summary;
        Optional<String> optional = parameters.getCommandResponse().getMenialRequest().getServiceProviders().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(parameters.getCommandResponse().getItemReference()))
                .map(ServiceProvider::getUserReference)
                .findFirst();
        if (optional.isPresent() && optional.get().equalsIgnoreCase(parameters.getUserReference())) {
            summary = "the comment you made on a Service Request post, was deleted";
        } else {
            summary = "someone's comment on a Service Request post that you commented on, was deleted";
        }

        UserNotificationInvestmentPayload userNotificationInvestmentPayload = UserNotificationInvestmentPayload.builder()
                .requestReference(parameters.getCommandResponse().getRequestReference())
                .investmentReference(parameters.getCommandResponse().getItemReference())
                .build();
        String payload = mapper.writeValueAsString(userNotificationInvestmentPayload);

        String attributes = mapper.writeValueAsString(parameters.getRequest());

        UserNotification userNotification = UserNotification.builder()
                .reference(UUID.randomUUID().toString())
                .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .serviceCode(parameters.getRequest().getServiceCode())
                .userReference(parameters.getUserReference())
                .type(parameters.getType())
                .summary(summary)
                .payload(payload)
                .attributes(attributes)
                .build();

        return mapper.writeValueAsString(userNotification);
    }
}
