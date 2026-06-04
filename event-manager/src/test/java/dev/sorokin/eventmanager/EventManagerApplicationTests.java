package dev.sorokin.eventmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = IntegrationTestExtension.PostgresInitializer.class)
class EventManagerApplicationTests {

    @Test
    void contextLoads() {
    }

}
