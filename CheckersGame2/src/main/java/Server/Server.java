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
            System.out.println("===Server Started===\nWaiting for players...");
            while (playerID<=maxPlayer) {
                socket = s.accept();
                playerID++;
                System.out.println("New Payer x" +playerID);
                if(playerID==1){
                    p1Socket = socket;
                } else {
                    p2Socket = socket;
                    Thread thread1 = new ReadWriteDataFromClient(p1Socket,p2Socket);
                    thread1.start();
                    Thread thread2 = new ReadWriteDataFromClient(p2Socket,p1Socket);
                    thread2.start();
                }
                System.out.println("Player "+playerID+" is connected.");
            }
            s.close();
        } catch (IOException e) {
            System.out.println("IOException in acceptConnections");
        }finally {
            try {
                socket.close();
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
    private class ReadWriteDataFromClient extends Thread {
        
        Socket s1;
        Socket s2;

        public ReadWriteDataFromClient(Socket s1, Socket s2) {
            this.s1 = s1;
            this.s2 = s2;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            while(true){
                try{
                    InputStream inputStream = s1.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    list = (LinkedList<Object>)objectInputStream.readObject();

                    OutputStream outputStream = s2.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(list);
                    objectOutputStream.flush();
                }catch(IOException e){
                    System.out.println("IOException from ReadWriteDataFromClient1");
                    break;
                }catch(ClassNotFoundException ex){
                    System.out.println("ClassNotFoundException from ReadWriteDataFromClient1");
                    break;
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