package kz.timshowtime.onlineShop.model.manyToMany.Embedded;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class CartItemId implements Serializable {
    private int cart;
    private int item;
}
