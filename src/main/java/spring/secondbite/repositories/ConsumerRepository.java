package spring.secondbite.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Consumer;

import java.util.Optional;
import java.util.UUID;

public interface ConsumerRepository extends JpaRepository<Consumer, UUID> {
    Optional<Consumer> findByUser(AppUser user);
    boolean existsByCpf(String cpf);
}
