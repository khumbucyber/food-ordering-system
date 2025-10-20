package com.food.ordering.system.domain.entity;

public abstract class BaseEntity<ID> {
    private ID id;

    /*
     * This method is used to set the ID of the entity.
     * ※constructorにしない理由は、IDがnullであることを許容するため。
     * 　Orderの新規作成時にはIDが以外のフィールドを入力値で設定し、
     * 　後からIDを採番することがあるから？
     */
    public void setId(ID id) {
        this.id = id;
    }

    public ID getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        BaseEntity other = (BaseEntity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
