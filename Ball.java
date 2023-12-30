package prehistoricvolleyball;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Класс Ball является производным от класса {@link SimpleBall}.
 * Представляет собой мяч, который могут отбивать игроки (объекты класса {@link Player}).
 * Класс также содержит текущий счет игры.
 *
 * Объект класса Ball создается только на стороне сервера, поскольку
 * он содержит в себе счет и позицию мяча, которые должны быть одинаковыми
 * как для клиента, так и для сервера, не могут быть аномалии.
 *
 * @author Рафаэль Мигда
 */
public class Ball extends SimpleBall {
    int countTouchedFloor = 1; // количество касаний мяча полом
    boolean hasTouchedFloor = false; // касался ли мяч пола (важная информация для анимации падения мяча)
    boolean touchable = true; // можно ли касаться мяча (нельзя, если только что проигран очко)
    boolean isRunning = false; // идет ли игра (да, если коснулись мяча)
    boolean lastPoint = true; // кто набрал последний очко (необходимо для установки мяча, true - игрок1, false - игрок2)
    boolean isEnd = false; // конец игры
    public Ball(BufferedImage image) {
        super(image);
        vy = 0;
        vx = 0;
    }
    private double floor = 420; // точка контакта мяча с полом
    private double fG = 0.08; // сила притяжения для мяча
    // подготовка строки,
    // содержащей данные о состоянии счета игры и позиции мяча
    // отправляемые клиенту
    String data;
    // последняя отправленная клиенту позиция по оси X мяча
    double lastX = 0;
    // последняя отправленная клиенту позиция по оси Y мяча
    double lastY = 0;
    /**
     * Метод обновляет позицию мяча (x, y) и его скорость (vx, vy).
     *
     * Метод позволяет обнаружить столкновение мяча со стенами слева и справа,
     * после чего происходит изменение скорости.
     *
     * Метод также отправляет информацию клиенту о текущем положении мяча.
     */
    @Override
    public void update() {
        x += vx; // обновляем позицию по оси x на скорость x
        y += vy;
        if ((lastX != this.x) || (lastY != this.y)) { // если x или y изменились - отправляем данные
            data = "02" + getX() + "*" + getY(); // 02 - код отправки координат мяча
            Game.server.sendData(data.getBytes());
            lastX = x;
            lastY = y;
        }
        if (isRunning)
            vy += fG; // притяжение мяча к полу

        if (x <= 0) // левая стена
            vx = Math.abs(vx); // отбиваем мяч (устанавливаем скорость на положительную)

        if (x + image.getWidth() >= 900) // правая стена
            vx = -Math.abs(vx);
        // сетка
        Rectangle r1 = new Rectangle((int) x, (int) y, image.getWidth(), image.getHeight()); // мяч
        Rectangle r2 = new Rectangle((Game.SIZE.width / 2) - 30, 235, 30, 300); // сетка
        // столкновение мяча - сетка
        if (r1.intersects(r2)) {
            if (y <= r2.y) // если он отскочил от верха сетки, мяч должен идти вверх
                vy = -Math.abs(vy);
            else
                vx = -(vx);
        }
        // ограничение максимальной и минимальной скорости мяча
        if (vy > 5)
            vy = 5;
        if (vy <= -5)
            vy = -5;
        if (vx > 5)
            vx = 5;
        if (vx <= -5)
            vx = -5;

        /* Мяч касается пола, текущий счет */
        if (y >= floor && countTouchedFloor < 4) {
            vy = -vy;
            hasTouchedFloor = true;
            countTouchedFloor++;

            // добавляем очко
            if (x > 450 && countTouchedFloor == 2) { // мяч падает у игрока 2
                if (touchable) {
                    addScore(true, false);
                    lastPoint = true;
                }
            }
            if (x < 450 && countTouchedFloor == 2) { // мяч падает у игрока 1, игрок 2 получает очко
                if (touchable) {
                    addScore(false, false);
                    lastPoint = false;
                }
            }
            // правила игры как в пляжный волейбол
            // игра до двух выигранных партий до 21 очка,
            // выигравшая команда должна иметь два очка преимущества.
            // Нет конечного очка.
            // В случае счета 1:1 играется третья партия,
            // которая играется по тем же правилам, но до 15 очков.
            // партии
            int countSets = scorePlayer1[1] + scorePlayer2[1];
            if (countSets != 2 && scorePlayer1[0] > 20 && scorePlayer1[0] > scorePlayer2[0] + 1) {
                addScore(true, true);
                scorePlayer1[0] = 0;
                scorePlayer2[0] = 0;
            }

            if (countSets != 2 && scorePlayer2[0] > 20 && scorePlayer2[0] > scorePlayer1[0] + 1) {
                addScore(false, true);
                scorePlayer1[0] = 0;
                scorePlayer2[0] = 0;
            }

            if (countSets == 2) {
                if (scorePlayer1[0] > 14 && scorePlayer1[0] > scorePlayer2[0] + 1)
                    addScore(true, true);

                if (scorePlayer2[0] > 14 && scorePlayer2[0] > scorePlayer1[0] + 1)
                    addScore(false, true);
            }

            if (scorePlayer1[1] > 1 || scorePlayer2[1] > 1) {
                isEnd = true;
            }
        }
        /* Анимация мяча от пола */
        if (hasTouchedFloor && y <= floor - ((1 / (double) countTouchedFloor) * 150)) {
            hasTouchedFloor = false;
            vy *= 0.6;
        }
        if (!isEnd && countTouchedFloor >= 4) {
            vy = 0;
            vx = 0;

            y = 200;
            if (lastPoint)
                x = 100;
            else
                x = 750;

            countTouchedFloor = 1;
            touchable = true;
            isRunning = false;
            licznik1 = 0;
            licznik2 = 0;
        }

        if (isEnd && i == 0) {
            Game.server.sendData("05".getBytes());
            Game.GS = Game.GameState.END;
            i++;
        }
    }
    int i = 0;

