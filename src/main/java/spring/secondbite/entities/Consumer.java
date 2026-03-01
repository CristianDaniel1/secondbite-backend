package spring.secondbite.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "consumers", schema = "public")
@Getter
@Setter
public class Consumer {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cpf", length = 15, nullable = false, unique = true)
    private String cpf;

    @Column(name = "phone", length = 15, nullable = false)
    private String phone;

    @Column(name = "address", length = 200)
    private String address;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;
}
