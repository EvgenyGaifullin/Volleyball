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
 * Класс Server представляет сервер, который можно создать и позволить клиенту подключиться к сокету,
 * чтобы отправлять и принимать пакеты от/к клиенту.
 * @author Rafał Migda
 */
public class Server extends Thread {
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
     * Метод отвечает за функционирование сервера, в том числе прослушивает
     * входящие пакеты от сервера.
     */
    @Override
    public void run() {
        try {
            this.socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            game.errorMessage = "Не удалось создать сокет.";
            Game.GS = Game.GameState.ERROR;
            return;
        }
        String message = "";
        byte[] data;
        DatagramPacket packet = null;
        while (true) {
            try {
                data = new byte[1024];
                packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                message = new String(packet.getData());
            } catch (UnknownHostException e) {
                game.errorMessage = "Произошла неизвестная ошибка.";
                Game.GS = Game.GameState.ERROR;
                return;
            } catch (Exception ex) {
                game.errorMessage = "Ошибка подключения.";
                Game.GS = Game.GameState.ERROR;
            }
            if (message.trim().equalsIgnoreCase("00*00")) { // "00*00" - запрос на присоединение к игре
                numberOfClients++; // увеличиваем количество клиентов, желающих поиграть
                if (numberOfClients == 1) { // если количество клиентов == 1 - можно начать игру
                    socket.connect(packet.getAddress(), packet.getPort()); // соединяем сокет с первым желающим поиграть
                    //clientIP = packet.getAddress()
                    //clientPort = packet.getPort();
                    sendData("00*00".getBytes()); // сообщаем клиенту, что у меня есть свободное место
                    Game.GS = Game.GameState.GAME; // устанавливаем состояние игры на "GAME", можно играть
                    System.out.println("К клиенту присоединился [" + packet.getAddress() + ":" + packet.getPort() + "]");
                }
            } else {
                double xMP = Double.parseDouble(message.substring(2, message.indexOf('*')));
                double yMP = Double.parseDouble(message.substring(message.indexOf('*') + 1));
                game.playerMP.setX(xMP);
                game.playerMP.setY(yMP);
            }
        }
    }

    /**
     * Метод для отправки пакета данных подключенному клиенту.
     *
     * @param data байты данных, которые нужно отправить
     */
    public void sendData(byte[] data) {
        //DatagramPacket packet = new DatagramPacket(data, data.length, clientIP, clientPort);
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            this.socket.send(packet);
        } catch (Exception e) {
            System.out.println("Не удалось отправить пакет.");
        }
    }
}
