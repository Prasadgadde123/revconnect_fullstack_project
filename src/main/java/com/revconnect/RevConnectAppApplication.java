package com.revconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class RevConnectAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RevConnectAppApplication.class, args);
	}

}
