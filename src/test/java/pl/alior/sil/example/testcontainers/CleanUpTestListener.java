package pl.alior.sil.example.testcontainers;

import com.github.dockerjava.api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

@Slf4j
public abstract class CleanUpTestListener  {

    private static final Map<String, String> MARKER_LABELS = Collections.singletonMap(
            DockerClientFactory.TESTCONTAINERS_SESSION_ID_LABEL,
            DockerClientFactory.SESSION_ID
    );

    static final List<Map.Entry<String, String>> DEATH_NOTE = Stream
                            .concat(DockerClientFactory.DEFAULT_LABELS.entrySet().stream(), MARKER_LABELS.entrySet().stream())
                            .<Map.Entry<String, String>>map(it -> new AbstractMap.SimpleEntry<>("label", it.getKey() + "=" + it.getValue()))
                            .collect(Collectors.toList());

//    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        String uniqueKey = this.getClass().getName();
        Object value = context.getRoot().getStore(GLOBAL).get(uniqueKey);
        if (value == null) {
            context.getRoot().getStore(GLOBAL).put(uniqueKey, this);
//            startRyukContainer();
        }
    }

        static final GenericContainer<?> ryukContainer;
    static {

        ryukContainer = new GenericContainer<>("testcontainers/ryuk:0.3.4")
                .withExposedPorts(8080)
                .withCreateContainerCmdModifier(cmd -> {
                    cmd.withName("testcontainers-ryuk-" + DockerClientFactory.SESSION_ID);
                    cmd.withHostConfig(
                            cmd
                                    .getHostConfig()
                                    .withAutoRemove(true)
                                    .withPrivileged(TestcontainersConfiguration.getInstance().isRyukPrivileged())
                                    .withBinds(
                                            new Bind(
                                                    DockerClientFactory.instance().getRemoteDockerUnixSocketPath(),
                                                    new Volume("/var/run/docker.sock")
                                            )
                                    )
                                    .withPortBindings(new PortBinding(Ports.Binding.bindPort(8090), new ExposedPort(8080)))
                    );
                })
                .waitingFor(Wait.forLogMessage(".*Started.*", 1));

        ryukContainer.start();

        String host = ryukContainer.getHost();
        Integer ryukPort = ryukContainer.getFirstMappedPort();
        try (Socket clientSocket = new Socket()) {
            clientSocket.connect(new InetSocketAddress(host, ryukPort), 5 * 1000);
            register(DEATH_NOTE, clientSocket.getOutputStream());
        } catch (Exception e) {
            throw new IllegalStateException("Could not connect to Ryuk at " + host + ":" + ryukPort, e);
        }
    }

    private static void register(List<Map.Entry<String, String>> filters, OutputStream out) throws IOException {
        String query = filters
                .stream()
                .map(it -> {
                    return (
                            URLEncoder.encode(it.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(it.getValue(), StandardCharsets.UTF_8)
                    );
                })
                .collect(Collectors.joining("&"));

        log.info("Sending '{}' to Ryuk", query);
        System.out.println("Sending to Ryuk: " + query);
        out.write(query.getBytes());
        out.write('\n');
        out.flush();
    }
}
