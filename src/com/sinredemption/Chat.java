package com.sinredemption;

import java.util.Scanner;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Chat extends Thread{
    private DatagramSocket socket;
    private String username;
    private InetAddress targetIp;
    private int port;
    private byte[] buf = new byte[1024];;

    public class Receiver extends Thread{

        public void run(){
            while (true){
                try{
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println(received);
                }catch (Exception e){
                    System.err.println(e);
                }
            }
        }
    }

    public class Sender extends Thread{

        public void run(){
            Scanner console = new Scanner(System.in);
            while(true) {
                try {

                    String tmp = console.nextLine();
                    if(tmp.charAt(0) == '@'){
                        String[] cmd = tmp.split(" ", 3);
                        if(cmd[0].equals("@quit")) {
                            System.exit(1);
                        } else if(cmd[0].equals("@name"))
                            username = cmd[1];
                        else HelpMsg();
                    }else
                        sendMessage(tmp);
                }catch (Exception e){
                    System.err.println(e.getCause());
                }
            }
        }
    }

    private void init(){
        Thread receiver = new Thread(new Receiver(), "Message Receiver");
        Thread sender = new Thread(new Sender(), "Message Sender");
        receiver.start();
        sender.start();
        HelpMsg();
    }

    public Chat(int p)  {
        try {
            this.socket = new DatagramSocket(p);
            this.username = "Server";
            System.out.println("Waiting for client...");
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            targetIp = packet.getAddress();
            port = packet.getPort();
            System.out.println("Connected...");
            init();
        }catch (Exception e){
            System.err.println(e);
        }
    }

    public Chat(InetAddress ip, int p){
        try {
            this.socket = new DatagramSocket();
            this.port = p;
            this.targetIp = ip;
            this.username = "Client";
            sendMessage("");
            init();
        }catch (Exception e){
            System.err.println(e);
        }
    }

    private void sendMessage(String in){
        String msg = username + ": " + in;
        byte[] tmp = msg.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(tmp, tmp.length, targetIp, port);
        try {
            socket.send(packet);
        }catch (Exception e){
            System.err.println(e);
        }
    }

    public void HelpMsg(){
        System.out.println("@name <Username> - Change username.\n@quit - Quit chat.");
    }

}
