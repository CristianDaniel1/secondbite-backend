package spring.secondbite.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import spring.secondbite.dtos.consumers.ConsumerDto;
import spring.secondbite.dtos.consumers.ConsumerResponseDto;
import spring.secondbite.dtos.consumers.UpdateConsumerDto;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Consumer;

@Mapper(componentModel = "spring")
public abstract class ConsumerMapper {
    @Autowired
    PasswordEncoder encoder;

    @Mapping(target = "user", expression = "java(toAppUser(dto))")
    public abstract Consumer toEntity(ConsumerDto dto);

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "modifiedAt", source = "user.modifiedAt")
    public abstract ConsumerResponseDto toDTO(Consumer candidate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.name", source = "name")
    @Mapping(target = "user.email", source = "email")
    @Mapping(target = "user.password", source = "password")
    public abstract void updateConsumerFromDto(ConsumerDto dto, @MappingTarget Consumer entity);

    public Consumer partialUpdateConsumerFromDto(UpdateConsumerDto dto, Consumer consumer) {
        if (dto.name() != null) consumer.getUser().setName(dto.name());
        if (dto.email() != null) consumer.getUser().setEmail(dto.email());
        if (dto.password() != null) {
            String encodedPassword = encoder.encode(dto.password());
            consumer.getUser().setPassword(encodedPassword);
        }
        if (dto.cpf() != null) consumer.setCpf(dto.cpf());
        if (dto.address() != null) consumer.setAddress(dto.address());
        if (dto.phone() != null) consumer.setPhone(dto.phone());

        return consumer;
    }

    public AppUser toAppUser(ConsumerDto dto) {
        AppUser user = new AppUser();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        return user;
    }
}
