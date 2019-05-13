package com.ccreanga.history;

import com.ccreanga.protocol.outgoing.server.ServerMsg;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSession {

    private Customer customer;
    private BlockingQueue<ServerMsg> messageQueues;
    private Socket socket;

}
