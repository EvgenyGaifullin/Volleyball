package prehistoricvolleyball;

import java.awt.image.BufferedImage;
/**
 * Klasa Player reprezentuje Gracza.
 * @author Rafał Migda
 */
public class Player extends Entity{
    public boolean isPlayerOne;
    private Key input;
    private boolean hasJumped = true;
    public Player(BufferedImage image, boolean isPlayerOne, Key input) {
        super(image);
         /* ustawiam domyślne wspolrzedne */
        if(isPlayerOne)
            this.x = 100;
        else
            this.x=600;
        this.y=300;
        this.isPlayerOne = isPlayerOne;
        this.input = input;
    }
    double lastX=0;
    double lastY=0;
    @Override
    public void update() {
        x += vx; // aktualizuje pozycje o predkosc na osi x
        y += vy;
        if(canControl()) { // wysyłam pakiet, gdy mogę sterować
            String data = "01"+getX()+"*"+getY(); // 01 - kod wysłania współrzędnych
            if( (lastX!=this.x) || (lastY!=this.y)) { // gdy x lub y zmienią pozycję - wyślij dane
                if(!isPlayerOne) // klient (klient zawsze jest player2) do serewera
                    Game.client.sendData(data.getBytes());
                else 
                    Game.server.sendData(data.getBytes());
                lastX=x; lastY = y;
            }
        }
        
        if(x<=0) // ściana lewa
            x=0;
        if (x+image.getWidth() >= 900) // ściana prawa
            x=900-image.getWidth();
        
        if((x > (Game.SIZE.width/2)-30-image.getWidth() ) && isPlayerOne) // siatka dla gracza 1
            x =(Game.SIZE.width/2)-30-image.getWidth();
        
        if((x < (Game.SIZE.width/2)+30 ) && !isPlayerOne) // siatka dla gracza 2
            x =(Game.SIZE.width/2)+30;
        
        if(canControl()) {
            // ruch lewo - prawo
            if(input.right) vx = 5;
            else if(input.left) vx = -5;
            else vx = 0;

            // ------------- skok
            if(input.up && hasJumped == false)
            {
                y -=7;
                vy -=7;
            }
            if(!input.up || y<=250) // ustawiam, że skoczyłem, gdy zwolnię klawisz lub y<=250
              hasJumped = true;

            if (hasJumped == true) 
                   vy += 0.5;

            if(y >= 300)
                hasJumped = false;

            if (hasJumped == false)
                vy = 0;
            // ------------- koniec skok
        }
    }
    /**
     * Metoda pozwalająca określić czy klawiatura jest przypisana do obiektu Player.
     * @return wartość logiczną rozstrzygającą czy mogę sterować Graczem.
     */
    public boolean canControl() {
        if(input != null) 
            return true;
        return false;
    }
}
