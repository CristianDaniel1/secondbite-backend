package spring.secondbite.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.secondbite.entities.AppUser;
import spring.secondbite.entities.Marketer;

import java.util.Optional;
import java.util.UUID;

public interface MarketerRepository extends JpaRepository<Marketer, UUID> {
    Optional<Marketer> findByUser(AppUser user);
    boolean existsByCnpj(String cnpj);

}
