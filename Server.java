package prehistoricvolleyball;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Klasa Server reprezentuje Serwer, który możemy utworzyć i umożliwić przyłączenie
 * Klienta do gniazda, dzięki czemu można wysyłać i odbierać pakiety od/do Klienta.
 * @author Rafał Migda
 */
public class Server extends Thread{
    public static final int PORT = 9876;
    private DatagramSocket socket;
    private static int numberOfClients = 0;
    private Game game;
    //private InetAddress clientIP;
    //private int clientPort;
    public Server(Game game) {
        this.game = game;
    }
    /**
     * Metoda odpowiedzialna za działanie serwera, m. in. nasłuchuje
     * pakiety przychodzących od serwera.
     */
    @Override
    public void run() {
        try {
            this.socket = new DatagramSocket(PORT);
        } catch( SocketException e) { 
                game.errorMessage = "Nie można utworzyć gniazda.";
                Game.GS = Game.GameState.ERROR;
                return;
        }
        String message="";
        byte[] data;
        DatagramPacket packet = null;
        while(true) {
            try {
                data = new byte[1024];
                packet = new DatagramPacket(data,data.length);
                socket.receive(packet);
                message = new String(packet.getData());
            } catch (UnknownHostException e) {
                game.errorMessage = "Wystąpił nieznany błąd.";
                Game.GS = Game.GameState.ERROR;
                return;
            } catch (Exception ex) {
                game.errorMessage = "Błąd połączenia.";
                Game.GS = Game.GameState.ERROR;
            }
            if(message.trim().equalsIgnoreCase("00*00")) { // "00*00" - wiadomość o chęci przyłączenia do gry
                numberOfClients++; // zwiększam liczbę klientów, którzy chcą ze mną zagrać
                if(numberOfClients==1) { // gdy liczba klientów == 1 - można ze mną zagrać
                    socket.connect(packet.getAddress(),packet.getPort()); // łączę socket z pierwszych chętnym do gry
                    //clientIP = packet.getAddress()
                    //clientPort = packet.getPort();
                    sendData("00*00".getBytes()); // inforumjący klienta o tym, że mam wolne miejsce
                    Game.GS = Game.GameState.GAME; // ustawiam status gry na "GAME", można już grać
                    System.out.println("Dołączył klient ["+packet.getAddress()+":"+packet.getPort()+"]");
                }
            }
            else {
                double xMP = Double.parseDouble(message.substring(2,message.indexOf('*')));
                double yMP = Double.parseDouble(message.substring(message.indexOf('*')+1));
                game.playerMP.setX(xMP);
                game.playerMP.setY(yMP);
            }
        }
    }
    /**
     * Metoda pozwalająca wysłać pakiet danych do dołączonego Klienta.
     * @param data bajty danych, które chcemy wysłać
     */
    public void sendData(byte[] data) {
        //DatagramPacket packet = new DatagramPacket(data,data.length,clientIP,clientPort);
        DatagramPacket packet = new DatagramPacket(data,data.length);
        try {
            this.socket.send(packet);
        } catch (Exception e) {
            System.out.println("Nie udało mi się wysłać pakietu.");
        }
    }
}