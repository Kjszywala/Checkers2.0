package com.mycompany.checkersgame2;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Kamil
 */
public class ClientThreadChat extends Thread {
    /*
        Stream for server listening
    */
    private BufferedReader in;
    /*
        New game window owner
    */
    public NewGame dialog;
        
    public ClientThreadChat(NewGame dialog) {
        this.dialog = dialog;
         // Associate the stream
        try {
            in = new BufferedReader(new InputStreamReader(
                                    dialog.socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
    String str=null;
    try {
        while ((str = in.readLine()) != null) {
            /**
             * Until there is no end of file - 
             * adding what is readed from the stream.
             */
            str = in.readLine();
            dialog.textArea.append(str + "\n");
            /**
             * To see last input in JTextArea
             */
            dialog.textArea.scrollRectToVisible(new Rectangle
                (0, dialog.textArea.getHeight()-2, 1, 1));
            dialog.textArea.repaint();
        }
        /**
         * Closing the stream.
         */
        in.close(); 
    } catch (IOException e) { 
        dialog.textArea.append("Error: " + e); 
    }
    } 
}

