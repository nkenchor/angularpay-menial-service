package io.angularpay.menial.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.menial.adapters.outbound.MongoAdapter;
import io.angularpay.menial.adapters.outbound.RedisAdapter;
import io.angularpay.menial.configurations.AngularPayConfiguration;
import io.angularpay.menial.domain.*;
import io.angularpay.menial.exceptions.ErrorObject;
import io.angularpay.menial.helpers.CommandHelper;
import io.angularpay.menial.models.*;
import io.angularpay.menial.validation.DefaultConstraintValidator;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.menial.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.menial.helpers.CommandHelper.validRequestStatusOrThrow;
import static io.angularpay.menial.helpers.Helper.getAllPartiesExceptActor;
import static io.angularpay.menial.models.UserNotificationType.PEER_INVESTOR_ADDED;
import static io.angularpay.menial.models.UserNotificationType.SOLO_INVESTOR_ADDED;

@Service
public class AddServiceProviderCommand extends AbstractCommand<AddServiceProviderCommandRequest, GenericReferenceResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        ResourceReferenceCommand<GenericCommandResponse, ResourceReferenceResponse>,
        TTLPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    private final AngularPayConfiguration configuration;

    public AddServiceProviderCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter, AngularPayConfiguration configuration) {
        super("AddServiceProviderCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
        this.configuration = configuration;
    }

    @Override
    protected String getResourceOwner(AddServiceProviderCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected GenericCommandResponse handle(AddServiceProviderCommandRequest request) {
        MenialRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        Supplier<GenericCommandResponse> supplier = () -> addInvestor(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse addInvestor(AddServiceProviderCommandRequest request) throws OptimisticLockingFailureException {
        MenialRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());

        ServiceProvider serviceProvider = ServiceProvider.builder()
                .reference(UUID.randomUUID().toString())
                .amount(Amount.builder()
                        .currency(request.getAddServiceProviderApiModel().getCurrency())
                        .value(request.getAddServiceProviderApiModel().getValue())
                        .build())
                .comment(request.getAddServiceProviderApiModel().getComment())
                .userReference(request.getAuthenticatedUser().getUserReference())
                .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .investmentStatus(InvestmentStatus.builder()
                        .status(InvestmentTransactionStatus.PENDING)
                        .build())
                .build();
        MenialRequest response = this.commandHelper.addItemToCollection(found, serviceProvider, found::getServiceProviders, found::setServiceProviders);
        return GenericCommandResponse.builder()
                .requestReference(found.getReference())
                .itemReference(serviceProvider.getReference())
                .menialRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(AddServiceProviderCommandRequest request) {
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
        MenialRequest request = commandResponse.getMenialRequest();
        Optional<ServiceProvider> optionalInvestor = request.getServiceProviders().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(commandResponse.getItemReference()))
                .findFirst();

        if (optionalInvestor.isEmpty()) {
            return PEER_INVESTOR_ADDED;
        } else {
            String value = optionalInvestor.get().getAmount().getValue();
            BigDecimal investment = new BigDecimal(value);
            BigDecimal targetAmount = new BigDecimal(request.getAmount().getValue());
            int result = investment.compareTo(targetAmount);
            return result == 0 ? SOLO_INVESTOR_ADDED : PEER_INVESTOR_ADDED;
        }
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return getAllPartiesExceptActor(commandResponse.getMenialRequest(), commandResponse.getItemReference());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, MenialRequest> parameters) throws JsonProcessingException {
        Optional<ServiceProvider> optional = parameters.getCommandResponse().getMenialRequest().getServiceProviders().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(parameters.getCommandResponse().getItemReference()))
                .findFirst();

        Amount amount;
        if (optional.isEmpty()) {
            amount = Amount.builder().currency("X").value("Y").build();
        } else {
            amount = optional.get().getAmount();
        }

        String template;
        if (parameters.getCommandResponse().getMenialRequest().getServiceClient().getUserReference()
                .equalsIgnoreCase(parameters.getUserReference())) {
            template = "someone wants to charge %s %s your Service Request";
        } else {
            template = "someone else wants to charge %s %s for a Service Request post that you commented on";
        }

        String summary = String.format(template, amount.getValue(), amount.getCurrency());

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

    @Override
    public ResourceReferenceResponse map(GenericCommandResponse genericCommandResponse) {
        return new ResourceReferenceResponse(genericCommandResponse.getItemReference());
    }

    @Override
    public String convertToTTLMessage(MenialRequest menialRequest, GenericCommandResponse genericCommandResponse) throws JsonProcessingException {
        URI deletionLink = UriComponentsBuilder.fromUriString(configuration.getSelfUrl())
                .path("/menial/requests/")
                .path(genericCommandResponse.getRequestReference())
                .path("/service-providers/")
                .path(genericCommandResponse.getItemReference())
                .path("/ttl")
                .build().toUri();

        return this.commandHelper.toJsonString(TimeToLiveModel.builder()
                .serviceCode(menialRequest.getServiceCode())
                .requestReference(menialRequest.getReference())
                .investmentReference(genericCommandResponse.getItemReference())
                .requestCreatedOn(menialRequest.getCreatedOn())
                .deletionLink(deletionLink.toString())
                .build());
    }
}
