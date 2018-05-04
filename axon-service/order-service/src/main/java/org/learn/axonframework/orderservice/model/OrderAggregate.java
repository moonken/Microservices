package org.learn.axonframework.orderservice.model;

import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateIdentifier;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.spring.stereotype.Aggregate;
import org.learn.axonframework.coreapi.OrderCancelledEvent;
import org.learn.axonframework.coreapi.OrderCompletedEvent;
import org.learn.axonframework.coreapi.OrderFiledEvent;
import org.learn.axonframework.orderservice.command.CancelOrderCommand;
import org.learn.axonframework.orderservice.command.FileOrderCommand;
import org.learn.axonframework.orderservice.command.OrderCompletedCommand;

import static org.axonframework.commandhandling.model.AggregateLifecycle.apply;

@NoArgsConstructor
@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;

    private boolean completed;

    @CommandHandler
    public OrderAggregate(FileOrderCommand command) {
        apply(new OrderFiledEvent(command.getOrderId(), command.getProductInfo()));
    }

    @CommandHandler
    public void handle(OrderCompletedCommand command) {
        apply(new OrderCompletedEvent(command.getOrderId(), command.getProductInfo()));
    }

    @CommandHandler
    public void handle(CancelOrderCommand command) {
        apply(new OrderCancelledEvent(command.getOrderId()));
    }

    @EventSourcingHandler
    public void on(OrderFiledEvent event) {
        orderId = event.getOrderId();
    }

}
