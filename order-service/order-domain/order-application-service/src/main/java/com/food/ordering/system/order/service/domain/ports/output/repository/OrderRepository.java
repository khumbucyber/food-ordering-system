package com.food.ordering.system.order.service.domain.ports.output.repository;

import java.util.Optional;

import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;

public interface OrderRepository {

    // ドメインエンティティをリポジトリに渡す
    // OrderエンティティオブジェクトをJPAエンティティオブジェクトに変換し、
    // データベースに保存する責務
    Order save(Order order);

    // Optionalを使用
    // 引数で指定されたtrackinIdを持つOrderが見つかる場合と見つからない場合があるため
    // Optionalの説明： https://chatgpt.com/share/677a81c4-0cb0-800e-988f-c194f87d76b5
    Optional<Order> findByTrackingId(TrackingId trackingId);
}
