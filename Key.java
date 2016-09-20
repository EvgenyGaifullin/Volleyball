package prehistoricvolleyball;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
/**
 * Klasa reprezentująca zdarzenia związane z klawiaturą,
 * które są niezbędne do funkcjonowania naszej gry.
 * @author Rafał Migda
 */
public class Key implements KeyListener{
    public boolean up, left, right;
    private boolean[] keys = new boolean[150];
    
    /**
     * Metoda odpowiedzialna za ustawienie odpowiedniej wartości na odpowiednim klawiszu.
     */
    public void update() {
        left = keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A]; // ustawiam wartość z tablicy keys
        right = keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D];
        up = keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W];
    }
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true; // gdy kliknę ustawiam klikniętego klawisza na true
    }
    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false; // zwalniam klawisz
    }
}
