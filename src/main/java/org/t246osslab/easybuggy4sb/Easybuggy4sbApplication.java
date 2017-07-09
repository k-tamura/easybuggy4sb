package org.t246osslab.easybuggy4sb;

import org.apache.catalina.servlets.DefaultServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Easybuggy4sbApplication {

	public static void main(String[] args) {
		SpringApplication.run(Easybuggy4sbApplication.class, args);
	}

	@Bean
	public ServletRegistrationBean servletRegistrationBean() {
		final DefaultServlet servlet = new DefaultServlet();
		final ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/uid/*");
		bean.setEnabled(true);
		bean.addInitParameter("listings", "true");
		bean.setLoadOnStartup(1);
		return bean;
	}
}
