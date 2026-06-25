package com.app.koratime.common.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {
    private final AppProperties appProperties;

    @Bean
    public Cloudinary cloudinary() {
        AppProperties.Cloudinary cloudinary = appProperties.getCloudinary();

        return new Cloudinary(
                ObjectUtils.asMap(
                        "cloud_name", cloudinary.getCloudName(),
                        "api_key", cloudinary.getApiKey(),
                        "api_secret", cloudinary.getApiSecret(),
                        "secure", true
                )
        );
    }
}
