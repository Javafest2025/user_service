package org.solace.scholar_ai.user_service.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.cloudinary-url}") String cloudinaryUrl) {
        // Using cloudinary.cloudinary-url from application.yml
        return new Cloudinary(cloudinaryUrl);
    }
}
