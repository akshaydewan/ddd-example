package com.thoughtworks;

import com.thoughtworks.adapter.http.CartAPI;
import com.thoughtworks.domain.aggregate.Cart;
import com.thoughtworks.domain.command.handler.AddItemCommandHandler;
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
        CartAPI.class,
        Cart.class, AddItemCommandHandler.class,
        })
@EntityScan(basePackageClasses = {DomainEventEntry.class})
public class EcommerceShoppingApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceShoppingApplication.class, args);
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable("CartEvents").build();
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable("OrderEvents").build();
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.fanoutExchange("fanout").build();
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with("*").noargs();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(exchange()).with("*").noargs();
    }

    @Autowired
    public void configure(AmqpAdmin admin) {
        admin.declareQueue(queue());
        admin.declareQueue(orderQueue());

        admin.declareExchange(exchange());

        admin.declareBinding(binding());
        admin.declareBinding(orderBinding());
    }

}
