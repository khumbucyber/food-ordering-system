package com.food.ordering.system.restaurant.service.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.food.ordering.system.domain.event.publisher.DomainEventPublisher;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import com.food.ordering.system.restaurant.service.domain.exception.RestaurantNotFoundException;
import com.food.ordering.system.restaurant.service.domain.mapper.RestaurantDataMapper;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.RestaurantRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * 注文承認処理のヘルパークラス
 * トランザクション管理とビジネスロジックの調整を行う
 */
@Slf4j
@Component
public class RestaurantApprovalRequestHelper {

    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantDataMapper restaurantDataMapper;
    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final DomainEventPublisher<OrderApprovedEvent> orderApprovedEventPublisher;
    private final DomainEventPublisher<OrderRejectedEvent> orderRejectedEventPublisher;

    public RestaurantApprovalRequestHelper(RestaurantDomainService restaurantDomainService,
                                           RestaurantDataMapper restaurantDataMapper,
                                           RestaurantRepository restaurantRepository,
                                           OrderApprovalRepository orderApprovalRepository,
                                           DomainEventPublisher<OrderApprovedEvent> orderApprovedEventPublisher,
                                           DomainEventPublisher<OrderRejectedEvent> orderRejectedEventPublisher) {
        this.restaurantDomainService = restaurantDomainService;
        this.restaurantDataMapper = restaurantDataMapper;
        this.restaurantRepository = restaurantRepository;
        this.orderApprovalRepository = orderApprovalRepository;
        this.orderApprovedEventPublisher = orderApprovedEventPublisher;
        this.orderRejectedEventPublisher = orderRejectedEventPublisher;
    }

    /**
     * 注文承認処理を永続化
     * @param restaurantApprovalRequest 注文承認リクエスト
     */
    @Transactional
    public void persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        log.info("Processing restaurant approval for order id: {}", restaurantApprovalRequest.getOrderId());
        List<String> failureMessages = new ArrayList<>();
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
        // 注文の検証と承認イベントの生成
        // orderApprovalEventにはOrderApprovedEventまたはOrderRejectedEventが入る
        OrderApprovalEvent orderApprovalEvent =
            this.restaurantDomainService.validateOrder(
                restaurant,
                failureMessages,
                this.orderApprovedEventPublisher,
                this.orderRejectedEventPublisher);
        this.orderApprovalRepository.save(restaurant.getOrderApproval());

        orderApprovalEvent.fire();
    }

    /**
     * レストラン情報を取得
     * @param restaurantApprovalRequest 注文承認リクエスト
     * @return レストラン集約
     */
    private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        Restaurant restaurant = this.restaurantDataMapper.restaurantApprovalRequestToRestaurant(restaurantApprovalRequest);
        Restaurant restaurantInformation = this.restaurantRepository.findRestaurantInformation(restaurant)
            .orElseThrow(() -> {
                log.error("Restaurant with id: {} not found", restaurantApprovalRequest.getRestaurantId());
                return new RestaurantNotFoundException("Restaurant with id: " + 
                    restaurantApprovalRequest.getRestaurantId() + " not found");
            });
        
        restaurant.setActive(restaurantInformation.isActive());
        restaurant.getOrderDetail().getProducts().forEach(product -> 
            restaurantInformation.getOrderDetail().getProducts().forEach(restaurantProduct -> {
                if (restaurantProduct.getId().equals(product.getId())) {
                    product.updateWithConfirmedNamePriceAndAvailability(
                        restaurantProduct.getName(), 
                        restaurantProduct.getPrice(), 
                        restaurantProduct.isAvailable());
                }
            }));
        restaurant.getOrderDetail().setId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())));
        
        return restaurant;
    }
}
