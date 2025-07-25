package kz.timshowtime.onlineShop.enums;

import lombok.Getter;

@Getter
public enum SortName {
   NO("no"), ALPHA("name"), PRICE("price");

    private final String fieldName;

    SortName(String fieldName) {
        this.fieldName = fieldName;
    }
}
