package com.ccreanga.history.outgoing.handlers;

import com.ccreanga.history.CurrentSession;
import com.ccreanga.history.Customer;
import com.ccreanga.protocol.outgoing.client.LoginMsg;
import com.ccreanga.protocol.outgoing.client.LogoutMsg;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class LogoutHandler {

    private CurrentSession currentSession;


    public void handle(Socket socket, Customer customer, LogoutMsg message) throws IOException {

        if (customer==null) {//ignore
            log.warn("no customer logged in for this connection, can't logout");
        } else {
            currentSession.logout(customer);
        }
    }

}
