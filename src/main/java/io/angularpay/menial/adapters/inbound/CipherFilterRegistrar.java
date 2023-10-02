package io.angularpay.menial.adapters.inbound;

import io.angularpay.menial.adapters.outbound.CipherServiceAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CipherFilterRegistrar {

    @ConditionalOnProperty(
            value = "angularpay.cipher.enabled",
            havingValue = "true",
            matchIfMissing = true)
    @Bean
    public FilterRegistrationBean<CipherFilter> registerPostCommentsRateLimiter(CipherServiceAdapter cipherServiceAdapter) {
        FilterRegistrationBean<CipherFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CipherFilter(cipherServiceAdapter));
        registrationBean.addUrlPatterns(
                "/menial/requests",
                "/menial/requests/*/summary",
                "/menial/requests/*/amount",
                "/menial/requests/*/service-providers",
                "/menial/requests/*/service-providers/*/payment"
        );
        return registrationBean;
    }
}
