package pl.alior.sil.example.testcontainers.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import pl.alior.sil.example.testcontainers.data.model.CustomerDto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class CustomerApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Container
    static JdbcDatabaseContainer<?> postgresContainer = new PostgreSQLContainer("postgres:14.5").withInitScript("db/init.sql");

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @DynamicPropertySource
    static void setUp(DynamicPropertyRegistry registry) {
        System.out.println("DJFSKJDHJFDSFJKDSHF");
        Startables.deepStart(postgresContainer).join();

        registry.add("datasource.mwapp.jdbc-url", postgresContainer::getJdbcUrl);
        registry.add("datasource.mwapp.username", postgresContainer::getUsername);
        registry.add("datasource.mwapp.password", postgresContainer::getPassword);
    }

    @Test
    public void isWorking() throws Exception {
        String jdbcUrl = postgresContainer.getJdbcUrl();
        String username = postgresContainer.getUsername();
        String password = postgresContainer.getPassword();
        Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
        ResultSet resultSet = conn.createStatement().executeQuery("SELECT 530;");
        resultSet.next();
        int result = resultSet.getInt(1);

        assertEquals(530, result);
    }

    @Test
    public void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(prepareCreateCustomerRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("1"))
                .andDo(MockMvcResultHandlers.print());
    }

    private CustomerDto prepareCreateCustomerRequest() {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setFistName("Robert");
        customerDto.setLastName("Burek");
        customerDto.setPesel("87081415271");
        return customerDto;
    }
}
