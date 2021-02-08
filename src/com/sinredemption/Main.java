package com.sinredemption;

import java.net.InetAddress;

public class Main extends Thread{



    public static void main(String args[]) throws Exception {
        String ip = null;
        int port = 0;

        for (int i = 0; i < args.length; i++) {
            if(args[i].equals("-ip")) {
                ip = args[++i];
            }

            if(args[i].equals("-port")) {
                port = Integer.parseInt(args[++i]);
            }
        }
        if(port != 0 && ip != null) {
            new Chat(InetAddress.getByName(ip), port);
        }else if(port != 0){
            new Chat(port);
        }
    }
}