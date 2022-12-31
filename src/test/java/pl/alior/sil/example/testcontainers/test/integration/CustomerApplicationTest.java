package pl.alior.sil.example.testcontainers.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Container
    static JdbcDatabaseContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.5").withInitScript("db/init.sql");

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
        Startables.deepStart(postgresContainer).join();

        registry.add("datasource.mwapp.jdbc-url", postgresContainer::getJdbcUrl);
        registry.add("datasource.mwapp.username", postgresContainer::getUsername);
        registry.add("datasource.mwapp.password", postgresContainer::getPassword);
    }

    @Test
    @Order(0)
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

    @ParameterizedTest
    @MethodSource("createCustomersData")
    @Order(1)
    public void shouldCreateUser(String fistName, String lastName, String pesel, String expectedId) throws Exception {
        mockMvc.perform(post("/customers").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(prepareCreateCustomerRequest(fistName, lastName, pesel))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(expectedId))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(2)
    public void shouldGetCustomer() throws Exception {
        mockMvc.perform(get("/customers/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("2"))
                .andExpect(jsonPath("$.firstName").value("Kamil"))
                .andExpect(jsonPath("$.lastName").value("Kotliński"))
                .andExpect(jsonPath("$.pesel").value("89081415271"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(3)
    public void shouldSearchByName() throws Exception {
        mockMvc.perform(get("/customers/search").param("firstName", "Kamil").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers").exists())
                .andExpect(jsonPath("$.customers[0].pesel").value("89081415271"))
                .andExpect(jsonPath("$.customers[1].pesel").value("84081415271"))
                .andDo(MockMvcResultHandlers.print());
    }

    private CustomerDto prepareCreateCustomerRequest(String fistName, String lastName, String pesel) {
        CustomerDto customerDto = new CustomerDto();
        customerDto.setFirstName(fistName);
        customerDto.setLastName(lastName);
        customerDto.setPesel(pesel);
        return customerDto;
    }

    private static Stream<Arguments> createCustomersData() {
        return Stream.of(
                Arguments.of("Robert", "Burek", "87081415271", "1"),
                Arguments.of("Kamil", "Kotliński", "89081415271", "2"),
                Arguments.of("Kamil", "Lasek", "84081415271", "3")
        );
    }
}
