package Server;

import java.io.*;
import java.net.*;
import java.util.*;
/**
 * Server class which handle connection between players.
 * @author kamil.szywala
 */
public class Server {
    /**
     * Variables needed to start the server.
     * LinkedList of objects which we going to read and send from and to both players.
     * playerID information which player you are.
     * maxPlayer is the integer of how many connections we can handle.
     * p1Socket,p2Socket players sockets.
     */
    static final int PORT = 6623;
    private static LinkedList<Object> list;
    private static int playerID = 0;
    private static int maxPlayer = 2;
    static Socket p1Socket;
    static Socket p2Socket;
    static Socket socket = null;
    /**
     * Function acceptConnection() - accepting connection of both players,
     * increasing playerID number if there is connection. Starting threads when
     * there is two players ready to play. Finally closing the connections.
     */
    @SuppressWarnings("unchecked")
    public void acceptConnection(){
        ServerSocket s = null;
        try {
            s = new ServerSocket(PORT);
            System.out.println("Server Started");
            while (playerID<=maxPlayer) {
                socket = s.accept();
                System.out.println("New Client");
                playerID++;
                if(playerID==1){
                    p1Socket = socket;
                } else {
                    p2Socket = socket;
                    Thread thread1 = new ReadWriteDataFromClient1();
                    thread1.start();
                    Thread thread2 = new ReadWriteDataFromClient2();
                    thread2.start();
                }
                System.out.println("Player "+playerID+" is connected.");
            }
        } catch (IOException e) {
            System.out.println("IOException in acceptConnections");
        }finally {
            try {
                socket.close();
                p1Socket.close();
                p2Socket.close();
                s.close();
            } catch (IOException e) {
                System.out.println("IOException in finnaly-acceptConnections");
            }
        }
    }
    /**
     * Thread class which reading and sending data from first player to second player.
     * Firstly it is reading list of pawns from player one and then sending it to
     * player two.
     */
    private class ReadWriteDataFromClient1 extends Thread {
        
        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            while(true){
                try{
                    InputStream inputStream = p1Socket.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    list = (LinkedList<Object>)objectInputStream.readObject();

                    OutputStream outputStream = p2Socket.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(list);
                }catch(IOException e){
                    System.out.println("IOException from ReadWriteDataFromClient1");
                }catch(ClassNotFoundException ex){
                    System.out.println("ClassNotFoundException from ReadWriteDataFromClient1");
                }
            }
        }   
    }
    /**
     * Thread class which reading and sending data from second player to first player.
     * Firstly it is reading list of pawns from player two and then sending it to
     * player one.
     */
    private class ReadWriteDataFromClient2 extends Thread {
        
        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            while(true){
                try{
                    InputStream inputStream = p2Socket.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    list = (LinkedList<Object>)objectInputStream.readObject();

                    OutputStream outputStream = p1Socket.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(list);
                }catch(IOException e){
                    System.out.println("IOException from ReadWriteDataFromClient2");
                }catch(ClassNotFoundException ex){
                    System.out.println("ClassNotFoundException from ReadWriteDataFromClient2");
                }
            }
        }   
    }
    /**
     * Main function - starting the server.
     * @param args 
     */
     public static void main(String[] args) {
        Server s = new Server();
        s.acceptConnection();
    }
}