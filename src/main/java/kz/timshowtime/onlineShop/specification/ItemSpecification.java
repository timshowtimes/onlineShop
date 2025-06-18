package kz.timshowtime.onlineShop.specification;

import kz.timshowtime.onlineShop.model.Item;
import org.springframework.data.jpa.domain.Specification;

public class ItemSpecification {
    public static Specification<Item> nameOrDescContains(String field) {
        return ((root, query, criteriaBuilder) -> {
            if (field == null || field.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            String pattern = "%" + field.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
            );
        });
    }
}
