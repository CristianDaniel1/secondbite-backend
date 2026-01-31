package spring.secondbite.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.secondbite.entities.AppUser;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
