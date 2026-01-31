package spring.secondbite.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring.secondbite.dtos.cart.CartItemResponseDto;
import spring.secondbite.dtos.cart.CartResponseDto;
import spring.secondbite.entities.Cart;
import spring.secondbite.entities.CartItem;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface CartMapper {

    @Mapping(target = "product", source = "product")
    @Mapping(target = "subTotal", expression = "java(item.getSubTotal())")
    CartItemResponseDto toItemDto(CartItem item);

    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalAmount", expression = "java(cart.getTotalAmount())")
    @Mapping(target = "totalItems", expression = "java(cart.getItems().size())")
    CartResponseDto toCartDto(Cart cart);
}
