package kh.edu.istad.platform.iam;

import kh.edu.istad.platform.iam.domain.entity.AccountActivityEventType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountActivityMigrationContractTest {
    @Test
    void forwardMigrationAllowsEveryApplicationActivityType() throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream("changelog/ddl/changelog-0016.sql")) {
            assertTrue(stream != null, "forward activity migration must be packaged");
            String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            for (AccountActivityEventType type : AccountActivityEventType.values()) {
                assertTrue(sql.contains("'" + type.name() + "'"),
                        () -> "activity event type is missing from database constraint: " + type);
            }
        }
    }
}
