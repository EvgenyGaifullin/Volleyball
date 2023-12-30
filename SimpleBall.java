package prehistoricvolleyball;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Класс SimpleBall представляет мяч, который мы можем создать.
 * Он является базовым классом для класса {@link Ball}.
 * Объект класса SimpleBall отличается от объекта класса Ball тем,
 * что не используется для обнаружения коллизий с объектом {@link Player}.
 *
 * Объект этого класса создается на стороне клиента, желающего подключиться к серверу.
 *
 * @author Rafał Migda
 */

public class SimpleBall extends Entity{
    int[] scorePlayer1 = new int[2]; // Массив для результатов: scorePlyer1[0] -> очки, scorePlyer1[1] -> сеты
    int[] scorePlayer2 = new int[2];
    public SimpleBall(BufferedImage image) {
        super(image);
        this.x=100;
        this.y=200;
        scorePlayer1[0]=0;
        scorePlayer1[1]=0;
        scorePlayer2[0]=0;
        scorePlayer2[1]=0;
    }
    /**
     * Метод, проверяющий столкновение с объектом класса {@link Player}.
     * @param player указывает на соответствующего игрока
     */
    public void checkCollisionWith(Player player) {}
    /**
     * Метод отрисовки текущего счета игры между игроками.
     * @param g Графика2D
     */
    public void renderScore(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("ИГРОК I      " + getSets1(), 50, 22); // показать сеты для игрока 1
        g.setColor(Color.YELLOW);
        g.drawString("" + getPoints1(), 150, 22); // показать очки для игрока 1

        g.setColor(Color.WHITE);
        g.drawString("ИГРОК II     " + getSets2(), 50, 42);
        g.setColor(Color.YELLOW);
        g.drawString("" + getPoints2(), 150, 42);
    }
    /**
     * Метод возвращает количество очков первого игрока.
     * @return Текущее количество очков первого игрока.
     */
    public int getPoints1() {
        return scorePlayer1[0];
    }
    /**
     * Метод возвращает количество очков второго игрока.
     * @return Текущее количество очков второго игрока.
     */
    public int getPoints2() {
        return scorePlayer2[0];
    }
    /**
     * Метод возвращает количество сетов первого игрока.
     * @return Текущее количество сетов первого игрока.
     */
    public int getSets1() {
        return scorePlayer1[1];
    }
    /**
     * Метод возвращает количество сетов второго игрока.
     * @return Текущее количество сетов второго игрока.
     */
    public int getSets2() {
        return scorePlayer2[1];
    }
    /**
     * Метод позволяет изменить количество очков для первого игрока.
     * @param score число, которое будет сохранено как текущее количество очков.
     */
    public void setPoint1(int score) {
        scorePlayer1[0]=score;
    }
    /**
     * Метод позволяет изменить количество очков для второго игрока.
     * @param score число, которое будет сохранено как текущее количество очков.
     */
    public void setPoint2(int score) {
        scorePlayer2[0]=score;
    }
    /**
     * Метод позволяет изменить количество сетов для первого игрока.
     * @param score число, которое будет сохранено как текущее количество сетов.
     */
    public void setSet1(int score) {
        scorePlayer1[1]=score;
    }
    /**
     * Метод позволяет изменить количество сетов для второго игрока.
     * @param score число, которое будет сохранено как текущее количество сетов.
     */
    public void setSet2(int score) {
        scorePlayer2[1]=score;
    }
}
