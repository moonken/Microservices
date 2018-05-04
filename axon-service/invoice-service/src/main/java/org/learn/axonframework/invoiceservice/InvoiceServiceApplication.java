package org.learn.axonframework.invoiceservice;

import org.axonframework.commandhandling.AsynchronousCommandBus;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.messaging.interceptors.TransactionManagingInterceptor;
import org.axonframework.serialization.Serializer;
import org.axonframework.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestOperations;

@EnableDiscoveryClient
@SpringBootApplication
public class InvoiceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvoiceServiceApplication.class, args);
	}

	@Bean
	public Exchange invoiceExchange() {
		return ExchangeBuilder.fanoutExchange("InvoiceEvents").durable(true).build();
	}

	// order AMQP queue

	@Bean
	public Queue orderQueue() {
		return QueueBuilder.durable("OrderQueue").build();
	}

	@Bean
	public Binding orderBinding() {
		return BindingBuilder.bind(orderQueue()).to(invoiceExchange()).with("*").noargs();
	}

	// query AMQP queue

	@Bean
	public Queue queryQueue() {
		return QueueBuilder.durable("QueryQueue").build();
	}

	@Bean
	public Binding queryBinding() {
		return BindingBuilder.bind(queryQueue()).to(invoiceExchange()).with("*").noargs();
	}

	@Autowired
	public void configure(AmqpAdmin admin) {
		admin.declareExchange(invoiceExchange());

		admin.declareQueue(orderQueue());
		admin.declareBinding(orderBinding());

		admin.declareQueue(queryQueue());
		admin.declareBinding(queryBinding());
	}

	//spring cloud settings - distributed command bus
	@Bean
	public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient) {
		return new SpringCloudCommandRouter(discoveryClient, new AnnotationRoutingStrategy());
	}

	@Bean
	public CommandBusConnector springHttpCommandBusConnector(@Qualifier("localSegment") CommandBus localSegment,
															 RestOperations restOperations,
															 Serializer serializer) {
		return new SpringHttpCommandBusConnector(localSegment, restOperations, serializer);
	}

	@Primary // to make sure this CommandBus implementation is used for autowiring
	@Bean
	public DistributedCommandBus springCloudDistributedCommandBus(CommandRouter commandRouter,
																  CommandBusConnector commandBusConnector) {
		return new DistributedCommandBus(commandRouter, commandBusConnector);
	}

	@Bean(destroyMethod = "shutdown")
	@Qualifier("localSegment")
	public CommandBus localSegment(TransactionManager transactionManager) {
		AsynchronousCommandBus asynchronousCommandBus = new AsynchronousCommandBus();
		asynchronousCommandBus.registerDispatchInterceptor(new BeanValidationInterceptor<>());
		asynchronousCommandBus.registerHandlerInterceptor(new TransactionManagingInterceptor<>(transactionManager));

		return asynchronousCommandBus;
	}
}
