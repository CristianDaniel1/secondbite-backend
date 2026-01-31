package spring.secondbite.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.secondbite.dtos.consumers.ConsumerResponseDto;
import spring.secondbite.dtos.consumers.UpdateConsumerDto;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Consumer;
import spring.secondbite.exceptions.UserNotFoundException;
import spring.secondbite.mappers.ConsumerMapper;
import spring.secondbite.repositories.ConsumerRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsumerService {
    private final ConsumerRepository repository;
    private final ConsumerMapper mapper;

    public List<ConsumerResponseDto> findAllConsumers() {
        List<Consumer> consumersResult = repository.findAll();

        return consumersResult
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public ConsumerResponseDto findConsumerById(UUID id) {
        Consumer consumer = findOptionalConsumer(id);
        return mapper.toDTO(consumer);
    }

    @Transactional
    public ConsumerResponseDto updateConsumer(UUID id, UpdateConsumerDto dto) {
        Consumer consumerEntity = findOptionalConsumer(id);
        Consumer consumer = mapper.partialUpdateConsumerFromDto(dto, consumerEntity);

        Consumer updatedConsumer = repository.save(consumer);
        return mapper.toDTO(updatedConsumer);
    }

    @Transactional
    public ConsumerResponseDto deleteConsumer(UUID id) {
        Consumer consumer = findOptionalConsumer(id);
        repository.delete(consumer);
        return mapper.toDTO(consumer);
    }

    public Consumer findConsumerByUser(AppUser user) {
        return repository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }

    public Consumer findOptionalConsumer(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }
}
