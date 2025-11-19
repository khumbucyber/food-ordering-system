package com.food.ordering.system.restaurant.service.dataaccess.restaurant.adapter;

import org.springframework.stereotype.Component;

import com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.OrderApprovalEntity;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import com.food.ordering.system.restaurant.service.dataaccess.restaurant.repository.OrderApprovalJpaRepository;
import com.food.ordering.system.restaurant.service.domain.entity.OrderApproval;
import com.food.ordering.system.restaurant.service.domain.ports.output.repository.OrderApprovalRepository;

/**
 * OrderApprovalRepository出力ポートの実装（アダプター）
 */
@Component
public class OrderApprovalRepositoryImpl implements OrderApprovalRepository {

    private final OrderApprovalJpaRepository orderApprovalJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    public OrderApprovalRepositoryImpl(OrderApprovalJpaRepository orderApprovalJpaRepository,
                                       RestaurantDataAccessMapper restaurantDataAccessMapper) {
        this.orderApprovalJpaRepository = orderApprovalJpaRepository;
        this.restaurantDataAccessMapper = restaurantDataAccessMapper;
    }

    /**
     * 注文承認結果を保存
     * @param orderApproval 承認結果エンティティ
     * @return 保存された承認結果
     */
    @Override
    public OrderApproval save(OrderApproval orderApproval) {
        OrderApprovalEntity orderApprovalEntity = 
            this.restaurantDataAccessMapper.orderApprovalToOrderApprovalEntity(orderApproval);
        OrderApprovalEntity savedEntity = this.orderApprovalJpaRepository.save(orderApprovalEntity);
        return this.restaurantDataAccessMapper.orderApprovalEntityToOrderApproval(savedEntity);
    }
}
