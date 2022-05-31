package com.mycompany.checkersgame2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import javax.swing.*;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;
/**
 *
 * @author Kamil
 */
public class NewGame implements ActionListener {
    /**
     * Variables needed to connect to the server and to set interface.
     * 
     */
    static final int PORT = 6623;
    protected String Address = "10.101.109.132";
    ObjectOutputStream objectOutputStream;
    protected InetAddress iAdres = null;
    protected Socket socket = null; 
    protected ServerSocket serverSocket;
    protected PrintWriter out;
    private final String bColour = "#a85a32";
    JFrame frame = new JFrame("Checkers");
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
        
        int size = 90;
        /**
         * Creating the pawns
         */
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
        
        checkersBoard.add(eastPanel, BorderLayout.EAST);
        eastPanel.setBackground(Color.decode(bColour));
        eastPanel.add(textArea, BorderLayout.CENTER);
        
        JPanel eastBottomPanel = new JPanel(new FlowLayout());
        eastPanel.add(eastBottomPanel,BorderLayout.SOUTH);
        eastBottomPanel.add(textField);
        eastBottomPanel.add(sendButton);
        eastBottomPanel.setBackground(Color.decode(bColour));
        sendButton.addActionListener(this);
        textField.addActionListener(this);
        /**
         * Connecting with the server.
         * Starting the thread which reading data from server.
         * Starting the thread which repainting the frame.
         */
        this.connect();
        Thread thread1 = new ReadDataFromServer(socket);
        Thread thread = new RefreshBoard(this);
        thread.start();
        thread1.start();
        
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
                //selectedChecker.Move(e.getX(), e.getY());
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
         * of the pawn.
         * mouseReleased - function is responsible for setting the pawn on the right
         * position. Then this method sending the data(our list of pawns) to server.
         * At the end it is repainting our frame.
         */
        frame.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            
            @Override

            public void mousePressed(MouseEvent e) {
                selectedChecker = getChecker(e.getX()-40,e.getY()-40);
            }
            /**
             * Moving the pawn when mouse released.
             */
            @Override
            @SuppressWarnings("unchecked")
            public void mouseReleased(MouseEvent e) {
                try{
                    selectedChecker.Move(e.getX()/100, e.getY()/100);
                    OutputStream outputStream = socket.getOutputStream();
                    objectOutputStream = new ObjectOutputStream(outputStream);
                    objectOutputStream.writeObject(checkers);
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
     * Thread class which reading data(our list of pawns) from server.
     */
    private class ReadDataFromServer extends Thread {
        
        Socket s;

        public ReadDataFromServer(Socket s) {
            this.s = s;
        }
        
        @SuppressWarnings("unchecked")
        public void run(){
            while(true){
                try{
                    InputStream inputStream = s.getInputStream();
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                    checkers = (LinkedList<Checker>)objectInputStream.readObject();
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
    /**
     * Thread that is needed for repainting the checkers board.
     */
    private class RefreshBoard extends Thread {
        
        NewGame dialog;

        public RefreshBoard(NewGame dialog) {
            this.dialog = dialog;
        }
        
        public void run(){
            while(true){
                dialog.frame.repaint();
            }
        }
    }
    
    /**
     * Connecting with the server.
     */
    void connect() {
        try {
            iAdres = InetAddress.getByName(Address);
            textArea.append("Connecting to the address = " + iAdres + "\n");
        } catch (Exception e) { 
            System.exit(0); 
        }
        try {
            System.out.println(iAdres);
            socket = new Socket(iAdres, PORT);
            textArea.append("Connected\n");
            System.out.println(socket);
        } catch (IOException e) {
            textArea.append("Not connected\n");
            e.printStackTrace();
        }
        try {
            out = new PrintWriter(new BufferedWriter(
                                  new OutputStreamWriter(
                                  socket.getOutputStream())), true);
            System.out.println("Thread started");
        } catch (Exception e) { 
            textArea.append("Error: " + e); 
        }
    }
    /**
     * Closing connection with the server
     * @param g 
     */
    @Override
    public void actionPerformed(ActionEvent g) {
        out.println(textField.getText());
        if (textField.getText().equals("end")){
            try{
                out.close();
                socket.close();
            }catch(Exception e){
            }
                System.exit(0);
            }
        textField.setText("");
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
} 


