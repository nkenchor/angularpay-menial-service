package io.angularpay.menial.ports.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.menial.models.VerifySignatureResponseModel;

import java.util.Map;

public interface CipherServicePort {
    VerifySignatureResponseModel verifySignature(String requestBody, Map<String, String> headers) throws JsonProcessingException;
}
