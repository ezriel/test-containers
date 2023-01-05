package pl.alior.sil.example.testcontainers.data.entity.mwapp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "address")
@Getter
@Setter
@ToString(exclude = "customer")
public class AddressEntity {
    @Id
    @SequenceGenerator(name = "address_id_gen", sequenceName = "address_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_id_gen")
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "type")
    private Integer type;
    @Column(name = "address")
    private String address;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressEntity that = (AddressEntity) o;
        return Objects.equals(type, that.type) && Objects.equals(customer, that.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, customer);
    }
}
