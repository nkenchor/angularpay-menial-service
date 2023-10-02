package io.angularpay.menial.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.menial.adapters.outbound.MongoAdapter;
import io.angularpay.menial.configurations.AngularPayConfiguration;
import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.Offer;
import io.angularpay.menial.domain.RequestStatus;
import io.angularpay.menial.domain.ServiceProvider;
import io.angularpay.menial.exceptions.CommandException;
import io.angularpay.menial.exceptions.ErrorCode;
import io.angularpay.menial.models.GenericCommandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.angularpay.menial.domain.InvestmentTransactionStatus.SUCCESSFUL;
import static io.angularpay.menial.exceptions.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CommandHelper {

    private final MongoAdapter mongoAdapter;
    private final ObjectMapper mapper;
    private final AngularPayConfiguration configuration;

    public GenericCommandResponse executeAcid(Supplier<GenericCommandResponse> supplier) {
        int maxRetry = this.configuration.getMaxUpdateRetry();
        OptimisticLockingFailureException optimisticLockingFailureException;
        int counter = 0;
        //noinspection ConstantConditions
        do {
            try {
                return supplier.get();
            } catch (OptimisticLockingFailureException exception) {
                if (counter++ >= maxRetry) throw exception;
                optimisticLockingFailureException = exception;
            }
        }
        while (Objects.nonNull(optimisticLockingFailureException));
        throw optimisticLockingFailureException;
    }

    public String getRequestOwner(String requestReference) {
        MenialRequest found = this.mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        return found.getServiceClient().getUserReference();
    }

    private static CommandException commandException(HttpStatus status, ErrorCode errorCode) {
        return CommandException.builder()
                .status(status)
                .errorCode(errorCode)
                .message(errorCode.getDefaultMessage())
                .build();
    }

    public String getInvestmentOwner(String requestReference, String investmentReference) {
        MenialRequest found = this.mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        if (CollectionUtils.isEmpty(found.getServiceProviders())) return "";
        return found.getServiceProviders().stream()
                .filter(x -> investmentReference.equalsIgnoreCase(x.getReference()))
                .map(ServiceProvider::getUserReference)
                .findFirst()
                .orElse("");
    }

    public String getBargainOwner(String requestReference, String bargainReference) {
        MenialRequest found = this.mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
        if (Objects.isNull(found.getBargain()) || CollectionUtils.isEmpty(found.getBargain().getOffers())) return "";
        return found.getBargain().getOffers().stream()
                .filter(x -> bargainReference.equalsIgnoreCase(x.getReference()))
                .map(Offer::getUserReference)
                .findFirst()
                .orElse("");
    }

    public <T> MenialRequest updateProperty(MenialRequest menialRequest, Supplier<T> getter, Consumer<T> setter) {
        setter.accept(getter.get());
        return this.mongoAdapter.updateRequest(menialRequest);
    }

    public <T> MenialRequest addItemToCollection(MenialRequest menialRequest, T newProperty, Supplier<List<T>> collectionGetter, Consumer<List<T>> collectionSetter) {
        if (CollectionUtils.isEmpty(collectionGetter.get())) {
            collectionSetter.accept(new ArrayList<>());
        }
        collectionGetter.get().add(newProperty);
        return this.mongoAdapter.updateRequest(menialRequest);
    }

    public <T> String toJsonString(T t) throws JsonProcessingException {
        return this.mapper.writeValueAsString(t);
    }

    public static MenialRequest getRequestByReferenceOrThrow(MongoAdapter mongoAdapter, String requestReference) {
        return mongoAdapter.findRequestByReference(requestReference).orElseThrow(
                () -> commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND)
        );
    }

    public static void validRequestStatusAndBargainExists(MenialRequest found, String bargainReference) {
        validRequestStatusOrThrow(found);
        if (Objects.isNull(found.getBargain()) || CollectionUtils.isEmpty(found.getBargain().getOffers())) {
            throw commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND);
        }
        if (found.getBargain().getOffers().stream().noneMatch(x -> bargainReference.equalsIgnoreCase(x.getReference()))) {
            throw commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND);
        }
    }

    public static void validRequestStatusAndInvestmentExists(MenialRequest found, String investmentReference) {
        validRequestStatusOrThrow(found);
        if (CollectionUtils.isEmpty(found.getServiceProviders())) {
            throw commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND);
        }
        if (found.getServiceProviders().stream().noneMatch(x -> investmentReference.equalsIgnoreCase(x.getReference()))) {
            throw commandException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND);
        }
    }

    public static void validRequestStatusOrThrow(MenialRequest found) {
        if (found.getStatus() == RequestStatus.COMPLETED) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, REQUEST_COMPLETED_ERROR);
        }
        if (found.getStatus() == RequestStatus.CANCELLED) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, REQUEST_CANCELLED_ERROR);
        }
    }

    public static void validateInvestmentStatusOrThrow(ServiceProvider serviceProvider) {
        if (Objects.nonNull(serviceProvider.getInvestmentStatus()) && serviceProvider.getInvestmentStatus().getStatus() == SUCCESSFUL) {
            throw commandException(HttpStatus.UNPROCESSABLE_ENTITY, REQUEST_COMPLETED_ERROR);
        }
    }
}
