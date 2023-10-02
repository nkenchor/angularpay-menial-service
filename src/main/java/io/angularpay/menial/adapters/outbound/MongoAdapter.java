package io.angularpay.menial.adapters.outbound;

import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.RequestStatus;
import io.angularpay.menial.ports.outbound.PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoAdapter implements PersistencePort {

    private final MenialRepository menialRepository;

    @Override
    public MenialRequest createRequest(MenialRequest request) {
        request.setCreatedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return menialRepository.save(request);
    }

    @Override
    public MenialRequest updateRequest(MenialRequest request) {
        request.setLastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return menialRepository.save(request);
    }

    @Override
    public Optional<MenialRequest> findRequestByReference(String reference) {
        return menialRepository.findByReference(reference);
    }

    @Override
    public Page<MenialRequest> listRequests(Pageable pageable) {
        return menialRepository.findAll(pageable);
    }

    @Override
    public Page<MenialRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses) {
        return menialRepository.findByStatusIn(pageable, statuses);
    }

    @Override
    public Page<MenialRequest> findByServiceClientUserReference(Pageable pageable, String userReference) {
        return menialRepository.findByServiceClientUserReference(pageable, userReference);
    }

    @Override
    public long getCountByRequestStatus(RequestStatus status) {
        return menialRepository.countByStatus(status);
    }

    @Override
    public long getTotalCount() {
        return menialRepository.count();
    }
}
