package com.mycompany.checkersgame2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import javax.swing.*;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;
import javax.swing.text.DefaultCaret;
/**
 * Class NewGame responsible for load a new game for two players.
 * ActionListener and KeyListener added for chat.
 * @author Kamil
 */
public class NewGame implements ActionListener, KeyListener {
    /**
     * Variables needed to connect to the server and to set interface.
     * Two ports: one for sending list of objects and secend for chat.
     * Address of the server.
     * ObjectOutpuStream for sending the list of objects.
     * Two sockets - list and chat.
     * out and in to send and read the strings to chat.
     * playerID to know which player you are.
     * positionHistoryY needed to establish who's turn is it.
     */
    static final int PORT = 6623;
    static final int PORT_CHAT = 6624;
    protected String Address = "192.168.1.186";
    ObjectOutputStream objectOutputStream;
    protected InetAddress iAdres = null;
    protected Socket socket = null; 
    protected Socket socketChat = null;
    protected ServerSocket serverSocket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected static int playerID;
    protected int positionHistoryY;
    protected boolean whosTurn;
    private final String bColour = "#a85a32";
    JFrame frame = new JFrame();
    JTextArea textArea = new JTextArea(10,29);
    JTextField textField = new JTextField(20);
    JButton sendButton = new JButton("Send"); 
    JPanel board=new JPanel();
    private JScrollPane scroll;
    
    /**
     * Linked list of our pawns.
     */
    public static LinkedList<Checker> checkers = new LinkedList();
    public static Checker selectedChecker = null;

