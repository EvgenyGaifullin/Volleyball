package prehistoricvolleyball;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Klasa SimpleBall reprezentuje piłkę, którą możemy utworzyć.
 * Jest klasą bazową dla klasy {@link Ball}.
 * Obiekt klasy SimpleBall różni się od obiektu klasy Ball tym,
 * że nie służy do wykrycia kolizji pomiędzy obiektem klasy {@link Player}.
 * 
 * Obiekt tej klasy jest tworzony po stronie klienta chcącego dołączyć do serwera.
 * 
 * @author Rafał Migda
 */

public class SimpleBall extends Entity{
    int[] scorePlayer1 = new int[2]; // tablica na wynik scorePlyer1[0] -> punkty, scorePlyer1[1] -> sety
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
     * Metoda pozwalająca sprawdzić czy nastąpiła kolizja
     * z obiektem klasy {@link Player}.
     * @param player wskazuje odpowiedniego gracza
     */
    public void checkCollisionWith(Player player) {}
    /**
     * Metoda rysująca aktualny wynik spotkania pomiędzy graczami.
     * @param g Grafika2D
     */
    public void renderScore(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("GRACZ I      "+getSets1(), 50, 22); // wyświetl sety dla gracza 1
        g.setColor(Color.YELLOW);
        g.drawString(""+getPoints1(), 150, 22); // wyświetl punkty dla gracza 1
        
        g.setColor(Color.WHITE);
        g.drawString("GRACZ II     "+getSets2(), 50, 42);
        g.setColor(Color.YELLOW);
        g.drawString(""+getPoints2(), 150, 42);
    }
    /**
     * Metoda zwraca punkty Gracza 1.
     * @return Aktualna liczba zdobytych punktów Gracza 1.
     */
    public int getPoints1() {
        return scorePlayer1[0];
    }
    /**
     * Metoda zwraca punkty Gracza 2.
     * @return Aktualna liczba zdobytych punktów Gracza 2.
     */
    public int getPoints2() {
        return scorePlayer2[0];
    }
    /**
     * Metoda zwraca liczbę setów Gracza 1.
     * @return Aktualna liczba zdobytych setów Gracza 1.
     */
    public int getSets1() {
        return scorePlayer1[1];
    }
    /**
     * Metoda zwraca liczbę setów Gracza 2.
     * @return Aktualna liczba zdobytych setów Gracza 2.
     */
    public int getSets2() {
        return scorePlayer2[1];
    }
    /**
     * Metoda pozwala zmienić liczbę punktów Graczowi 1.
     * @param score liczba, która ma zostać zapisana jako aktualny stan zdobytych punktów.
     */
    public void setPoint1(int score) {
        scorePlayer1[0]=score;
    }
    /**
     * Metoda pozwala zmienić liczbę punktów Graczowi 2.
     * @param score liczba, która ma zostać zapisana jako aktualny stan zdobytych punktów.
     */
    public void setPoint2(int score) {
        scorePlayer2[0]=score;
    }
    /**
     * Metoda pozwala zmienić liczbę setów Graczowi 1.
     * @param score liczba, która ma zostać zapisana jako aktualny stan zdobytych setów.
     */
    public void setSet1(int score) {
        scorePlayer1[1]=score;
    }
    /**
     * Metoda pozwala zmienić liczbę setów Graczowi 2.
     * @param score liczba, która ma zostać zapisana jako aktualny stan zdobytych setów.
     */
    public void setSet2(int score) {
        scorePlayer2[1]=score;
    }
}
