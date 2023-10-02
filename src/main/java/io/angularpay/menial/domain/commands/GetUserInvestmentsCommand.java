package io.angularpay.menial.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.menial.adapters.outbound.MongoAdapter;
import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.Role;
import io.angularpay.menial.domain.ServiceProvider;
import io.angularpay.menial.exceptions.ErrorObject;
import io.angularpay.menial.models.GetUserInvestmentsCommandRequest;
import io.angularpay.menial.models.UserInvestmentModel;
import io.angularpay.menial.validation.DefaultConstraintValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GetUserInvestmentsCommand extends AbstractCommand<GetUserInvestmentsCommandRequest, List<UserInvestmentModel>> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetUserInvestmentsCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator) {
        super("GetUserInvestmentsCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetUserInvestmentsCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected List<UserInvestmentModel> handle(GetUserInvestmentsCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        List<UserInvestmentModel> investmentRequests = new ArrayList<>();
        List<MenialRequest> response = this.mongoAdapter.listRequests(pageable).getContent();
        for (MenialRequest menialRequest : response) {
            List<ServiceProvider> serviceProviders = menialRequest.getServiceProviders();
            for (ServiceProvider serviceProvider : serviceProviders) {
                if (request.getAuthenticatedUser().getUserReference().equalsIgnoreCase(serviceProvider.getUserReference())) {
                    investmentRequests.add(UserInvestmentModel.builder()
                            .requestReference(menialRequest.getReference())
                            .investmentReference(serviceProvider.getReference())
                            .userReference(serviceProvider.getUserReference())
                            .requestCreatedOn(serviceProvider.getCreatedOn())
                            .build());
                }
            }
        }
        return investmentRequests;
    }

    @Override
    protected List<ErrorObject> validate(GetUserInvestmentsCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }
}
