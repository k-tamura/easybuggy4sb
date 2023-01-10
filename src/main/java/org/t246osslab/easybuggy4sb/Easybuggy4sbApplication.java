package org.t246osslab.easybuggy4sb;

import org.apache.catalina.servlets.DefaultServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Easybuggy4sbApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Easybuggy4sbApplication.class, args);
	}

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Easybuggy4sbApplication.class);
    }
    
	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
	    /* Enable directory listing under /uid/ */
		final DefaultServlet servlet = new DefaultServlet();
		final ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/uid/backup/*");
		bean.setEnabled(true);
		bean.addInitParameter("listings", "true");
		bean.setLoadOnStartup(1);
		return bean;
	}
	
	@Bean
	public InitializationListener executorListener() {
	   return new InitializationListener();
	}
}
