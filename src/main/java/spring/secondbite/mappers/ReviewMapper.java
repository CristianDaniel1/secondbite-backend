package spring.secondbite.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import spring.secondbite.dtos.reviews.ReviewDto;
import spring.secondbite.dtos.reviews.ReviewResponseDto;
import spring.secondbite.entities.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "consumer", ignore = true)
    @Mapping(target = "marketer", ignore = true)
    Review toEntity(ReviewDto dto);

    @Mapping(target = "consumerName", source = "consumer.user.name")
    ReviewResponseDto toDto(Review review);
}
