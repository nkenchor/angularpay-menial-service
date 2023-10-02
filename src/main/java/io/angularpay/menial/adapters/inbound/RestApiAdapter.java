package io.angularpay.menial.adapters.inbound;

import io.angularpay.menial.configurations.AngularPayConfiguration;
import io.angularpay.menial.domain.*;
import io.angularpay.menial.domain.commands.*;
import io.angularpay.menial.models.*;
import io.angularpay.menial.ports.inbound.RestApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.angularpay.menial.domain.DeletedBy.*;
import static io.angularpay.menial.helpers.Helper.fromHeaders;

@RestController
@RequestMapping("/menial/requests")
@RequiredArgsConstructor
public class RestApiAdapter implements RestApiPort {

    private final AngularPayConfiguration configuration;

    private final CreateRequestCommand createRequestCommand;
    private final UpdateMenialSummaryCommand updateMenialSummaryCommand;
    private final UpdateAmountCommand updateAmountCommand;
    private final AddServiceProviderCommand addServiceProviderCommand;
    private final RemoveServiceProviderCommand removeServiceProviderCommand;
    private final AddBargainCommand addBargainCommand;
    private final AcceptBargainCommand acceptBargainCommand;
    private final RejectBargainCommand rejectBargainCommand;
    private final DeleteBargainCommand deleteBargainCommand;
    private final MakePaymentCommand makePaymentCommand;
    private final UpdateRequestStatusCommand updateRequestStatusCommand;
    private final GetRequestByReferenceCommand getRequestByReferenceCommand;
    private final GetNewsfeedCommand getNewsfeedCommand;
    private final GetUserRequestsCommand getUserRequestsCommand;
    private final GetUserInvestmentsCommand getUserInvestmentsCommand;
    private final GetNewsfeedByStatusCommand getNewsfeedByStatusCommand;
    private final GetRequestListByStatusCommand getRequestListByStatusCommand;
    private final GetRequestListCommand getRequestListCommand;
    private final ScheduledRequestCommand scheduledRequestCommand;
    private final GetStatisticsCommand getStatisticsCommand;

