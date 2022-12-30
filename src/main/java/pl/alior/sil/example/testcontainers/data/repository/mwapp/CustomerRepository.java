package pl.alior.sil.example.testcontainers.data.repository.mwapp;

import org.springframework.data.repository.CrudRepository;
import pl.alior.sil.example.testcontainers.data.entity.mwapp.CustomerEntity;

import java.util.List;

public interface CustomerRepository extends CrudRepository<CustomerEntity, Long> {
    List<CustomerEntity> findByFirstNameOrPesel(String fistName, String pesel);

    int countByPesel(String pesel);
}
