package com.psp.cbt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This is the entry point of the whole application.
// Running this class starts an embedded web server (Tomcat)
// on port 8080 and wires up everything else automatically.
@SpringBootApplication
public class CbtApplication {
    public static void main(String[] args) {
        SpringApplication.run(CbtApplication.class, args);
    }
}
