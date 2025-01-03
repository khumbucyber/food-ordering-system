// 動画でこのEntity作成はなかった気がするが、OrderDomainServiceでimportが必要となったため作成

package com.food.ordering.system.order.service.domain.entity;

import java.util.List;

import com.food.ordering.system.domain.ValueObject.RestaurantId;
import com.food.ordering.system.domain.entity.AggregateRoot;

// 外部クラス
public class Restaurant extends AggregateRoot<RestaurantId> {
    private final List<Product> products;
    private Boolean active;

    // 外部クラスのコンストラクタ
    private Restaurant(Builder builder) {
        super.setId(builder.restaurantId);
        this.products = builder.products;
        this.active = builder.active;
    }

    // 内部クラスをnewするメソッド
    // これを最初書かなかった。これ要るの？
    public static Builder builder() {
        return new Builder();
    }

    // 内部クラス
    public static class Builder {
        private RestaurantId restaurantId;
        private List<Product> products;
        private Boolean active;

        public Builder setRestaurantId(RestaurantId restaurantId) {
            this.restaurantId = restaurantId;
            return this;
        }
        public Builder setProducts(List<Product> products) {
            this.products = products;
            return this;
        }
        public Builder setActive(Boolean active) {
            this.active = active;
            return this;
        }
        public Restaurant build() {
            Restaurant restaurant = new Restaurant(this);
            return restaurant;
        }
    } 

    public List<Product> getProducts() {
        return this.products;
    }
    public boolean isActive() {
        // とりあえず実装しておく
        // activeはBooleanのフラグなのでそのまま返せばいいだろう
        return this.active;
    }
}
