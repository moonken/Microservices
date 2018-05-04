package org.learn.axonframework.orderservice.model;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.Before;
import org.junit.Test;
import org.learn.axonframework.coreapi.OrderFiledEvent;
import org.learn.axonframework.coreapi.ProductInfo;
import org.learn.axonframework.orderservice.command.FileOrderCommand;

public class OrderAggregateTest {

    private static final String ORDER1_ID = "1234";
    private static final String ORDER1_PRODUCT_ID = "testProduct";
    private static final String ORDER1_COMMENT = "testComment";
    private static final int ORDER1_PRICE = 100;

    private AggregateTestFixture<OrderAggregate> fixture;

    @Before
    public void setUp() {
        fixture = new AggregateTestFixture<>(OrderAggregate.class);
    }

    @Test
    public void testOrderCreatedFiresEvents() {
        fixture.givenNoPriorActivity()
                .when(new FileOrderCommand(ORDER1_ID, new ProductInfo(ORDER1_PRODUCT_ID, ORDER1_COMMENT, ORDER1_PRICE)))
                .expectEvents(new OrderFiledEvent(ORDER1_ID, new ProductInfo(ORDER1_PRODUCT_ID, ORDER1_COMMENT, ORDER1_PRICE)));
    }

}
