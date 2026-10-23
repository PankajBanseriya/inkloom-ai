package com.inkloom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InkloomApplicationTests {

    @Test
    @DisplayName("Spring Application Context loads successfully")
    void contextLoads() {
        assertTrue(true, "Context loaded successfully");
    }
}
