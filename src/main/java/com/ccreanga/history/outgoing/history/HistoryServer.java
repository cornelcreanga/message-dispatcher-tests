package com.ccreanga.history.outgoing.history;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.ccreanga.history.ServerConfig;
import com.ccreanga.history.util.IOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HistoryServer implements Runnable {

    private ServerConfig serverConfig;

    private HistoryConnectionProcessor historyConnectionProcessor;

    private ServerSocket serverSocket = null;
    private boolean isStopped = false;

    public HistoryServer(ServerConfig serverConfig, HistoryConnectionProcessor historyConnectionProcessor) {
        this.serverConfig = serverConfig;
        this.historyConnectionProcessor = historyConnectionProcessor;
    }


    public void run() {
        ExecutorService threadPool = new ThreadPoolExecutor(
            64,
            128,
            60,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(128));


        try {
            serverSocket = new ServerSocket(serverConfig.getHistoryPort());
            log.info("history server started on {}", serverConfig.getHistoryPort());
            while (!isStopped) {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);

                try {
                    threadPool.execute(() -> {
                        try {
                            //keep the connection open unless close/not authorized
                            historyConnectionProcessor.handleConnection(socket);
                            IOUtil.closeSocketPreventingReset(socket);
                        } catch (Exception e) {
                            if (!e.getMessage().equals("Connection reset")) {
                                e.printStackTrace();
                            }
                        } finally {
                            IOUtil.closeSocketPreventingReset(socket);
                        }
                    });

                } catch (RejectedExecutionException e) {
                    IOUtil.closeSocketPreventingReset(socket);

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("Server Stopped.");
    }

    public synchronized void stop() {
        isStopped = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }
}
