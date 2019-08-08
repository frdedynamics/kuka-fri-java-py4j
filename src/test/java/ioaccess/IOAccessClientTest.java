package ioaccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IOAccessClientTest {

    @BeforeEach
    void setUp() {
        /*
        * set up
         */
    }

    @AfterEach
    void tearDown() {
        /*
        * tear down
         */
    }

    @Test
    void getTrueBool() {
        IOAccessClient client = new IOAccessClient();
        assertTrue(client.getTrueBool());
    }
}