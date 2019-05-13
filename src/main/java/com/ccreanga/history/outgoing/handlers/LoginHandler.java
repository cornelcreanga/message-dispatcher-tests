package com.ccreanga.history.outgoing.handlers;


import com.ccreanga.history.CurrentSession;
import com.ccreanga.history.Customer;
import com.ccreanga.history.CustomerSessionStatus;
import com.ccreanga.history.gateway.CustomerStorage;
import com.ccreanga.protocol.outgoing.MessageIO;
import com.ccreanga.protocol.outgoing.client.LoginMsg;
import com.ccreanga.protocol.outgoing.server.InfoMsg;
import com.google.common.util.concurrent.Striped;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@AllArgsConstructor
public class LoginHandler {

    private static Striped<ReadWriteLock> stripedLock = Striped.lazyWeakReadWriteLock(100);

    private CustomerStorage customerStorage;

    private CurrentSession currentSession;

    public Optional<Customer> handle(Socket socket, LoginMsg message) throws IOException {

        Customer customer;
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();



        InfoMsg resultMessage;
        String name = message.getName();
        log.info("ClientLoginMessage {}", name);

        Lock customerLock = stripedLock.get(name).readLock();//prevent concurrent login for the same customers
        customerLock.lock();

        try {
            Set<Customer> customers = customerStorage.getCustomers();
            Optional<Customer> optional = customers.stream().filter(c -> c.getName().equals(name)).findAny();
            if (optional.isEmpty()) {
                log.info("Not authorized");
                MessageIO.serializeServerMsg(new InfoMsg(InfoMsg.UNAUTHORIZED),out);
                return Optional.empty();
            }
            customer = optional.get();
            CustomerSessionStatus status = currentSession.login(customer, socket);
            if (status.isAlreadyLoggedIn()) {
                resultMessage = new InfoMsg(InfoMsg.ALREADY_AUTHENTICATED);
                log.info("Already authorized.");
            } else {
                log.info("Authorized");
                resultMessage = new InfoMsg(InfoMsg.AUTHORIZED);
            }

            MessageIO.serializeServerMsg(resultMessage,out);
            return optional;
        } catch (Exception e) {
            throw new RuntimeException(e);//todo

        } finally {
            customerLock.unlock();
        }
    }

}
