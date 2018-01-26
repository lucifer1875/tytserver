package com.tyt.listener;

import com.tyt.service.ServerService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucifer on 2018/1/8.
 */
public class ServerListener {
    private static List<Socket> sockets = new ArrayList<>();

    public static void main(String[] args) {
        init();
    }

    public static void init (){
        try {
            ServerSocket serverSocket = new ServerSocket(8001);
            while (true){
                System.out.println("server is running ");
                Socket client = serverSocket.accept();
                if (-1 == client.getInetAddress().toString().indexOf("39.106.166.24")){

                    for (int i = 0; i < sockets.size(); i++) {
                        if (!sockets.get(i).getKeepAlive()){
                            sockets.remove(i);
                        }
                    }
                    sockets.add(client);
                }
                new ServerService(client, sockets);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
