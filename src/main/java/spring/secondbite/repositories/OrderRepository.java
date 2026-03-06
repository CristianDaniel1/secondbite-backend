package spring.secondbite.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spring.secondbite.entities.Order;
import spring.secondbite.entities.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    long countByMarketerIdAndStatus(UUID marketerId, Status status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.marketer.id = :marketerId AND o.status = :status AND o.createdAt >= :startOfDay AND o.createdAt <= :endOfDay")
    BigDecimal sumRevenueByDateRange(
            @Param("marketerId") UUID marketerId,
            @Param("status") Status status,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}