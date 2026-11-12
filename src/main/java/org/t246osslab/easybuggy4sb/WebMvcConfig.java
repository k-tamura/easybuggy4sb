package org.t246osslab.easybuggy4sb;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Disable extension matching (the behavior of matching /login.php etc. to /login)
        configurer.setUseSuffixPatternMatch(false);
    }
}