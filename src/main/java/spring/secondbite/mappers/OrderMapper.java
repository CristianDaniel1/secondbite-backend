package spring.secondbite.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring.secondbite.dtos.orders.OrderItemDto;
import spring.secondbite.dtos.orders.OrderResponseDto;
import spring.secondbite.entities.Order;
import spring.secondbite.entities.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "consumerName", source = "consumer.user.name")
    @Mapping(target = "marketerName", source = "marketer.user.name")
    OrderResponseDto toDto(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "price", source = "priceAtPurchase")
    @Mapping(target = "productImageUrl", expression = "java(item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty() ? item.getProduct().getImages().get(0) : null)")
    OrderItemDto toItemDto(OrderItem item);
}