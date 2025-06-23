package kz.timshowtime.onlineShop.model.manyToMany.Embedded;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class CartItemId implements Serializable {
    private Integer cart;
    private Long item;
}
