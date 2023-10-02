package io.angularpay.menial.helpers;

import io.angularpay.menial.domain.Bargain;
import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.RequestStatus;

import java.util.ArrayList;
import java.util.UUID;

import static io.angularpay.menial.common.Constants.SERVICE_CODE;
import static io.angularpay.menial.util.SequenceGenerator.generateRequestTag;

public class ObjectFactory {

    public static MenialRequest pmtRequestWithDefaults() {
        return MenialRequest.builder()
                .reference(UUID.randomUUID().toString())
                .serviceCode(SERVICE_CODE)
                .status(RequestStatus.ACTIVE)
                .requestTag(generateRequestTag())
                .serviceProviders(new ArrayList<>())
                .bargain(Bargain.builder()
                        .offers(new ArrayList<>())
                        .build())
                .build();
    }
}