package io.angularpay.menial.ports.outbound;

import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PersistencePort {
    MenialRequest createRequest(MenialRequest request);
    MenialRequest updateRequest(MenialRequest request);
    Optional<MenialRequest> findRequestByReference(String reference);
    Page<MenialRequest> listRequests(Pageable pageable);
    Page<MenialRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses);
    Page<MenialRequest> findByServiceClientUserReference(Pageable pageable, String userReference);
    long getCountByRequestStatus(RequestStatus status);
    long getTotalCount();
}
