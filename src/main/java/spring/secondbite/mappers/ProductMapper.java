package spring.secondbite.mappers;

import org.mapstruct.*;
import spring.secondbite.dtos.products.ProductDto;
import spring.secondbite.dtos.products.ProductResponseDto;
import spring.secondbite.entities.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "marketer", ignore = true)
    Product toEntity(ProductDto dto);

    ProductResponseDto toResponseDto(Product product);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "marketer", ignore = true)
    void updateFromDto(ProductDto dto, @MappingTarget Product product);
}
