package com.food.ordering.system.order.service.domain;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;

import lombok.extern.slf4j.Slf4j;

// OrderApplicationServiceから移譲され処理を行うクラス
// Domainサービスを呼び出す。
@Slf4j
@Component
public class OrderCreateCommandHandler {

    // createOrderの内容をOrderCreateHelperへ移したことでコメントアウト
    // 移したのは、Spring AOPの制約のため。@Transactional含めプロキシを通過する際に注入されるため呼び出し可能である必要がある？？？
    // ここからでは@Transactionalアノテーションは機能しないらしい。
    // testで動かすようになったら、試しにこっちから直接呼び出してみること。
    // // ドメインサービス
    // private final OrderDomainService orderDomainService;

    // // リポジトリ
    // private final OrderRepository orderRepository;
    // private final CustomerRepository customerRepository;
    // private final RestaurantRepository restaurantRepository;

    // データマッパー(入/出力⇔エンティティ)
    private final OrderDataMapper orderDataMapper;

    private final OrderCreateHelper orderCreateHelper;

    private final OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher;

    
    // コンストラクタインジェクション
    public OrderCreateCommandHandler(OrderDataMapper orderDataMapper, 
        OrderCreateHelper orderCreateHelper,
        OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher) {
        // this.orderDomainService = orderDomainService;
        // this.orderRepository = orderRepository;
        // this.customerRepository = customerRepository;
        // this.restaurantRepository = restaurantRepository;
        this.orderDataMapper = orderDataMapper;
        this.orderCreateHelper = orderCreateHelper;
        this.orderCreatedPaymentRequestMessagePublisher = orderCreatedPaymentRequestMessagePublisher;
    }

    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
        orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);
        return orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder());

        // Udemy-22 で、createOrderの内容をOrderCreateHelperへ移した
    } 
}
