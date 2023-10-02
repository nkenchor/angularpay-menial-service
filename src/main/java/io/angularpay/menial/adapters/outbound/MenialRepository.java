package io.angularpay.menial.adapters.outbound;

import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MenialRepository extends MongoRepository<MenialRequest, String> {

    Optional<MenialRequest> findByReference(String reference);
    Page<MenialRequest> findAll(Pageable pageable);
    Page<MenialRequest> findByStatusIn(Pageable pageable, List<RequestStatus> statuses);
    Page<MenialRequest> findByServiceClientUserReference(Pageable pageable, String userReference);
    long countByStatus(RequestStatus status);
}
