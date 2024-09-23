package com.github.paopaoyue.metrics.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(Properties.class)
public class Configuration implements ApplicationContextAware {

    private static final Logger logger = LogManager.getLogger(Configuration.class);

    private static ConfigurableApplicationContext context;
    private static Properties prop;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        prop = applicationContext.getBean(Properties.class);
        context = (ConfigurableApplicationContext) applicationContext;
        logger.info("STS metrics service configuration loaded: {}", prop);
    }

    public static Properties getProp() {
        return prop;
    }

    public static boolean isRedisEnabled() {
        String redisHost = context.getEnvironment().getProperty("spring.data.redis.host");
        String redisPort = context.getEnvironment().getProperty("spring.data.redis.port");

        return redisHost != null && redisPort != null;
    }

    public static boolean isClickhouseEnabled() {
        return !prop.getClickhouseHost().isBlank() && prop.getClickhousePort() > 0;
    }

    @Bean
    @ConditionalOnProperty(value = "spring.data.redis.host")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Add some specific configuration here. Key serializers, etc.
        return template;
    }
}
