package spring.secondbite.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.secondbite.dtos.marketers.MarketerResponseDto;
import spring.secondbite.dtos.marketers.UpdateMarketerDto;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Marketer;
import spring.secondbite.exceptions.UserNotFoundException;
import spring.secondbite.mappers.MarketerMapper;
import spring.secondbite.repositories.MarketerRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketerService {

    private final MarketerRepository repository;
    private final MarketerMapper mapper;

    public List<MarketerResponseDto> findAllMarketers() {
        List<Marketer> marketers = repository.findAll();

        return marketers
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public MarketerResponseDto findMarketerById(UUID id) {
        Marketer marketer = findOptionalMarketer(id);
        return mapper.toDTO(marketer);
    }

    @Transactional
    public MarketerResponseDto updateMarketer(UUID id, UpdateMarketerDto dto) {
        Marketer marketerEntity = findOptionalMarketer(id);
        Marketer marketer = mapper.partialUpdateMarketerFromDto(dto, marketerEntity);

        Marketer updatedMarketer = repository.save(marketer);
        return mapper.toDTO(updatedMarketer);
    }

    @Transactional
    public MarketerResponseDto deleteMarketer(UUID id) {
        Marketer marketer = findOptionalMarketer(id);
        repository.delete(marketer);
        return mapper.toDTO(marketer);
    }

    public Marketer findMarketerByUser(AppUser user) {
        return repository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }

    public Marketer findOptionalMarketer(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }
}
