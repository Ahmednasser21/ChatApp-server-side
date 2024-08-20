/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatroomwithdbserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ahmed
 */
public class ClientHandler {

    DataInputStream reader;
    DataOutputStream writer;
    Socket socket;
    public static Vector<ClientHandler> clients = new Vector<>();
    static HashMap<String, ClientHandler> onlineUsers = new HashMap<>();
    Dao dao;
    String sender;
    String reciever;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        reader = new DataInputStream(socket.getInputStream());
        writer = new DataOutputStream(socket.getOutputStream());
        clients.add(this);
        readMessageFromClient();
        try {
            dao = Dao.getInstance();
        } catch (SQLException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendMessageToClient(String message) throws IOException {
        writer.writeUTF(message);
    }

    public void readMessageFromClient() throws IOException {
        new Thread(() -> {

            while (!socket.isClosed()) {
                try {
                    String message = reader.readUTF();
                    if (message.startsWith("signUp")) {
                        signUp(message);
                    } else if (message.startsWith("login")) {
                        login(message);
                    } else if (message.startsWith("request")) {
                        String[] receiverPlayer = message.split(",");
                        reciever = receiverPlayer[1];
                        onlineUsers.get(receiverPlayer[1]).writer.writeUTF("handleRequest" + "," + sender + "," + reciever);
                        System.out.println("receiver is " + receiverPlayer[1]);
                        System.out.println("sender is " + sender);
                    } else if ((message).equals("decline")) {
                        onlineUsers.get(sender).writer.writeUTF("request declined");
                    } else if (message.startsWith("accepted")) {
                        String[] str = message.split(",");
                        sender = str[1];
                        reciever = str[2];

                        System.out.println(sender + "senderrrrrr");
                        System.out.println(reciever + "receiverrrrrr");
                        onlineUsers.get(sender).writer.writeUTF("navigate");
                        onlineUsers.get(reciever).writer.writeUTF("navigate");
                    } else if (message.startsWith("message")) {

                        onlineUsers.get(sender).writer.writeUTF(message);
                        onlineUsers.get(reciever).writer.writeUTF(message);
                    }else if(message.equals("updateList")){
                        sendOnlinePlayers();
                        sendMessageToClient("updateList");
                    }else if(message.equals("signOut")){
                        onlineUsers.remove(sender);
                        dao.changeStatus(sender, false);
                        sendMessageToClient("singOutSuccessfully");
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        }
        ).start();
    }

    public void sendMessageToAllClients(String message) throws IOException {
        for (ClientHandler client : clients) {
            client.sendMessageToClient(message);
        }
    }

    public void signUp(String splittedMessage) {

        String[] receivedMessage = splittedMessage.split(",");

        if (receivedMessage[0].equals("signUp")) {
            try {

                if (dao.isUsernameExist(receivedMessage[1])) {
                    sendMessageToClient("alreadyExist");
                } else {

                    UserDto user = new UserDto(receivedMessage[1], receivedMessage[2], receivedMessage[3]);

                    try {
                        dao.insertUser(user);
                        System.out.println("inserted successfully " + user.getUsername());
                        sendMessageToClient("registeredSuccessfully");
                    } catch (SQLException ex) {
                        Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    public void login(String splittedMessage) {
        String[] receivedMessage = splittedMessage.split(",");
        sender = receivedMessage[1];
        if (receivedMessage[0].equals("login")) {
            try {
                if (dao.isUsernameAndPasswordExist(receivedMessage[1], receivedMessage[2])) {
                    writer.writeUTF("loginSuccess");
                    onlineUsers.put(receivedMessage[1], this);
                    dao.changeStatus(receivedMessage[1], true);
                    sendOnlinePlayers();
                } else {
                    writer.writeUTF("loginFail");
                }
            } catch (SQLException ex) {
                Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ServerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void sendOnlinePlayers() {
        try {

            for (String key : onlineUsers.keySet()) {

                if (key.equals(sender)) {
                    continue;
                } else {
                    writer.writeUTF("onlinePlayersStart" + "," + key);

                }

            }

        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
