package com.thoughtworks.ddd.bootcamp;

import com.rabbitmq.client.Channel;
import com.thoughtworks.domain.aggregate.Order;
import com.thoughtworks.domain.command.CreateOrderCommand;
import com.thoughtworks.domain.event.CartCheckoutEvent;
import org.axonframework.amqp.eventhandling.DefaultAMQPMessageConverter;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.axonframework.serialization.Serializer;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootApplication
@EntityScan(basePackageClasses = {DomainEventEntry.class})
@ComponentScan(basePackageClasses = {Order.class})
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

	@Component
	@ProcessingGroup("statistics")
	public static class OrderUpdater {

		private CommandGateway commandGateway;

		public OrderUpdater(CommandGateway commandGateway) {
			this.commandGateway = commandGateway;
		}

		@EventHandler
		public void handle(CartCheckoutEvent event) {
			String orderId = UUID.randomUUID().toString();
			commandGateway.send(new CreateOrderCommand(orderId,event.getId(), event.getItems()));
		}
	}

	@Bean
	public SpringAMQPMessageSource orderSource(Serializer serlializer) {

		return new SpringAMQPMessageSource(new DefaultAMQPMessageConverter(serlializer)) {
			@RabbitListener(queues = "OrderEvents")
			@Transactional
			public void onMessage(Message message, Channel channel) throws Exception {
				super.onMessage(message, channel);
			}
		};
	}


	@Bean
	public Queue queue() {
		return QueueBuilder.durable("OrderCreatedEvents").build();
	}

	@Bean
	public Exchange exchange() {
		return ExchangeBuilder.fanoutExchange("fanoutorder").build();
	}

	@Bean
	public Binding binding() {
		return BindingBuilder.bind(queue()).to(exchange()).with("*").noargs();
	}


	@Autowired
	public void configure(AmqpAdmin admin) {
		admin.declareQueue(queue());

		admin.declareExchange(exchange());

		admin.declareBinding(binding());
	}

}
