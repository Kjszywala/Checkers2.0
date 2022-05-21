package Server;

import java.io.*;
import java.net.*;
import java.util.*;

//klasa w której będziemy pamiętali informacje o
//kolejnym kliencie łączącym się z serwerem
//w większej aplikacji klasa ta powinna być obszerniejsza

class OpisKlienta {
    PrintWriter wyjscie;
    String nazwa;
    
    OpisKlienta(String nazwa, PrintWriter wyjscie) {
        this.nazwa = nazwa;
        this.wyjscie = wyjscie;
    }

    //przesyłanie informacji do innego klienta
    synchronized void napiszDoMnie(String nadawca, String wiadomosc) {
        wyjscie.println(nadawca + ": " + wiadomosc);
    }
}

//klasa będąca wątkiem obsługującym konkretnego klienta
class ObslugaKlienta extends Thread {
    private Socket socket;
    private BufferedReader wejscie;
    private PrintWriter wyjscie;	
    private OpisKlienta opis;

    //kontener w którym zapamiętam informacje
    // o wszystkich aktywnych klientach
    //jako że jest static każdy klient ma do niego dostęp
    
    static HashSet<OpisKlienta> klienci = new HashSet<OpisKlienta>();    

    //konstruktor - przygotowanie odpowiednich strumieni
    public ObslugaKlienta(Socket socket) throws IOException {
        this.socket = socket;
        wejscie = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        wyjscie = new PrintWriter(
                    new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                            true); //z automatycznym opróżnianiem bufora
    }
    
    public void powiedzWszystkim(String nazwa, String wiadomosc){
//            Iterator iterator = klienci.iterator();
//            OpisKlienta nastepny;
//            while (iterator.hasNext()) {
//                nastepny = (OpisKlienta)iterator.next();
//                nastepny.napiszDoMnie(nazwa, wiadomosc);
//            }

          // albo po prostu tak
            for (OpisKlienta klient : klienci)
                klient.napiszDoMnie(nazwa, wiadomosc);
    }

    void wypiszKlientow() {
        Iterator iterator = klienci.iterator();
        OpisKlienta nastepny;
        wyjscie.println("People logged: " + klienci.size());

        while (iterator.hasNext()) {
            nastepny = (OpisKlienta) iterator.next();
            wyjscie.println(nastepny.nazwa);
        }
        wyjscie.println();
    }

    public void run() {
        //UWAGA: w tej funkcji powinno znaleźć się mnóstwo
        //oddzielnych bloków try-catch zapewniających poprawność
        //działania serwera. Obsługujących takie sytuacje jak np.
        //nieoczekiwane przerwanie połączenia z klientem.
        //Uproszczenie zastosowano z powodu chęci przedstawienia
        //zasad działania serwera w sposób najprostszy.
        String nazwaKlienta=null;
        try {
            //pytam o nazwę
            wyjscie.println("Write your name: ");
            wyjscie.println("Write your name: ");
            nazwaKlienta = wejscie.readLine();

            System.out.println("Zalogował się: " + nazwaKlienta);
            powiedzWszystkim("", "Logged in: " + nazwaKlienta);

            //zapisuję tego klienta do kontenera i wypisuję jego zawartość
            opis = new OpisKlienta(nazwaKlienta, wyjscie);
            klienci.add(opis);
            wypiszKlientow();

            //czytam info od klienta i przesyłam innym
            while (true) {
                String info = wejscie.readLine();
                if (info.equals("end")) {
                    System.out.println("Logged out: " + nazwaKlienta);
                    powiedzWszystkim("", "Logged out: " + nazwaKlienta);
                    klienci.remove(opis);
                    wejscie.close();
                    wyjscie.close();
                    socket.close();
                    break;
                }
               	powiedzWszystkim(nazwaKlienta, info);
                powiedzWszystkim(nazwaKlienta, info);
            }
        } catch (Exception e) { 
        }
//        try{
//            InputStream inputStream = socket.getInputStream();
//            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
//            LinkedList<Object> list =(LinkedList<Object>)objectInputStream.readObject();
//        }catch(IOException e){
//            
//        }catch(ClassNotFoundException ex){
//            
//        }
    } 
}

public class Server {
    static final int PORT = 6623;
    public static void main(String[] args) {
        ServerSocket s = null;
        Socket socket = null;
        try {
            s = new ServerSocket(PORT);
            System.out.println("Server Started");
            while (true) {
                socket = s.accept();
                System.out.println("New Client");
                
                new ObslugaKlienta(socket).start();                
            }
        } catch (IOException e) {
        } finally {
            try {
                socket.close();
                s.close();
            } catch (IOException e) {}
        }
    }
}