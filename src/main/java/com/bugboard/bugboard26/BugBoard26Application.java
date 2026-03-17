package com.bugboard.bugboard26;

import com.bugboard.bugboard26.ui.SwingApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BugBoard26Application {

    public static void main(String[] args) {
        // DEVE stare qui, prima di tutto il resto
        System.setProperty("java.awt.headless", "false");

        SpringApplication app = new SpringApplication(BugBoard26Application.class);
        app.setHeadless(false);  // ← disabilita headless anche su Spring
        app.run(args);

        SwingApp.launch();
    }
}

