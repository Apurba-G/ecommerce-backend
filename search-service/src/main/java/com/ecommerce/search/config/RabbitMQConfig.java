package com.ecommerce.search.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CATALOG_EXCHANGE = "catalog.exchange";
    public static final String SEARCH_CATALOG_QUEUE = "search.catalog.queue";
    public static final String CATALOG_ROUTING_KEY = "product.#";

    @Bean
    public TopicExchange catalogExchange() {
        return new TopicExchange(CATALOG_EXCHANGE, true, false);
    }

    @Bean
    public Queue searchCatalogQueue() {
        return QueueBuilder.durable(SEARCH_CATALOG_QUEUE).build();
    }

    @Bean
    public Binding searchCatalogBinding(Queue searchCatalogQueue, TopicExchange catalogExchange) {
        return BindingBuilder.bind(searchCatalogQueue).to(catalogExchange).with(CATALOG_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
