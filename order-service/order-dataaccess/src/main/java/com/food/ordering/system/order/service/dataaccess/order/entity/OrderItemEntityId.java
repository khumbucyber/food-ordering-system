package com.food.ordering.system.order.service.dataaccess.order.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * OrderItemEntityIdは、OrderItemEntityのIDを表すクラスで、
 * OrderItemEntityのIDは、OrderEntityのIDとOrderItemEntityのIDの組み合わせで一意になる。
 * この複数列の主キーに対して、本クラスを作成する。
 * Serializableを実装する必要がある。識別子はエンティティを永続化するときにシリアル化できる必要があるため。
 * (シリアル化とは、オブジェクトをバイトストリームに変換して保存または送信できるようにするプロセス)
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntityId implements Serializable{

    private Long id;
    private OrderEntity orderEntity;
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((orderEntity == null) ? 0 : orderEntity.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OrderItemEntityId other = (OrderItemEntityId) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (orderEntity == null) {
            if (other.orderEntity != null)
                return false;
        } else if (!orderEntity.equals(other.orderEntity))
            return false;
        return true;
    }

}
