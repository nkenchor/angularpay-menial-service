package io.angularpay.menial.models;

import io.angularpay.menial.domain.DeletedBy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RemoveServiceProviderCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotEmpty
    private String investmentReference;

    @NotNull
    private DeletedBy deletedBy;

    RemoveServiceProviderCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