    @PostMapping("/schedule/{schedule}")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse createScheduledRequest(
            @PathVariable String schedule,
            @RequestBody CreateRequest request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        ScheduledRequestCommandRequest scheduledRequestCommandRequest = ScheduledRequestCommandRequest.builder()
                .runAt(schedule)
                .createRequest(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return scheduledRequestCommand.execute(scheduledRequestCommandRequest);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse create(
            @RequestBody CreateRequest request,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        CreateRequestCommandRequest createRequestCommandRequest = CreateRequestCommandRequest.builder()
                .createRequest(request)
                .authenticatedUser(authenticatedUser)
                .build();
        return createRequestCommand.execute(createRequestCommandRequest);
    }

    @PutMapping("/{requestReference}/summary")
    @Override
    public void updateSummary(
            @PathVariable String requestReference,
            @RequestBody SummaryModel summaryModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateMenialSummaryCommandRequest updateMenialSummaryCommandRequest = UpdateMenialSummaryCommandRequest.builder()
                .requestReference(requestReference)
                .summary(summaryModel.getSummary())
                .authenticatedUser(authenticatedUser)
                .build();
        updateMenialSummaryCommand.execute(updateMenialSummaryCommandRequest);
    }

    @PutMapping("/{requestReference}/amount")
    @Override
    public void updateAmount(
            @PathVariable String requestReference,
            @RequestBody Amount amount,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateAmountCommandRequest updateAmountCommandRequest = UpdateAmountCommandRequest.builder()
                .requestReference(requestReference)
                .amount(amount)
                .authenticatedUser(authenticatedUser)
                .build();
        updateAmountCommand.execute(updateAmountCommandRequest);
    }

    @PostMapping("/{requestReference}/service-providers")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse addServiceProvider(
            @PathVariable String requestReference,
            @RequestBody AddServiceProviderApiModel addServiceProviderApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        AddServiceProviderCommandRequest addServiceProviderCommandRequest = AddServiceProviderCommandRequest.builder()
                .requestReference(requestReference)
                .addServiceProviderApiModel(addServiceProviderApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return addServiceProviderCommand.execute(addServiceProviderCommandRequest);
    }

    @DeleteMapping("/{requestReference}/service-providers/{investmentReference}")
    @Override
    public void removeServiceProvider(
            @PathVariable String requestReference,
            @PathVariable String investmentReference,
            @RequestHeader Map<String, String> headers) {
        removeServiceProvider(requestReference, investmentReference, headers, INVESTOR);
    }

    @DeleteMapping("/{requestReference}/service-providers/{investmentReference}/ttl")
    @Override
    public void removeServiceProviderTTL(
            @PathVariable String requestReference,
            @PathVariable String investmentReference,
            @RequestHeader Map<String, String> headers) {
        removeServiceProvider(requestReference, investmentReference, headers, TTL_SERVICE);
    }

    @DeleteMapping("/{requestReference}/service-providers/{investmentReference}/platform")
    @Override
    public void removeServiceProviderPlatform(
            @PathVariable String requestReference,
            @PathVariable String investmentReference,
            @RequestHeader Map<String, String> headers) {
        removeServiceProvider(requestReference, investmentReference, headers, PLATFORM);
    }

    private void removeServiceProvider(
            String requestReference,
            String investmentReference,
            Map<String, String> headers,
            DeletedBy deletedBy) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RemoveServiceProviderCommandRequest removeServiceProviderCommandRequest = RemoveServiceProviderCommandRequest.builder()
                .requestReference(requestReference)
                .investmentReference(investmentReference)
                .deletedBy(deletedBy)
                .authenticatedUser(authenticatedUser)
                .build();
        removeServiceProviderCommand.execute(removeServiceProviderCommandRequest);
    }

    @PostMapping("{requestReference}/bargains")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse addBargain(
            @PathVariable String requestReference,
            @RequestBody AddBargainApiModel addBargainApiModel,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        AddBargainCommandRequest addBargainCommandRequest = AddBargainCommandRequest.builder()
                .requestReference(requestReference)
                .addBargainApiModel(addBargainApiModel)
                .authenticatedUser(authenticatedUser)
                .build();
        return addBargainCommand.execute(addBargainCommandRequest);
    }

    @PutMapping("{requestReference}/bargains/{bargainReference}")
    @Override
    public void acceptBargain(
            @PathVariable String requestReference,
            @PathVariable String bargainReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        AcceptBargainCommandRequest acceptBargainCommandRequest = AcceptBargainCommandRequest.builder()
                .requestReference(requestReference)
                .bargainReference(bargainReference)
                .authenticatedUser(authenticatedUser)
                .build();
        acceptBargainCommand.execute(acceptBargainCommandRequest);
    }

    @DeleteMapping("{requestReference}/bargains/{bargainReference}")
    @Override
    public void rejectBargain(
            @PathVariable String requestReference,
            @PathVariable String bargainReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        RejectBargainCommandRequest rejectBargainCommandRequest = RejectBargainCommandRequest.builder()
                .requestReference(requestReference)
                .bargainReference(bargainReference)
                .authenticatedUser(authenticatedUser)
                .build();
        rejectBargainCommand.execute(rejectBargainCommandRequest);
    }

    @DeleteMapping("{requestReference}/bargains/{bargainReference}/delete")
    @Override
    public void deleteBargain(
            @PathVariable String requestReference,
            @PathVariable String bargainReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        DeleteBargainCommandRequest deleteBargainCommandRequest = DeleteBargainCommandRequest.builder()
                .requestReference(requestReference)
                .bargainReference(bargainReference)
                .authenticatedUser(authenticatedUser)
                .build();
        deleteBargainCommand.execute(deleteBargainCommandRequest);
    }

    @PostMapping("{requestReference}/service-providers/{investmentReference}/payment")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public GenericReferenceResponse makePayment(
            @PathVariable String requestReference,
            @PathVariable String investmentReference,
            @RequestBody PaymentRequest paymentRequest,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        MakePaymentCommandRequest makePaymentCommandRequest = MakePaymentCommandRequest.builder()
                .requestReference(requestReference)
                .investmentReference(investmentReference)
                .paymentRequest(paymentRequest)
                .authenticatedUser(authenticatedUser)
                .build();
        return makePaymentCommand.execute(makePaymentCommandRequest);
    }

    @PutMapping("/{requestReference}/status")
    @Override
    public void updateRequestStatus(
            @PathVariable String requestReference,
            @RequestBody RequestStatusModel status,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        UpdateRequestStatusCommandRequest updateRequestStatusCommandRequest = UpdateRequestStatusCommandRequest.builder()
                .requestReference(requestReference)
                .status(status.getStatus())
                .authenticatedUser(authenticatedUser)
                .build();
        updateRequestStatusCommand.execute(updateRequestStatusCommandRequest);
    }

    @GetMapping("/{requestReference}")
    @Override
    public MenialRequest getRequestByReference(
            @PathVariable String requestReference,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetRequestByReferenceCommandRequest getRequestByReferenceCommandRequest = GetRequestByReferenceCommandRequest.builder()
                .requestReference(requestReference)
                .authenticatedUser(authenticatedUser)
                .build();
        return getRequestByReferenceCommand.execute(getRequestByReferenceCommandRequest);
    }

    @GetMapping("/list/newsfeed/page/{page}")
    @Override
    public List<MenialRequest> getNewsfeedModel(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetRequestListCommandRequest genericGetRequestListCommandRequest = GenericGetRequestListCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getNewsfeedCommand.execute(genericGetRequestListCommandRequest);
    }

    @GetMapping("/list/user-request/page/{page}")
    @Override
    public List<UserRequestModel> getUserRequests(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetUserRequestsCommandRequest getUserRequestsCommandRequest = GetUserRequestsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getUserRequestsCommand.execute(getUserRequestsCommandRequest);
    }

    @GetMapping("/list/user-investment/page/{page}")
    @Override
    public List<UserInvestmentModel> getUserInvestments(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetUserInvestmentsCommandRequest getUserInvestmentsCommandRequest = GetUserInvestmentsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getUserInvestmentsCommand.execute(getUserInvestmentsCommandRequest);
    }

    @GetMapping("/list/newsfeed/page/{page}/filter/statuses/{statuses}")
    @ResponseBody
    @Override
    public List<MenialRequest> getNewsfeedByStatus(
            @PathVariable int page,
            @PathVariable List<RequestStatus> statuses,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetByStatusCommandRequest genericGetByStatusCommandRequest = GenericGetByStatusCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .statuses(statuses)
                .build();
        return getNewsfeedByStatusCommand.execute(genericGetByStatusCommandRequest);
    }

    @GetMapping("/list/page/{page}/filter/statuses/{statuses}")
    @ResponseBody
    @Override
    public List<MenialRequest> getRequestListByStatus(
            @PathVariable int page,
            @PathVariable List<RequestStatus> statuses,
            Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetByStatusCommandRequest genericGetByStatusCommandRequest = GenericGetByStatusCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .statuses(statuses)
                .build();
        return getRequestListByStatusCommand.execute(genericGetByStatusCommandRequest);
    }

    @GetMapping("/list/page/{page}")
    @ResponseBody
    @Override
    public List<MenialRequest> getRequestList(
            @PathVariable int page,
            @RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GenericGetRequestListCommandRequest genericGetRequestListCommandRequest = GenericGetRequestListCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .paging(Paging.builder().size(this.configuration.getPageSize()).index(page).build())
                .build();
        return getRequestListCommand.execute(genericGetRequestListCommandRequest);
    }

    @GetMapping("/statistics")
    @ResponseBody
    @Override
    public List<Statistics> getStatistics(@RequestHeader Map<String, String> headers) {
        AuthenticatedUser authenticatedUser = fromHeaders(headers);
        GetStatisticsCommandRequest getStatisticsCommandRequest = GetStatisticsCommandRequest.builder()
                .authenticatedUser(authenticatedUser)
                .build();
        return getStatisticsCommand.execute(getStatisticsCommandRequest);
    }
}
