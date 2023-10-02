
package io.angularpay.menial.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("menial_requests")
public class MenialRequest {

    @Id
    private String id;
    @Version
    private int version;
    @JsonProperty("service_code")
    private String serviceCode;
    private String summary;
    private Amount amount;
    private Bargain bargain;
    @JsonProperty("created_on")
    private String createdOn;
    @JsonProperty("service_client")
    private ServiceClient serviceClient;
    @JsonProperty("service_providers")
    private List<ServiceProvider> serviceProviders;
    @JsonProperty("last_modified")
    private String lastModified;
    private String reference;
    @JsonProperty("request_tag")
    private String requestTag;
    private RequestStatus status;
}