    public NewGame(){
        /**
         * Connecting with the server.
         */
        this.connect();
        frame.setTitle("Checkers V1.0 Player: " + playerID + ".");
        int size = 90;
        /**
         * Creating the pawns
         */
        createPawns();
        /**
         * Panel with the checkers board and pawns.
         * Adding the pawns as an objects.
         */
        JPanel checkersBoard = new JPanel(new BorderLayout());
        board=new JPanel(){
            @Override
            public void paint(Graphics g) {
            boolean white=true;
            for(int y = 0; y < 8; y++){
                for(int x = 0; x < 8; x++){
                    if(white){
                        g.setColor(Color.WHITE);
                    }else{
                        g.setColor(Color.BLACK);
                    }
                    g.fillRect(x*100, y*100, 100, 100);
                    white=!white;
                }
                white=!white;
                }
                for(Checker c: checkers) {
                    if(c.white) {
                        g.setColor(Color.decode("#c7c4c3"));
                    } else {
                        g.setColor(Color.decode("#803f1f"));
                    }
                    g.fillOval(c.x, c.y, size, size);
                }
            }
        };
        frame.add(checkersBoard);
        board.setOpaque(true);
        checkersBoard.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        checkersBoard.add(board,BorderLayout.CENTER);
        board.setOpaque(false);
        checkersBoard.setBackground(Color.decode(bColour));
        board.setBackground(Color.decode(bColour));
        
        JPanel eastPanel = new JPanel(new BorderLayout());
        textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        textArea.setEditable(false);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        scroll = new JScrollPane(textArea);
        /**
         * Set the textArea always scroll to bottom.
         */
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        checkersBoard.add(eastPanel, BorderLayout.EAST);
        eastPanel.setBackground(Color.decode(bColour));
        eastPanel.add(scroll, BorderLayout.CENTER);
        
        JPanel eastBottomPanel = new JPanel(new FlowLayout());
        eastPanel.add(eastBottomPanel,BorderLayout.SOUTH);
        eastBottomPanel.add(textField);
        eastBottomPanel.add(sendButton);
        eastBottomPanel.setBackground(Color.decode(bColour));
        sendButton.addActionListener(this);
        textField.addActionListener(this);
        textField.addKeyListener(this);
        /**
         * Starting the thread which reading data from server.
         * Starting the thread which repainting the frame.
         */
        Thread thread = new RefreshBoard(this);
        thread.start();
        Thread thread1 = new ReadDataFromServer(socket);
        thread1.start();
        Thread thread2 = new ReadStringFromServer();
        thread2.start();
        
        frame.setUndecorated(false);
        frame.setSize(1200,900);
        frame.setContentPane(checkersBoard);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(HIDE_ON_CLOSE);
        /**
         * Mouse motion listener, mouseDragged function has been used to move
         * checkers.
         */
        frame.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                try{
                    if(selectedChecker != null){
                        selectedChecker.x = e.getX()-75;
                        selectedChecker.y = e.getY()-89;
                        frame.repaint();
                    }
                }catch(NullPointerException ex){
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
        /**
         * Mouse listener
         * mousePressed - function is responsible for getting the position x,y
         * of the pawn. Depends from whosTurn, deciding which player should move.
         * mouseReleased - function is responsible for setting the pawn on the right
         * position. Then method sending the data(our list of pawns) to server.
         * At the end it is repainting our frame.
         */
        frame.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            
            @Override

            public void mousePressed(MouseEvent e) {
                try{
                    positionHistoryY = getChecker(e.getX()-40,e.getY()-40).positionY;
                    if(playerID == 1 && getChecker(e.getX()-40,e.getY()-40).white==true && whosTurn==false){
                        selectedChecker = getChecker(e.getX()-40,e.getY()-40);
                    }
                    if(playerID == 2 && getChecker(e.getX()-40,e.getY()-40).white==false && whosTurn==true){
                        selectedChecker = getChecker(e.getX()-40,e.getY()-40);
                        
                    }
                }catch(NullPointerException ex){
                }
            }
            /**
             * Moving the pawn when mouse released. Setting whosTurn to !whosTurn
             * so the other player can make a move. Sending our list, whosTurn to
             * the server.
             */
            @Override
            @SuppressWarnings("unchecked")
            public void mouseReleased(MouseEvent e) {
                try{
                    selectedChecker.Move(e.getX()/100, e.getY()/100);
                    if(positionHistoryY != selectedChecker.positionY){
                        if(whosTurn == false){
                            whosTurn = true;
                        }
                        else {
                            whosTurn = false;
                        }
                    }
                    selectedChecker = null;
                    OutputStream outputStream = socket.getOutputStream();
                    objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(checkers);
                    objectOutputStream.writeBoolean(whosTurn);
                    objectOutputStream.flush();
                    frame.repaint();
                }catch(NullPointerException ex){
                }catch(IOException exc){
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }
    /**
     * Create pawns method
     */
    public void createPawns(){
        Checker wc1 = new Checker(0,7,true,checkers,false);
        Checker wc2 = new Checker(2,7,true,checkers,false);
        Checker wc3 = new Checker(4,7,true,checkers,false);
        Checker wc4 = new Checker(6,7,true,checkers,false);
        Checker wc5 = new Checker(1,6,true,checkers,false);
        Checker wc6 = new Checker(3,6,true,checkers,false);
        Checker wc7 = new Checker(5,6,true,checkers,false);
        Checker wc8 = new Checker(7,6,true,checkers,false);
        Checker wc9 = new Checker(0,5,true,checkers,false);
        Checker wc10 = new Checker(2,5,true,checkers,false);
        Checker wc11 = new Checker(4,5,true,checkers,false);
        Checker wc12 = new Checker(6,5,true,checkers,false);
        
        Checker bc1 = new Checker(1,0,false,checkers,false);
        Checker bc2 = new Checker(3,0,false,checkers,false);
        Checker bc3 = new Checker(5,0,false,checkers,false);
        Checker bc4 = new Checker(7,0,false,checkers,false);
        Checker bc5 = new Checker(0,1,false,checkers,false);
        Checker bc6 = new Checker(2,1,false,checkers,false);
        Checker bc7 = new Checker(4,1,false,checkers,false);
        Checker bc8 = new Checker(6,1,false,checkers,false);
        Checker bc9 = new Checker(1,2,false,checkers,false);
        Checker bc10 = new Checker(3,2,false,checkers,false);
        Checker bc11 = new Checker(5,2,false,checkers,false);
        Checker bc12 = new Checker(7,2,false,checkers,false);
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }
    /**
     * Keyboard event which letting you to use enter in chat.
     * @param e 
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == e.VK_ENTER){
            sendMessage();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    /**
     * Connecting with the server. Two sockets needed for pawns and chat.
     */
    void connect() {
        try {
            iAdres = InetAddress.getByName(Address);
        } catch (Exception e) { 
            System.exit(0); 
        }
        try {
            socket = new Socket(iAdres, PORT);
            socketChat = new Socket(iAdres,PORT_CHAT);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            textArea.append("Connected\n");
            playerID = input.readInt();
            textArea.append("You are player: "+playerID);
        } catch (IOException e) {
            textArea.append("Not connected\n");
        }
    }
    /**
     * When button 'Send' pressed, sending the output from textField to server.
     */
    private void sendMessage(){
        String line = textField.getText();
        textArea.append("\nPlayer "+playerID+": "+line);
        try{
            out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(
                        socketChat.getOutputStream())),true);
            out.println("Player "+playerID+": "+line);
            textField.setText("");
        }catch(IOException e){
            System.out.println("IOException in sendMessage()");
        }
    }
    /**
     * Sending textField output to server.
     * @param g 
     */
    @Override
    public void actionPerformed(ActionEvent g) {
        
        switch(g.getActionCommand()){
            case "Send" -> sendMessage();
        }
    }
    /**
     * Show window if player win or lose.
     */
    public void checkIfPlayerWin(){
        
        int checkWinCounterWhite = 0;
        int checkWinCounterBrown = 0;
        
        for(Checker c: checkers){
            if(c.white == true){
                checkWinCounterWhite+=1;
            } else {
                checkWinCounterBrown+=1;
            } 
        }
        if(checkWinCounterWhite == 0 && playerID == 1){
            JOptionPane.showMessageDialog(
                    null, 
                    "Sorry!!! You lost.", 
                    "InfoBox: Looser", 
                    JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            RefreshBoard.currentThread().stop();
        }
        if(checkWinCounterBrown == 0 && playerID == 2){
            JOptionPane.showMessageDialog(
                    null, 
                    "Sorry!!! You lost.", 
                    "InfoBox: Looser", 
                    JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            RefreshBoard.currentThread().stop();
        }
        if(checkWinCounterWhite == 0 && playerID == 2){
            JOptionPane.showMessageDialog(
                    null, 
                    "Congratulations!!! You won.", 
                    "InfoBox: Winner", 
                    JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            RefreshBoard.currentThread().stop();
        }
        if(checkWinCounterBrown == 0 && playerID == 1){
            JOptionPane.showMessageDialog(
                    null, 
                    "Congratulations!!! You won.", 
                    "InfoBox: Winner", 
                    JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
            RefreshBoard.currentThread().stop();
        }
    }
    
    /**
     * Getting the checker position x and y.
     * @param x
     * @param y
     * @return 
     */
    public static Checker getChecker(int x, int y){
        int xposition = x/100;
        int yposition = y/100;
        for(Checker c: checkers){
            if(c.positionX==xposition && c.positionY==yposition){
                return c;
            }
        }
        return null;
    }
     /**
     * Thread that is needed for repainting the checkers board.
     */
    private class RefreshBoard extends Thread {
        
        NewGame dialog;

        public RefreshBoard(NewGame dialog) {
            this.dialog = dialog;
        }
        
        @Override
        public void run(){
            while(true){
                try{
                    dialog.frame.repaint();
                    checkIfPlayerWin();
                } catch(NullPointerException e){  
                }
            }
        }
    }
    /**
     * Reading output from server and appending it to chat.
     */
    private class ReadStringFromServer extends Thread {
        
        String line;
        
        @Override
        public void run(){
            try{
                in = new BufferedReader(
                    new InputStreamReader(socketChat.getInputStream()));
                while(true){
                    line = in.readLine();
                    textArea.append("\n"+line);
                }
            }catch(IOException e){
                System.out.println("IOexception in ReadStringFromServer run()");
            }
        }
    }
    /**
     * Thread class which reading data(our list of pawns) from server.
     */
    private class ReadDataFromServer extends Thread {
        
        Socket s;

        public ReadDataFromServer(Socket s) {
            this.s = s;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void run(){
            while(true){
                try{
                    InputStream inputStream = s.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    checkers = (LinkedList<Checker>)objectInputStream.readObject();
                    whosTurn = objectInputStream.readBoolean();
                }catch(IOException e){
                    System.out.println("IOException in ReadDataFromServer");
                    break;
                }catch(ClassNotFoundException ex){
                    System.out.println("ClassNotFoundException in ReadDataFromServer");
                    break;
                }
            }
        }
    }
} 


