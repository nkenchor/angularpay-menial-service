package io.angularpay.menial.ports.inbound;

import io.angularpay.menial.domain.*;
import io.angularpay.menial.models.*;

import java.util.List;
import java.util.Map;

public interface RestApiPort {
    GenericReferenceResponse createScheduledRequest(String schedule, CreateRequest request, Map<String, String> headers);
    GenericReferenceResponse create(CreateRequest request, Map<String, String> headers);
    void updateSummary(String requestReference, SummaryModel summaryModel, Map<String, String> headers);
    void updateAmount(String requestReference, Amount amount, Map<String, String> headers);
    GenericReferenceResponse addServiceProvider(String requestReference, AddServiceProviderApiModel addServiceProviderApiModel, Map<String, String> headers);
    void removeServiceProvider(String requestReference, String investmentReference, Map<String, String> headers);
    void removeServiceProviderTTL(String requestReference, String investmentReference, Map<String, String> headers);
    void removeServiceProviderPlatform(String requestReference, String investmentReference, Map<String, String> headers);
    GenericReferenceResponse addBargain(String requestReference, AddBargainApiModel addBargainApiModel, Map<String, String> headers);
    void acceptBargain(String requestReference, String bargainReference, Map<String, String> headers);
    void rejectBargain(String requestReference, String bargainReference, Map<String, String> headers);
    void deleteBargain(String requestReference, String bargainReference, Map<String, String> headers);
    GenericReferenceResponse makePayment(String requestReference, String investmentReference, PaymentRequest paymentRequest, Map<String, String> headers);
    void updateRequestStatus(String requestReference, RequestStatusModel status, Map<String, String> headers);
    MenialRequest getRequestByReference(String requestReference, Map<String, String> headers);
    List<MenialRequest> getNewsfeedModel(int page, Map<String, String> headers);
    List<UserRequestModel> getUserRequests(int page, Map<String, String> headers);
    List<UserInvestmentModel> getUserInvestments(int page, Map<String, String> headers);
    List<MenialRequest> getNewsfeedByStatus(int page, List<RequestStatus> statuses, Map<String, String> headers);
    List<MenialRequest> getRequestListByStatus(int page, List<RequestStatus> statuses, Map<String, String> headers);
    List<MenialRequest> getRequestList(int page, Map<String, String> headers);
    List<Statistics> getStatistics(Map<String, String> headers);
}
