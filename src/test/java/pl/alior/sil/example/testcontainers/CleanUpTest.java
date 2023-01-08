package pl.alior.sil.example.testcontainers;

import com.github.dockerjava.api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.rnorth.ducttape.ratelimits.RateLimiter;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

@Slf4j
public class CleanUpTest implements BeforeAllCallback {
    static final String ACKNOWLEDGMENT = "ACK";
    private static final RateLimiter RYUK_ACK_RATE_LIMITER = RateLimiterBuilder
            .newBuilder()
            .withRate(4, TimeUnit.SECONDS)
            .withConstantThroughput()
            .build();

    private static final Map<String, String> MARKER_LABELS = Collections.singletonMap(
            DockerClientFactory.TESTCONTAINERS_SESSION_ID_LABEL,
            DockerClientFactory.SESSION_ID
    );

    static final List<Map.Entry<String, String>> DEATH_NOTE = Stream
            .concat(DockerClientFactory.DEFAULT_LABELS.entrySet().stream(), MARKER_LABELS.entrySet().stream())
            .<Map.Entry<String, String>>map(it -> new AbstractMap.SimpleEntry<>("label", it.getKey() + "=" + it.getValue()))
            .collect(Collectors.toList());

    //    @Override
    public void beforeAll(ExtensionContext context) {
        String uniqueKey = this.getClass().getName();
        Object value = context.getRoot().getStore(GLOBAL).get(uniqueKey);
        if (value == null) {
            context.getRoot().getStore(GLOBAL).put(uniqueKey, this);
            startRyukContainer();
        }
    }


    public void startRyukContainer() {
        GenericContainer<?> ryukContainer = new GenericContainer<>("testcontainers/ryuk:0.3.4")
                .withReuse(true)//don't register labels in resource reaper
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

        CountDownLatch ryukScheduledLatch = new CountDownLatch(1);

        String host = ryukContainer.getHost();
        Integer ryukPort = ryukContainer.getFirstMappedPort();

        Thread kiraThread = new Thread(
                DockerClientFactory.TESTCONTAINERS_THREAD_GROUP,
                () -> {
                    while (true) {
                        RYUK_ACK_RATE_LIMITER.doWhenReady(() -> {
                            int index = 0;
                            try (Socket clientSocket = new Socket()) {
                                clientSocket.connect(new InetSocketAddress(host, ryukPort), 5 * 1000);
                                BufferedReader ryukInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                synchronized (DEATH_NOTE) {
                                    while (true) {
                                        if (index >= 1) {
                                            try {
                                                DEATH_NOTE.wait(1_000);
                                            } catch (InterruptedException ex) {
                                                throw new RuntimeException(ex);
                                            }
                                        } else {
                                            boolean isAcknowledged = register(DEATH_NOTE, clientSocket.getOutputStream(), ryukInput);
                                            if (isAcknowledged) {
                                                log.debug("Received 'ACK' from Ryuk");
                                                ryukScheduledLatch.countDown();
                                                index++;
                                            } else {
                                                log.debug("Didn't receive 'ACK' from Ryuk. Will retry to send filters.");
                                            }
                                        }

                                    }
                                }
                            } catch (Exception e) {
                                throw new IllegalStateException("Could not connect to Ryuk at " + host + ":" + ryukPort, e);
                            }
                        });
                    }
                }
        );

        kiraThread.setDaemon(true);
        kiraThread.start();
        try {
            if (!ryukScheduledLatch.await(TestcontainersConfiguration.getInstance().getRyukTimeout(), TimeUnit.SECONDS)) {
                log.error("Timed out waiting for Ryuk container to start. Ryuk's logs:\n{}", ryukContainer.getLogs());
                throw new IllegalStateException(String.format("Could not connect to Ryuk at %s:%s", host, ryukPort));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean register(List<Map.Entry<String, String>> filters, OutputStream out, BufferedReader in) throws IOException {
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
        return waitForAcknowledgment(in);
    }

    private static boolean waitForAcknowledgment(BufferedReader in) throws IOException {
        String line = in.readLine();
        while (line != null && !ACKNOWLEDGMENT.equalsIgnoreCase(line)) {
            line = in.readLine();
        }
        return ACKNOWLEDGMENT.equalsIgnoreCase(line);
    }
}
