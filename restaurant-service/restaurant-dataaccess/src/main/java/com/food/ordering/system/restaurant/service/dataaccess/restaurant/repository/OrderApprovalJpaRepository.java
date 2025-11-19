package com.food.ordering.system.restaurant.service.dataaccess.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.ordering.system.restaurant.service.dataaccess.restaurant.entity.OrderApprovalEntity;

import java.util.UUID;

/**
 * 注文承認結果の永続化用JPAリポジトリ
 */
@Repository
public interface OrderApprovalJpaRepository extends JpaRepository<OrderApprovalEntity, UUID> {
}
