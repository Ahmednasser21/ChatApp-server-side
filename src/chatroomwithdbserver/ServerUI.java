package chatroomwithdbserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class ServerUI extends AnchorPane {

    protected final Button btn_start;
    protected final Button btn_stop;
    Dao dao;
    ServerSocket serverSocket;
    Socket socket;
    Thread t;
    volatile boolean isThreadStarted;
    ClientHandler client;
    String message;

    public ServerUI() {
        btn_start = new Button();
        btn_stop = new Button();
        setId("AnchorPane");
        setPrefHeight(750.0);
        setPrefWidth(900.0);

        btn_start.setId("start_server_btn");
        btn_start.setLayoutX(246.0);
        btn_start.setLayoutY(303.0);
        btn_start.setMnemonicParsing(false);
        btn_start.setOnAction(this::handleOnBtnStart);
        btn_start.setText("Start Server");

        btn_stop.setId("stop_server_btn");
        btn_stop.setLayoutX(521.0);
        btn_stop.setLayoutY(303.0);
        btn_stop.setMnemonicParsing(false);
        btn_stop.setOnAction(this::handleOnBtnStop);
        btn_stop.setText("Stop Server");

        getChildren().add(btn_start);
        getChildren().add(btn_stop);

        isThreadStarted = false;
    }

    protected void handleOnBtnStart(javafx.event.ActionEvent actionEvent) {
        if (!isThreadStarted) {
            System.out.println("Starting the server");
            if (t == null || !t.isAlive()) {
                try {
                    serverSocket = new ServerSocket(6004);
                    t = new Thread() {
                        @Override
                        public void run() {
                            while (!Thread.currentThread().isInterrupted() && !serverSocket.isClosed()) {
                                try {
                                    socket = serverSocket.accept();
                                    System.out.println("New client connected");
                                    new ClientHandler(socket);
                                    
                                } catch (IOException ex) {
                                    if (!Thread.currentThread().isInterrupted()) {
                                        Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, "Error accepting client", ex);
                                    }
                                }
                            }
                        }
                    };
                    t.start();
                    isThreadStarted = true;
                    btn_start.setDisable(true);
                    btn_stop.setDisable(false);
                } catch (IOException ex) {
                    Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, "Failed to start server", ex);
                }
            }
        }
    }
    
    

    

    protected void handleOnBtnStop(javafx.event.ActionEvent actionEvent) {
        if (isThreadStarted && t != null && t.isAlive()) {
            System.out.println("Stopping the server");
            t.interrupt();
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                isThreadStarted = false;
                btn_start.setDisable(false);
                btn_stop.setDisable(true);
                System.out.println("Server has stopped");
            } catch (IOException e) {
                Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, "Error stopping server", e);
            }
        }
    }
}
