package org.example.revconnectapp;

import com.revconnect.RevConnectAppApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = RevConnectAppApplication.class)
@ActiveProfiles("test")
class RevConnectAppApplicationTests {

    @Test
    void contextLoads() {
    }

}