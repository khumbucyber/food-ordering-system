package com.food.ordering.system.order.service.dataaccess.customer.entity;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Udemy-36
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor // Bulderパターンを使用するのに必要
@Table(name = "order_customer_m_view", schema="customer")
@Entity
public class CustomerEntity {

    // Customerの存在チェックのみ実装するので、IDのみでよい
    @Id
    private UUID id;
}
