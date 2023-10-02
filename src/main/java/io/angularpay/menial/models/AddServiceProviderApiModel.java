
package io.angularpay.menial.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddServiceProviderApiModel {

    @NotEmpty
    private String currency;

    @NotEmpty
    private String value;

    @NotEmpty
    private String comment;
}
