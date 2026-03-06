package spring.secondbite.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spring.secondbite.entities.Marketer;
import spring.secondbite.entities.Product;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    List<Product> findAllByMarketer(Marketer marketer);

    long countByMarketerIdAndQuantityGreaterThan(UUID marketerId, Integer quantity);

    List<Product> findAllByIdInAndMarketerId(List<UUID> ids, UUID marketerId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.marketer.id = :marketerId AND p.quantity > 0 AND p.validation <= :thresholdDate")
    long countExpiringSoon(
            @Param("marketerId") UUID marketerId,
            @Param("thresholdDate") LocalDate thresholdDate
    );

    @Query("SELECT p FROM Product p WHERE p.marketer.id = :marketerId AND p.quantity > 0 AND p.validation <= :thresholdDate")
    List<Product> findExpiringSoonProducts(
            @Param("marketerId") UUID marketerId,
            @Param("thresholdDate") LocalDate thresholdDate
    );
}
