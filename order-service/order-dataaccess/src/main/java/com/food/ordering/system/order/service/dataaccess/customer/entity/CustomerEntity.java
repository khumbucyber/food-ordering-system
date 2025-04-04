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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor // Bulderパターンを使用するのに必要
@Table(name = "order_customer_m_view", schema="customer")
@Entity
public class CustomerEntity {

    @Id
    private UUID id;
}
