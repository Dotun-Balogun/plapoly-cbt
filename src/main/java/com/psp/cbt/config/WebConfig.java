package com.psp.cbt.config;

import com.psp.cbt.interceptor.AdminAuthInterceptor;
import com.psp.cbt.interceptor.StudentAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Registers our two "gatekeeper" interceptors so they run before
// any request that starts with /admin/ or /student/.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final StudentAuthInterceptor studentAuthInterceptor;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor,
                      StudentAuthInterceptor studentAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.studentAuthInterceptor = studentAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");

        registry.addInterceptor(studentAuthInterceptor)
                .addPathPatterns("/student/**")
                .excludePathPatterns("/student/login");
    }
}
