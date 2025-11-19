package com.food.ordering.system.restaurant.service.domain.ports.output.repository;

import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;

/**
 * 注文承認結果の永続化を行う出力ポート
 */
public interface OrderApprovalRepository {

    /**
     * 注文承認結果を保存
     * @param orderApproval 承認結果エンティティ
     * @return 保存された承認結果
     */
    OrderApproval save(OrderApproval orderApproval);
}
