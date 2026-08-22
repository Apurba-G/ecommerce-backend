package com.ecommerce.user.config;

import com.ecommerce.common.constant.EventConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(EventConstants.AUTH_EXCHANGE);
    }

    @Bean
    public Queue userProfileQueue() {
        return new Queue(EventConstants.USER_PROFILE_QUEUE, true);
    }

    @Bean
    public Binding userProfileBinding(Queue userProfileQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userProfileQueue)
                .to(authExchange)
                .with(EventConstants.USER_REGISTERED_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
