package ioaccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IOAccessClientTest {

    @BeforeEach
    void setUp() {
        /*
        * maybe set the time to something specific for testing
        */
    }

    @AfterEach
    void tearDown() {
        /*
        * stawp stuff
         */
    }

    @Test
    void getTrueBool() {
        IOAccessClient client = new IOAccessClient();
        assertTrue(client.getTrueBool());
    }
}