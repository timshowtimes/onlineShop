package kz.timshowtime.onlineShop.model.manyToMany.Embedded;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class OrderItemId implements Serializable {
    private Long orders;
    private Long item;
}
