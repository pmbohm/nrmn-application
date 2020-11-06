package au.org.aodn.nrmn.restapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${frontend.pages.whitelist}")
    private List<String> frontendPagesWhitelist;

    @Value("${app.cors.max_age_secs}")
    private long MAX_AGE_SECS;

    @Autowired
    GlobalRequestInterceptor globalRequestInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE")
            .maxAge(MAX_AGE_SECS);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalRequestInterceptor)
            .addPathPatterns("/api/**/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        /* forward incoming front end page requests to react */
        frontendPagesWhitelist.stream()
            .forEach(frontEndPage -> registry.addViewController(frontEndPage).setViewName("forward:/"));
    }
}
