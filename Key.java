package prehistoricvolleyball;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Класс, представляющий события клавиатуры,
 * необходимые для функционирования нашей игры.
 * @author Рафаэль Мигда
 */
public class Key implements KeyListener {
    public boolean up, left, right;
    private boolean[] keys = new boolean[150];

    /**
     * Метод, отвечающий за установку соответствующего значения для определенной клавиши.
     */
    public void update() {
        left = keys[KeyEvent.VK_LEFT] || keys[KeyEvent.VK_A]; // Устанавливаю значение из массива keys
        right = keys[KeyEvent.VK_RIGHT] || keys[KeyEvent.VK_D];
        up = keys[KeyEvent.VK_UP] || keys[KeyEvent.VK_W];
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true; // Когда нажата клавиша, устанавливаю значение клавиши как true
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false; // Освобождаю клавишу
    }
}
