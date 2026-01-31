package spring.secondbite.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.secondbite.entities.CartItem;

import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
}
