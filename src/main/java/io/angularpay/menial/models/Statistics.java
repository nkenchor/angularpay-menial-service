package io.angularpay.menial.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Statistics {
    private String name;
    private String value;
}
