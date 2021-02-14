package com.sinredemption;



import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;



public class Chat extends Thread{
    private DatagramSocket socket;
    private String username;
    private InetAddress targetIp;
    private int port;
    private byte[] buf = new byte[1024];
    public class Receiver extends Thread{

        public void run(){
            while (true){
                try{
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    String[] cmd = received.split(" ", 3);
                    if(cmd[0].equals("<file>")){
                        Path filePath = Path.of(new File(cmd[1]).getName());
                        long filesize = Integer.parseInt(cmd[2]);
                        OutputStream writer = Files.newOutputStream(filePath);
                        for(int i = 0; i < filesize; i = i + buf.length){
                            socket.receive(packet);
                            writer.write(packet.getData(), 0, buf.length);
                        }
                        System.err.println("File received: " + filePath.getName(0));
                    }else
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
                    if(tmp.charAt(0) == '@' && !tmp.equals("\n")){
                        String[] cmd = tmp.split(" ", 3);
                        if(cmd[0].equals("@quit")) {
                            System.exit(1);
                        } else if(cmd[0].equals("@name"))
                            username = cmd[1];
                        else if(cmd[0].equals("@file")){
                            try {
                                Path filePath = Path.of(cmd[1]);
                                long fileSize = Files.size(filePath);
                                byte[] sendfile  = new String("<file> " + new File(cmd[1]).getName() + " "+ fileSize).getBytes();
                                DatagramPacket packet = new DatagramPacket(sendfile, sendfile.length, targetIp, port);
                                socket.send(packet);
                                InputStream reader = Files.newInputStream(filePath);
                                for(int i = 0; i < fileSize; i = i + buf.length){
                                    reader.read(buf, 0, buf.length);
                                    packet = new DatagramPacket(buf, buf.length, targetIp, port);
                                    socket.send(packet);
                                }
                            }catch (Exception e){
                                System.err.println(e.getCause());
                            }
                        }else HelpMsg();
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
