package com.ccreanga.history.outgoing.history;

import static com.ccreanga.protocol.outgoing.client.ClientMsg.LOGIN;
import static com.ccreanga.protocol.outgoing.client.ClientMsg.LOGOUT;
import static com.ccreanga.protocol.outgoing.client.ClientMsg.HISTORICAL_DATA;
import static com.ccreanga.protocol.outgoing.server.InfoMsg.HISTORY_ALREADY_STARTED;
import static com.ccreanga.protocol.outgoing.server.InfoMsg.HISTORY_BAD_INTERVAL;

import com.ccreanga.history.Customer;
import com.ccreanga.history.outgoing.handlers.*;
import com.ccreanga.protocol.outgoing.MessageIO;
import com.ccreanga.protocol.outgoing.client.*;
import com.ccreanga.protocol.outgoing.server.InfoMsg;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class HistoryConnectionProcessor {

    private LoginHandler loginHandler;
    private LogoutHandler logoutHandler;
    private HistoryHandler historyHandler;

    public void handleConnection(Socket socket) throws Exception {
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        Customer customer = null;
        boolean historicalDataProcessing = false;
        //add it to register section
        while(true){
            Optional<ClientMsg> optional = MessageIO.deSerializeClientMsg(in);
            if (optional.isEmpty()){
                return;
            }
            ClientMsg msg = optional.get();
            log.trace("message type {}",msg.getType());
            switch (msg.getType()){
                case LOGIN:{
                    Optional<Customer> optionalCustomer = loginHandler.handle(socket,(LoginMsg) msg);
                    if (optionalCustomer.isPresent())
                        customer = optionalCustomer.get();
                    else{
                        throw new AuthorizationException();//todo
                    }
                    break;
                }
                case -1://socket close, logout
                case LOGOUT:{
                    logoutHandler.handle(socket,customer,(LogoutMsg)msg);
                    break;
                }
                case HISTORICAL_DATA:{
                    if (customer == null) {
                        log.info("no customer logged in, can't send data");
                        return;
                    } else {
                        if (historicalDataProcessing){
                            MessageIO.serializeServerMsg(new InfoMsg(HISTORY_ALREADY_STARTED),out);
                            break;
                        }
                        historicalDataProcessing = true;
                        HistoryDataMsg message = (HistoryDataMsg)msg;
                        if ((message.getStartTimestamp()<=0) || (message.getEndTimestamp()<message.getStartTimestamp())){
                            MessageIO.serializeServerMsg(new InfoMsg(HISTORY_BAD_INTERVAL),out);
                            break;
                        }
                        historyHandler.handle(customer.getName(),socket,message);
                        return;//close connection
                    }
                }
                default:{
                    throw new MalformedException("invalid message type " + msg.getType(), "BAD_MESSAGE_TYPE");
                }
            }
            out.flush();

        }
    }

}
