package com.thoughtworks.ddd.bootcamp;

import com.rabbitmq.client.Channel;
import com.thoughtworks.domain.event.OrderCreatedEvent;
import com.thoughtworks.domain.model.OrderView;
import org.axonframework.amqp.eventhandling.DefaultAMQPMessageConverter;
import org.axonframework.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.serialization.Serializer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
@EnableCouchbaseRepositories(basePackageClasses = {OrderQueryRepository.class})

public class OrderQueryApplication extends AbstractCouchbaseConfiguration {

	public static void main(String[] args) {
		SpringApplication.run(OrderQueryApplication.class, args);
	}

	@Override
	protected List<String> getBootstrapHosts() {
		return Collections.singletonList("127.0.0.1");
	}

	@Override
	protected String getBucketName() {
		return "orders";
	}

	@Override
	protected String getBucketPassword() {
		return "";
	}

	@Component
	@ProcessingGroup("statistics")
	public static class OrderUpdater {

		private OrderQueryRepository repository;

		public OrderUpdater(OrderQueryRepository repository) {
			this.repository = repository;
		}

		@EventHandler
		public void handle(OrderCreatedEvent event) {
			repository.save(new OrderView(event.getId(), event.getItems()));
		}
	}

	@Bean
	public SpringAMQPMessageSource orderSource(Serializer serlializer) {

		return new SpringAMQPMessageSource(new DefaultAMQPMessageConverter(serlializer)) {
			@RabbitListener(queues = "OrderCreatedEvents")
			public void onMessage(Message message, Channel channel) throws Exception {
				super.onMessage(message, channel);
			}
		};
	}
}
