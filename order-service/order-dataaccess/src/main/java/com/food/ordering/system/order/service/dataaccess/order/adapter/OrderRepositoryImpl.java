package com.food.ordering.system.order.service.dataaccess.order.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.food.ordering.system.order.service.dataaccess.order.mapper.OrderDataAccessMapper;
import com.food.ordering.system.order.service.dataaccess.order.repository.OrderJpaRepository;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

/*
 * ドメイン層のapplicationサービスのoutputポートであるOrderRepositoryを実装するクラス
 * OrderRepositoryは、ドメイン層のOrderエンティティをJPAエンティティに変換し、
 * データベースに保存する責務を持つ。
 * また、TrackingIdでOrderを検索する機能も持つ。
 */

@Component
public class OrderRepositoryImpl implements OrderRepository{

    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository, OrderDataAccessMapper orderDataAccessMapper) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderDataAccessMapper = orderDataAccessMapper;
    }

    @Override
    public Order save(Order order) {
        return orderDataAccessMapper.orderEntityToOrder(
            orderJpaRepository.save(orderDataAccessMapper.orderToOrderEntity(order)));
    }

    // 指定されたTrackingIdでOrderを検索する
    @Override
    public Optional<Order> findByTrackingId(TrackingId trackingId) {
        return orderJpaRepository.findByTrackingId(trackingId.getValue())
            // finadByTrackingIdの戻り値はOptional<OrderEntity>
            // Optionalのmapメソッドは、Optionalの変数がNullでない場合にmapの中を実行する。
            // map()内は、メソッド参照。 orderEntity -> orderDataAccessMapper.orderENtityToOrder(orderENtity)と同義
            .map(orderDataAccessMapper::orderEntityToOrder);
    }
}
