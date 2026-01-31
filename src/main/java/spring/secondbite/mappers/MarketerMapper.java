package spring.secondbite.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import spring.secondbite.dtos.marketers.MarketerDto;
import spring.secondbite.dtos.marketers.MarketerResponseDto;
import spring.secondbite.dtos.marketers.UpdateMarketerDto;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Marketer;

@Mapper(componentModel = "spring")
public abstract class MarketerMapper {
    @Autowired
    PasswordEncoder encoder;

    @Mapping(target = "user", expression = "java(toAppUser(dto))")
    public abstract Marketer toEntity(MarketerDto dto);

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "roles", source = "user.roles")
    public abstract MarketerResponseDto toDTO(Marketer company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.name", source = "name")
    @Mapping(target = "user.email", source = "email")
    @Mapping(target = "user.password", source = "password")
    public abstract void updateMarketerFromDto(MarketerDto dto, @MappingTarget Marketer entity);

    public Marketer partialUpdateMarketerFromDto(UpdateMarketerDto dto, Marketer marketer) {
        if (dto.name() != null) marketer.getUser().setName(dto.name());
        if (dto.email() != null) marketer.getUser().setEmail(dto.email());
        if (dto.password() != null) {
            String encodedPassword = encoder.encode(dto.password());
            marketer.getUser().setPassword(encodedPassword);
        }
        if (dto.cnpj() != null) marketer.setCnpj(dto.cnpj());
        if (dto.zipcode() != null) marketer.setZipcode(dto.zipcode());
        if (dto.phone() != null) marketer.setPhone(dto.phone());

        return marketer;
    }

    public AppUser toAppUser(MarketerDto dto) {
        AppUser user = new AppUser();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        return user;
    }
}
