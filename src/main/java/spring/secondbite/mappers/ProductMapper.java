package spring.secondbite.mappers;

import org.mapstruct.*;
import spring.secondbite.dtos.products.CloneProductDto;
import spring.secondbite.dtos.products.ProductDetailResponseDto;
import spring.secondbite.dtos.products.ProductDto;
import spring.secondbite.dtos.products.ProductResponseDto;
import spring.secondbite.entities.Product;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "marketer", ignore = true)
    Product toEntity(ProductDto dto);

    @Mapping(target = "marketerId", source = "marketer.id")
    @Mapping(target = "marketerName", source = "marketer.user.name")
    @Mapping(target = "stallName", source = "marketer.stallName")
    @Mapping(target = "originalPrice", source = "price")
    @Mapping(target = "price", source = "discountedPrice")
    @Mapping(target = "discountPercentage", source = "discountPercentage")
    ProductResponseDto toResponseDto(Product product);

    @Mapping(target = "marketerId", source = "marketerId")
    @Mapping(target = "originalPrice", source = "productDto.originalPrice")
    @Mapping(target = "price", source = "productDto.price")
    @Mapping(target = "discountPercentage", source = "productDto.discountPercentage")
    ProductDetailResponseDto toDetailResponseDto(
            ProductResponseDto productDto, UUID marketerId,
            String marketerName, String stallName, Double marketerRating);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "marketer", ignore = true)
    @Mapping(target = "validation", source = "dto.validation")
    @Mapping(target = "price", source = "dto.price")
    @Mapping(target = "quantity", source = "dto.quantity")
    @Mapping(target = "isAutoDiscount", constant = "true")
    @Mapping(target = "manualDiscountPercentage", ignore = true)
    Product cloneEntity(Product original, CloneProductDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "marketer", ignore = true)
    void updateFromDto(ProductDto dto, @MappingTarget Product product);
}
