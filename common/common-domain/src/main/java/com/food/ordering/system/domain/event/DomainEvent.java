package com.food.ordering.system.domain.event;

// ジェネリック。機能的にはこの定義は使わないが、イベントオブジェクトをマークすると役立つ。
// 例)OrderEventを作ったときに、このイベントを発生させる元のエンティティのOrderをマークする(Tに指定する)。
public interface DomainEvent<T> {
    void fire();
}
