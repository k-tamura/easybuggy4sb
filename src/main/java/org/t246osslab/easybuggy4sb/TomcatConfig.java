package org.t246osslab.easybuggy4sb;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.t246osslab.easybuggy4sb.core.filters.AuthenticationFilter;
import org.t246osslab.easybuggy4sb.core.filters.EncodingFilter;
import org.t246osslab.easybuggy4sb.core.filters.HttpsEnforcementFilter;
import org.t246osslab.easybuggy4sb.core.filters.SecurityFilter;

@Configuration
public class TomcatConfig {

    @Bean
    public EmbeddedServletContainerFactory servletContainerFactory() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.addContextCustomizers(new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                context.setUseHttpOnly(false);
            }
        });
        Connector connector = new Connector(TomcatEmbeddedServletContainerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(80);
        connector.setSecure(false);
        tomcat.addAdditionalTomcatConnectors(connector);
        return tomcat;
    }

    @Bean
    public FilterRegistrationBean encodingFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new EncodingFilter());
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean authenticationFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new AuthenticationFilter());
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean httpsEnforcementFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new HttpsEnforcementFilter());
        registration.setOrder(3);
        return registration;
    }

    @Bean
    public FilterRegistrationBean securityFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new SecurityFilter());
        registration.setOrder(4);
        return registration;
    }
}