package kz.timshowtime.onlineShop.model;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode
public class OrderItemId implements Serializable {
    private Long order;
    private Long item;
}
