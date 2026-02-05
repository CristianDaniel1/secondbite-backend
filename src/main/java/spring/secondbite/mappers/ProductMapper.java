package spring.secondbite.mappers;

import org.mapstruct.*;
import spring.secondbite.dtos.products.ProductDetailResponseDto;
import spring.secondbite.dtos.products.ProductDto;
import spring.secondbite.dtos.products.ProductResponseDto;
import spring.secondbite.entities.Product;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "marketer", ignore = true)
    Product toEntity(ProductDto dto);

    ProductResponseDto toResponseDto(Product product);

    ProductDetailResponseDto toDetailResponseDto(
            ProductResponseDto productDto, UUID marketerid,
            String marketerName, String stallName, Double rating);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "marketer", ignore = true)
    void updateFromDto(ProductDto dto, @MappingTarget Product product);
}
