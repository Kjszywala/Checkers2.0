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
    static final int PORT_CHAT = 6624;
    private static LinkedList<Object> list;
    private static int playerID = 0;
    private static int maxPlayer = 2;
    private boolean whosTurn;
    static Socket p1Socket;
    static Socket p2Socket;
    static Socket p1SocketChat;
    static Socket p2SocketChat;
    static Socket socket = null;
    static Socket socket2 = null;
    /**
     * Function acceptConnection() - accepting connection of both players,
     * increasing playerID number if there is connection. Starting threads when
     * there is two players ready to play. Finally closing the connections.
     */
    @SuppressWarnings("unchecked")
    public void acceptConnection(){
        ServerSocket s = null;
        ServerSocket ss = null;
        try {
            s = new ServerSocket(PORT);
            ss = new ServerSocket(PORT_CHAT);
            System.out.println("===Server Started===\nWaiting for players...");
            while (playerID<=maxPlayer) {
                socket = s.accept();
                socket2 = ss.accept();
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                playerID++;
                out.writeInt(playerID);
                if(playerID==1){
                    p1Socket = socket;
                    p1SocketChat = socket2;
                } else {
                    p2Socket = socket;
                    p2SocketChat = socket2;
                    Thread thread1 = new ReadWriteDataFromClient(p1Socket,p2Socket,whosTurn);
                    thread1.start();
                    Thread thread2 = new ReadWriteDataFromClient(p2Socket,p1Socket,whosTurn);
                    thread2.start();
                    Thread thread3 = new ReadWriteStringFromTheClient(p1SocketChat, p2SocketChat);
                    thread3.start();
                    Thread thread4 = new ReadWriteStringFromTheClient(p2SocketChat, p1SocketChat);
                    thread4.start();
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
        boolean whosTurn;
        
        public ReadWriteDataFromClient(Socket s1, Socket s2, boolean whosTurn) {
            this.s1 = s1;
            this.s2 = s2;
            this.whosTurn = whosTurn;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            while(true){
                try{
                    InputStream inputStream = s1.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    list = (LinkedList<Object>)objectInputStream.readObject();
                    whosTurn = objectInputStream.readBoolean();
                    
                    OutputStream outputStream = s2.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(list);
                    objectOutputStream.writeBoolean(whosTurn);
                    objectOutputStream.flush();
                }catch(IOException e){
                    System.out.println("IOException from ReadWriteDataFromClient");
                    e.printStackTrace();
                    break;
                }catch(ClassNotFoundException ex){
                    System.out.println("ClassNotFoundException from ReadWriteDataFromClient");
                    break;
                }
            }
        }   
    }
    private class ReadWriteStringFromTheClient extends Thread {
        
        Socket s1;
        Socket s2;
        BufferedReader in;
        PrintWriter out;
        String line;

        public ReadWriteStringFromTheClient(Socket s1, Socket s2) {
            this.s1 = s1;
            this.s2 = s2;
        }
        
        @Override
        public void run(){
            try{
                in = new BufferedReader(
                        new InputStreamReader(s1.getInputStream()));
                out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(
                        s2.getOutputStream())),true);
            while(true){
                line = in.readLine();
                out.println(line);
            }
            }catch(IOException e){ 
                System.out.println("IOException in ReadWriteStringFromTheClient run()");
                e.printStackTrace();
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