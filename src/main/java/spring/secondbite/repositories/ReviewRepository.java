package spring.secondbite.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spring.secondbite.entities.Consumer;
import spring.secondbite.entities.Marketer;
import spring.secondbite.entities.Review;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findAllByMarketer(Marketer marketer, Pageable pageable);

    boolean existsByConsumerAndMarketer(Consumer consumer, Marketer marketer);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.marketer.id = :marketerId")
    Double getAverageRating(@Param("marketerId") UUID marketerId);
}
