package spring.secondbite.repositories.specs;

import org.springframework.data.jpa.domain.Specification;
import spring.secondbite.entities.Product;
import spring.secondbite.entities.enums.Category;

public class ProductSpecs {
    public static Specification<Product> nameLike(String name) {
        return (root, query, cb) ->
                cb.like( cb.upper(root.get("name")), "%" + name.toUpperCase() + "%");
    }

    public static Specification<Product> categoryEqual(Category category) {
        return (root, query, cb) ->
                cb.equal(root.get("category"), category);
    }
}
