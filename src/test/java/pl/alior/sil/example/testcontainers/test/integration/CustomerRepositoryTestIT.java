package pl.alior.sil.example.testcontainers.test.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import pl.alior.sil.example.testcontainers.CleanUpTest;
import pl.alior.sil.example.testcontainers.data.entity.mwapp.AddressEntity;
import pl.alior.sil.example.testcontainers.data.entity.mwapp.CustomerEntity;
import pl.alior.sil.example.testcontainers.data.repository.mwapp.CustomerRepository;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ExtendWith(CleanUpTest.class)
class CustomerRepositoryTestIT {//extends CleanUpTest {

    @Container
    static JdbcDatabaseContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.5").withUsername("customers");
    @Autowired
    CustomerRepository customerRepository;

    @DynamicPropertySource
    static void setUp(DynamicPropertyRegistry registry) {
        Startables.deepStart(postgresContainer).join();

        registry.add("datasource.mwapp.jdbc-url", postgresContainer::getJdbcUrl);
        registry.add("datasource.mwapp.username", postgresContainer::getUsername);
        registry.add("datasource.mwapp.password", postgresContainer::getPassword);
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgresContainer::getUsername);
        registry.add("spring.flyway.password", postgresContainer::getPassword);
        registry.add("spring.flyway.schemas", () -> "customers");
        registry.add("spring.flyway.default-schema", () -> "customers");
    }

    @Test
    void shouldCreateCustomer() {
        CustomerEntity entity = new CustomerEntity();
        entity.setFirstName("Jacenty");
        entity.setLastName("Toto");
        entity.setPesel("45761245678");
        AddressEntity address = new AddressEntity();
        address.setCustomer(entity);
        address.setAddress("Address1");
        address.setType(1);
        entity.setAddresses(Set.of(address));
        customerRepository.save(entity);

        assertEquals(1L, entity.getCustomerId());

        entity = new CustomerEntity();
        entity.setFirstName("Grzegorz");
        entity.setLastName("Krychowiak");
        entity.setPesel("54678712345");
        customerRepository.save(entity);

        assertEquals(2L, entity.getCustomerId());
        assertEquals(2L, customerRepository.count());

        CustomerEntity fetched = customerRepository.findById(1L).get();

        assertEquals("Address1", fetched.getAddresses().iterator().next().getAddress());
    }
}