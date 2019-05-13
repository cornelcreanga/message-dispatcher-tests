package com.ccreanga.history;

import com.ccreanga.history.outgoing.history.HistoryServer;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@AllArgsConstructor
@ComponentScan("com.ccreanga")
public class ServerRun implements CommandLineRunner {

    private HistoryServer historyServer;

    @Override
    public void run(String... args) throws Exception {
        Thread thread3 = new Thread(historyServer);
        thread3.start();


    }

    public static void main(String[] args) {
        SpringApplication.run(ServerRun.class, args);
    }
}
