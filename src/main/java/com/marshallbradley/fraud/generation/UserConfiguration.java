package com.marshallbradley.fraud.generation;

import com.marshallbradley.fraud.models.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "users")
@PropertySource(value = "classpath:users.yml", factory = YamlPropertySourceFactory.class)
@Data
public class UserConfiguration {

    private List<User> profiles;
}
