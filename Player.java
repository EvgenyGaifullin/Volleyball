package prehistoricvolleyball;

import java.awt.image.BufferedImage;

/**
 * Класс Player представляет игрока.
 * @author Рафаэль Мигда
 */
public class Player extends Entity {
    public boolean isPlayerOne;
    private Key input;
    private boolean hasJumped = true;

    public Player(BufferedImage image, boolean isPlayerOne, Key input) {
        super(image);
        /* Устанавливаю начальные координаты */
        if (isPlayerOne)
            this.x = 100;
        else
            this.x = 600;
        this.y = 300;
        this.isPlayerOne = isPlayerOne;
        this.input = input;
    }

    double lastX = 0;
    double lastY = 0;

    @Override
    public void update() {
        x += vx; // Обновляю положение по оси X
        y += vy;

        if (canControl()) { // Отправляю пакет, когда могу управлять
            String data = "01" + getX() + "*" + getY(); // 01 - код отправки координат
            if ((lastX != this.x) || (lastY != this.y)) { // Когда X или Y изменяются - отправляю данные
                if (!isPlayerOne) // Клиент (клиент всегда является player2) к серверу
                    Game.client.sendData(data.getBytes());
                else
                    Game.server.sendData(data.getBytes());
                lastX = x;
                lastY = y;
            }
        }

        if (x <= 0) // Левая стена
            x = 0;
        if (x + image.getWidth() >= 900) // Правая стена
            x = 900 - image.getWidth();

        if ((x > (Game.SIZE.width / 2) - 30 - image.getWidth()) && isPlayerOne) // Сетка для игрока 1
            x = (Game.SIZE.width / 2) - 30 - image.getWidth();

        if ((x < (Game.SIZE.width / 2) + 30) && !isPlayerOne) // Сетка для игрока 2
            x = (Game.SIZE.width / 2) + 30;

        if (canControl()) {
            // Движение влево - вправо
            if (input.right)
                vx = 5;
            else if (input.left)
                vx = -5;
            else
                vx = 0;

            // ------------- Прыжок
            if (input.up && !hasJumped) {
                y -= 7;
                vy -= 7;
            }
            if (!input.up || y <= 250) // Устанавливаю, что произошел прыжок, когда отпускаю клавишу или Y<=250
                hasJumped = true;

            if (hasJumped)
                vy += 0.5;

            if (y >= 300)
                hasJumped = false;

            if (!hasJumped)
                vy = 0;
            // ------------- Конец прыжка
        }
    }

    /**
     * Метод, позволяющий определить, связана ли клавиатура с объектом Player.
     * @return логическое значение, указывающее, может ли управлять Игроком.
     */
    public boolean canControl() {
        if(input != null) 
            return true;
        return false;
    }
}
