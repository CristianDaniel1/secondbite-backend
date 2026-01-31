package spring.secondbite.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.secondbite.entities.Cart;
import spring.secondbite.entities.Consumer;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByConsumer(Consumer consumer);
}