package spring.secondbite.repositories.specs;

import org.springframework.data.jpa.domain.Specification;
import spring.secondbite.entities.Consumer;
import spring.secondbite.entities.Marketer;
import spring.secondbite.entities.Order;
import spring.secondbite.entities.enums.Status;

public class OrderSpecs {

    public static Specification<Order> hasConsumer(Consumer consumer) {
        return (root, query, cb) -> cb.equal(root.get("consumer"), consumer);
    }

    public static Specification<Order> hasMarketer(Marketer marketer) {
        return (root, query, cb) -> cb.equal(root.get("marketer"), marketer);
    }

    public static Specification<Order> hasStatus(Status status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }
}