    private void addScore(boolean isPlayer1, boolean isSet) {
        if (!isEnd) {
            if (isSet) {
                if (isPlayer1)
                    scorePlayer1[1]++;
                else
                    scorePlayer2[1]++;
                data = "03" + getSets1() + "*" + getSets2();
            } else {
                if (isPlayer1) {
                    scorePlayer1[0]++;
                    lastPoint = true;
                } else {
                    scorePlayer2[0]++;
                    lastPoint = false;
                }
                touchable = false;
                data = "04" + getPoints1() + "*" + getPoints2();
            }
            Game.server.sendData(data.getBytes());
        }
    }
    int licznik1 = 0;
    int licznik2 = 0;
    boolean canI = true;
    long lastTime = System.currentTimeMillis();

    @Override
    public void checkCollisionWith(Player player) {
        Rectangle r1 = new Rectangle((int) x, (int) y, image.getWidth(), image.getHeight());
        Rectangle r2 = new Rectangle((int) player.x, (int) player.y, player.image.getWidth(), player.image.getHeight());
        if (r1.intersects(r2) && canI && touchable) {
            double tmpVx = (x - player.x) / 5;
            double tmpVy = (y - player.y) / 5;
            x += tmpVx;
            vx = tmpVx;
            y += tmpVy;
            vy = tmpVy;
            isRunning = true;
            canI = false;
            if (licznik1 > 2 && player.isPlayerOne) {
                addScore(false, false);
            }
            if (licznik2 > 2 && !player.isPlayerOne) {
                addScore(true, false);
            }

            if (player.isPlayerOne) {
                licznik1++;
                licznik2 = 0;
            } else {
                licznik2++;
                licznik1 = 0;
            }
        }
        if (System.currentTimeMillis() - lastTime > 1000) {
            canI = true;
            lastTime = System.currentTimeMillis();
        }
    }
}
