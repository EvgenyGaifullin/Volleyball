package prehistoricvolleyball;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Класс, представляющий любой объект (например, мяч, игрока).
 * @author Рафаэль Мигда
 */
public abstract class Entity {
    public double x, y, vx = 0, vy = 0;
    public BufferedImage image;
    private Game game;

    public Entity(BufferedImage image) {
        this.image = image;
    }

    /**
     * Метод обновления скорости и позиции объекта.
     */
    public void update() {}

    /**
     * Метод, позволяющий отрисовать объект.
     * @param g Графика2D
     */
    public void render(Graphics2D g) {
        g.drawImage(image, (int) x, (int) y, game);
    }

    /**
     * Метод устанавливает позицию объекта по оси X.
     * @param x позиция по оси X
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Метод устанавливает позицию объекта по оси Y.
     * @param y позиция по оси Y
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Метод возвращает текущую позицию объекта по оси X.
     * @return позиция по оси X
     */
    public double getX() {
        return x;
    }

    /**
     * Метод возвращает текущую позицию объекта по оси Y.
     * @return позиция по оси Y
     */
    public double getY() {
        return y;
    }
}
