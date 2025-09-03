package org.t246osslab.easybuggy4sb;

import org.apache.catalina.Context;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;

@Configuration
public class TomcatConfig {

    @Bean
    public EmbeddedServletContainerFactory servletContainerFactory() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        tomcat.addContextCustomizers(tomcatContextCustomizer());
        return tomcat;
    }

    @Bean
    public TomcatContextCustomizer tomcatContextCustomizer() {
        return (context) -> {
            context.setUseHttpOnly(false);
        };
    }
}