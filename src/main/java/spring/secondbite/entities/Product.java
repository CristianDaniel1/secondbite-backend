package spring.secondbite.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import spring.secondbite.entities.enums.Category;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products", schema = "public")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Product {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "size_type", length = 10, nullable = false)
    private String sizeType;

    @Column(name = "validation", nullable = false)
    private LocalDate validation;

    @Column(name = "price", precision = 18, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private Category category;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_filename")
    private List<String> images = new ArrayList<>();

    @LastModifiedDate
    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marketer_id")
    private Marketer marketer;

    @Transient
    public Integer getDiscountPercentage() {
        if (this.validation == null) return 0;

        long daysToExpire = ChronoUnit.DAYS.between(LocalDate.now(), this.validation);

        if (daysToExpire <= 1) {
            return 75;
        } else if (daysToExpire <= 3) {
            return 50;
        } else {
            return 20;
        }
    }

    @Transient
    public BigDecimal getDiscountedPrice() {
        if (this.price == null) return BigDecimal.ZERO;

        Integer discount = getDiscountPercentage();
        if (discount == 0) return this.price;

        BigDecimal discountMultiplier = BigDecimal.valueOf(100 - discount).divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP);
        return this.price.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
