package prehistoricvolleyball;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
/**
 * Klasa reprezentująca dowolny byt (np. piłkę, gracza).
 * @author Rafał Migda
 */
public abstract class Entity {
    public double x, y, vx=0, vy=0;
    public BufferedImage image;
    private Game game;
    
    public Entity(BufferedImage image) {
        this.image = image;
    }
    /**
     * Metoda aktualizująca prędkość i pozycje bytu.
     */
    public void update() {}
    /**
     * Metoda dzięki, której możemy narysować nasz byt.
     * @param g Grafika2D
     */
    public void render(Graphics2D g) {
        g.drawImage(image,(int)x, (int)y,game);
    }
    /**
     * Metoda pozwalająca zmienić pozycję bytu na osi X.
     * @param x pozycja na osi X
     */
    public void setX(double x) {
        this.x = x;
    }
    /**
     * Metoda pozwalająca zmienić pozycję bytu na osi Y.
     * @param y pozycja na osi Y
     */
    public void setY(double y) {
        this.y = y;
    }
    /**
     * Metoda zwraca aktualną pozycję bytu na osi X
     * @return pozycja na osi X
     */
    public double getX() {
        return x;
    }
    /**
     * Metoda zwraca aktualną pozycję bytu na osi Y
     * @return pozycja na osi Y
     */
    public double getY() {
        return y;
    }
    
}