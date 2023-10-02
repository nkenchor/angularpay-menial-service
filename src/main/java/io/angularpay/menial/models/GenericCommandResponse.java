
package io.angularpay.menial.models;

import io.angularpay.menial.domain.MenialRequest;
import io.angularpay.menial.domain.commands.MenialRequestSupplier;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class GenericCommandResponse extends GenericReferenceResponse implements MenialRequestSupplier {

    private final String requestReference;
    private final String itemReference;
    private final MenialRequest menialRequest;
}
