
package io.angularpay.menial.models;

import io.angularpay.menial.domain.Amount;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CreateRequest {

    @NotNull
    @Valid
    private Amount amount;

    @NotEmpty
    private String summary;
}
