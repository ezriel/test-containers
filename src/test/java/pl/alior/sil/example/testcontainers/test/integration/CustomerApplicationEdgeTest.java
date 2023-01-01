package pl.alior.sil.example.testcontainers.test.integration;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class CustomerApplicationEdgeTest {

    @Autowired
    private MockMvc mockMvc;

    static Network network = Network.newNetwork();
    @Container
    static ToxiproxyContainer toxiContainer = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0").withNetwork(network);

    @Container
    static JdbcDatabaseContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:14.5")
            .withInitScript("db/init.sql")
            .withNetwork(network)
            .withExposedPorts(5432)
            .withNetworkAliases("postgres");

    static ToxiproxyClient toxiproxyClient;
    static Proxy proxy;

    @DynamicPropertySource
    static void setUp(DynamicPropertyRegistry registry) throws Exception {
        registry.add("spring.flyway.enabled", () -> "false");
        Startables.deepStart(postgresContainer, toxiContainer).join();
        System.out.println("Postgres URL: " + postgresContainer.getJdbcUrl());
        System.out.println("Postgres Mapped Port: " + postgresContainer.getFirstMappedPort());
        System.out.println("Postgres Exposed Port: " + postgresContainer.getExposedPorts());
        System.out.println("Postgres Host: " + postgresContainer.getHost());
        System.out.println("Toxi Host: " + toxiContainer.getHost());
        System.out.println("Toxi Control Port: " + toxiContainer.getControlPort());
        Integer proxyPort = toxiContainer.getMappedPort(8666);
        System.out.println("proxyPort: " + proxyPort);

        toxiproxyClient = new ToxiproxyClient(toxiContainer.getHost(), toxiContainer.getControlPort());
        proxy = toxiproxyClient.createProxy("postgres", "0.0.0.0:8666", "postgres:5432");

        registry.add("datasource.mwapp.jdbc-url", () -> "jdbc:postgresql://" + toxiContainer.getHost() + ":" + proxyPort + "/test?loggerLevel=OFF");
        registry.add("datasource.mwapp.username", postgresContainer::getUsername);
        registry.add("datasource.mwapp.password", postgresContainer::getPassword);
    }

    @Test
    void shouldDbBeUnavailableWhenSearchingCustomers() throws Exception {

//        proxy.toxics()
//                .latency("latency", ToxicDirection.DOWNSTREAM, 2_100)
//                .setJitter(100);

        //proxy.toxics().bandwidth("CUT_CONNECTION_DOWNSTREAM", ToxicDirection.DOWNSTREAM, 0);
        //proxy.toxics().bandwidth("CUT_CONNECTION_UPSTREAM", ToxicDirection.UPSTREAM, 0);

//        proxy.toxics().timeout("TIMEOUT", ToxicDirection.UPSTREAM, 5_000);
        proxy.toxics().resetPeer("RESET", ToxicDirection.UPSTREAM, 1_000);

        mockMvc.perform(get("/customers/search").param("firstName", "Kamil").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("100100"))
                .andExpect(jsonPath("$.msg").value("Unable to search customers"))
                .andDo(MockMvcResultHandlers.print());
    }
}
