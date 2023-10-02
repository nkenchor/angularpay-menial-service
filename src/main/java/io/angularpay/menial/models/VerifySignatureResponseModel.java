
package io.angularpay.menial.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifySignatureResponseModel {

    @JsonProperty("is_valid")
    private boolean valid;

}
