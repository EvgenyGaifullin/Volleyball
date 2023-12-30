package prehistoricvolleyball;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Класс Client представляет клиента, который отвечает за соединение,
 * отправку и получение определенных пакетов от/для сервера.
 * @author Рафаэль Мигда
 */
public class Client extends Thread {
    private InetAddress ipAddress;
    private static final int PORT = 9876;
    private DatagramSocket socket;
    private Game game;

    public Client(Game game, String ipAddress) {
        this.game = game;
        try {
            this.socket = new DatagramSocket();
            this.ipAddress = InetAddress.getByName(ipAddress);
        } catch (SocketException | UnknownHostException e) {
            System.out.println("1");
        }
    }

    /**
     * Метод отвечает за работу клиента, включая прослушивание
     * входящих пакетов от сервера.
     */
    @Override
    public void run() {
        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet); // получаем пакеты от сервера
            } catch (IOException ex) {
            }

            String message = new String(packet.getData());
            String messageID = message.substring(0, 2).trim();
            if (messageID.equalsIgnoreCase("00")) {
                System.out.println("Подключено к серверу ["
                        + packet.getAddress().getHostAddress()
                        + ":" + packet.getPort()
                        + "]> ");
                Game.GS = Game.GameState.GAME; // получили ответный пакет - устанавливаем состояние игры на "GAME", можно начинать игру
            } else if (messageID.equalsIgnoreCase("01")) { // обновление координат игрока mp
                double xMP = Double.parseDouble(message.substring(2, message.indexOf('*')));
                double yMP = Double.parseDouble(message.substring(message.indexOf('*') + 1));
                game.playerMP.setX(xMP);
                game.playerMP.setY(yMP);
            } else if (messageID.equalsIgnoreCase("02")) { // обновление положения мяча
                double xBallMP = Double.parseDouble(message.substring(2, message.indexOf('*')));
                double yBallMP = Double.parseDouble(message.substring(message.indexOf('*') + 1));
                game.ball.setX(xBallMP);
                game.ball.setY(yBallMP);
            } else if (messageID.equalsIgnoreCase("03")) { // обновление счета
                int set1 = (int) Double.parseDouble(message.substring(2, message.indexOf('*')));
                int set2 = (int) Double.parseDouble(message.substring(message.indexOf('*') + 1));
                game.ball.setSet1(set1);
                game.ball.setSet2(set2);
            } else if (messageID.equalsIgnoreCase("04")) { // обновление очков
                int point1 = (int) Double.parseDouble(message.substring(2, message.indexOf('*')));
                int point2 = (int) Double.parseDouble(message.substring(message.indexOf('*') + 1));
                game.ball.setPoint1(point1);
                game.ball.setPoint2(point2);
            } else { // конец игры
                Game.GS = Game.GameState.END;
            }
        }
    }

    /**
     * Метод позволяет отправить пакет данных серверу.
     * @param data байты данных, которые нужно отправить
     */
    public void sendData(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, PORT);
        try {
            socket.send(packet);
        } catch (IOException ex) {
        }
    }
}
