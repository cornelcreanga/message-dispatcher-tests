package com.ccreanga.history;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ServerConfig {

    @Value("${serverConfig.history.port}")
    private int historyPort;


}
