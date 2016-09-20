package prehistoricvolleyball;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
/**
 * Klasa Ball jest pochodną po klasie {@link SimpleBall}.
 * Reprezentuje piłkę, którą mogą odbijać Gracze (obiekty klasy {@link Player}).
 * Klasa zawiera również aktualny wynik meczu.
 * 
 * Obiekt klasy Ball jest tworzony jedynie po stronie serwera, gdyż
 * zawiera w sobie wynik i pozycję piłki, które muszą być jednakowe,
 * zarówno dla klienta, jak i serwera, nie mogą występować anomalie.
 * 
 * @author Rafał Migda
 */
public class Ball extends SimpleBall{
    int countTouchedFloor = 1; // ile razy piłka dotknęła podłogi
    boolean hasTouchedFloor = false; // czy pilka dotknela podlogi (istotna informacja do animacji spadania pilki)
    boolean touchable = true; // czy pilke mozna dotknac (nie mozna gdy wlasnie stracono punkt)
    boolean isRunning = false; // czy gra sie toczy (tak, jesli dotknieto pilki)
    boolean lastPoint = true; // kto zdobyl ostatniego punkta (potrzebne do ustawienia pilki, true - gracz1, false - gracz2)
    boolean isEnd=false; // czy koniec meczu
    public Ball(BufferedImage image) {
        super(image);
        vy=0;
        vx=0;
    }
    private double floor = 420; // punkt styku piłki z podłogą
    private double fG=0.08; //siła przyciągania dla piłki
    // przygotowanie zmiennej typu String,
    // docelowo zawierającej dane związane z stanem punktowym meczu i pozycją piłki
    // wysyłane do klienta
    String data;
    // ostatnia wysłana do klienta pozycja na osi X piłki
    double lastX=0;
    // ostatnia wysłana do klienta pozycja na osi X piłki
    double lastY=0;
    /**
     * Metoda aktualizująca pozycję piłki (x,y) oraz jej prędkość (vx,vy).
     * 
     * Metoda umożliwia wykrycie kolizji piłki ze ścianami lewą i prawą
     * po czym następuje zmiana prędkości.
     * 
     * Metoda wysyła także informacje do klienta o aktualnym położeniu piłki.
     */
    @Override
    public void update() {
        x += vx; // aktualizuje pozycje o predkosc na osi x
        y += vy;
        if( (lastX!=this.x) || (lastY!=this.y)) { // gdy x lub y zmienią pozycję - wyślij dane
            data = "02"+getX()+"*"+getY(); // 02 - kod wysłania współrzędnych piłki
            Game.server.sendData(data.getBytes());
            lastX=x; lastY = y;
        }
        if(isRunning)
            vy+=fG; // przyciąganie piłki do podłogi
        
        if(x<=0) // ściana lewa
            vx=Math.abs(vx); // odbijam piłkę (ustawiam prędkość na dodatnią)
        
        if (x+image.getWidth() >= 900) // ściana prawa
            vx = -Math.abs(vx);
        // siatka
        Rectangle r1 = new Rectangle((int)x,(int)y,image.getWidth(),image.getHeight()); // piłka
        Rectangle r2 = new Rectangle((Game.SIZE.width/2)-30,235,30,300); // siatka
        // kolizja piłka - siatka
        if(r1.intersects(r2)) {
            if(y<=r2.y) // gdy się odbije od góry siatki, piłka ma iść w górę
                vy = -Math.abs(vy); // idzie w górę
            else // w przeciwnym wypadku
                vx = -(vx); // zmień prędkość w poziomie
        }
        // ograniczenie maksymalnej i minimalnej prędkości piłki
        if(vy>5)
            vy=5;
        if(vy<=-5)
            vy=-5;
        if(vx>5)
            vx=5;
        if(vx<=-5)
            vx=-5;
        
        /* Piłka dotyka podłogi, aktualny wynik */
        if(y>=floor && countTouchedFloor<4)
        {
            vy=-vy;
            hasTouchedFloor=true;
            countTouchedFloor++;
            
            // dopisanie wyniku
            if(x>450 && countTouchedFloor==2) { // pilka spada u gracza 2
                if(touchable) { // sprawdzam czy punktu nie zdobyto poprzez odbicie 4 razy pod rząd
                    addScore(true,false);
                    lastPoint=true;
                }
            }
            if(x<450 && countTouchedFloor==2) { // pilka spada u gracza1, gracz2 otrzymuje punkt
                if(touchable) {
                    addScore(false,false);
                    lastPoint=false;
                }
            }
            // zasady gry takie jak w siatkówce plażowej
            // gra się: do dwóch wygranych setów do 21 pkt.,
            // wygrana drużyna musi posiadać dwa punkty przewagi.
            // Nie ma punktu końcowego.
            // W razie stanu po 1 w setach, rozgrywana jest trzecia partia,
            // którą toczy się na takich samych zasadach, tyle że do 15 pkt.
            // sety
            int countSets = scorePlayer1[1]+scorePlayer2[1];
            if(countSets!=2 && scorePlayer1[0]>20 && scorePlayer1[0]>scorePlayer2[0]+1) { // gdy liczba punktow wynosi wiecej niz 2 i gracz 1 ma przewage min. 2 punktow
                addScore(true,true);// dopisuje wygranego seta
                scorePlayer1[0]=0; // zeruje punkty
                scorePlayer2[0]=0;
            }
            
            if(countSets!=2 && scorePlayer2[0]>20 && scorePlayer2[0]>scorePlayer1[0]+1) {
                addScore(false,true);
                scorePlayer1[0]=0;
                scorePlayer2[0]=0;
            }
            
            if(countSets==2) // gdy nie koniec (czyli liczba setow == 2), gramy do 15 punktow
            {
                if(scorePlayer1[0]>14 && scorePlayer1[0]>scorePlayer2[0]+1) // dopisuje wygrany set graczowi 1
                    addScore(true,true);
                
                if(scorePlayer2[0]>14 && scorePlayer2[0]>scorePlayer1[0]+1) // dopisuje wygrany set graczowi 2
                    addScore(false,true);
            }
            
            if(scorePlayer1[1]>1 || scorePlayer2[1]>1) { // gdy któryś z graczy ma więcej niż 1 set - koniec meczu
                isEnd=true;
            }
        }
        /* Animacja piłki od podłogi */
        if(hasTouchedFloor && y<=floor-((1/(double)countTouchedFloor)*150))
        {
            hasTouchedFloor=false;
            vy*=0.6;
        }
        if(!isEnd && countTouchedFloor>=4) { // gdy wykona animacje opadania ustawiam pilke w miejscu
            vy=0;
            vx=0;
            
            y=200; // wysokosc pilki
            if(lastPoint) // gdy punkta zdobyl gracz1
                x=100;
            else
                x=750;
            
            countTouchedFloor=1; // ustawiam domyslna liczbe odbic od podlogi
            touchable = true; // pilke mozna juz odbijac
            isRunning=false; // brak przyciagania
            licznik1=0; // ustawiam licznik odbic na 0
            licznik2=0;
        } //koniec piłka od podłogi
        
        if(isEnd && i==0) { // koniec meczu, wyślij pakiet tylko raz
            Game.server.sendData("05".getBytes()); // wyslij pakiet do klienta o końcu meczu
            Game.GS = Game.GameState.END; 
            i++;
        }
    }
    int i=0;
    /**
     * Metoda pomocnicza, pozwalająca zaktualizować aktualny wynik meczu,
     * na zasadzie przypisania punkta lub seta konkretnego Graczowi.
     * @param isPlayer1 określa czy gracz pierwszy czy drugi
     * @param isSet  określa czy należy zaktualizować set czy wynik
     */
    private void addScore(boolean isPlayer1, boolean isSet) {
        if(!isEnd) {
            if(isSet) {
                if(isPlayer1) 
                    scorePlayer1[1]++;
                else 
                    scorePlayer2[1]++;
                data = "03"+getSets1()+"*"+getSets2(); // 03 - kod wysłania aktualizacji setów zawodników
           }
           else {
                if(isPlayer1) {
                    scorePlayer1[0]++;
                    lastPoint = true;
                }
                else {
                   scorePlayer2[0]++; 
                   lastPoint = false;
                }
                touchable=false; // po zdobyciu punktu, piłki nie można dotknąć (ma się wykonać animacja kilku odbić od podłogi)
                data = "04"+getPoints1()+"*"+getPoints2(); // 04 - kod wysłania aktualizacji punktów zawodników
            }
           Game.server.sendData(data.getBytes()); // wyslij pakiet do klienta o aktualizacji wyniku
        }
    }
    int licznik1=0; // licznik odbic pilki
    int licznik2=0;
    boolean canI = true; // zabezpieczenie przed podwójny odbiciem w zbyt krótkim czasie
    long lastTime = System.currentTimeMillis();
    @Override
    public void checkCollisionWith(Player player) {
        Rectangle r1 = new Rectangle((int)x,(int)y,image.getWidth(),image.getHeight());
        Rectangle r2 = new Rectangle((int)player.x,(int)player.y,player.image.getWidth(),player.image.getHeight());
        if(r1.intersects(r2) && canI && touchable) {
            // kierunek odbicia
            double tmpVx = (x-player.x)/5;
            double tmpVy = (y-player.y)/5;
            x+=tmpVx;
            vx=tmpVx;
            y+=tmpVy;
            vy=tmpVy;
            isRunning = true;
            canI=false; //zabezpieczenie przed podwójnym odbiciem w BARDZO krótkim odstepie czasu
            if(licznik1>2 && player.isPlayerOne) { // gdy pilke odbije wiecej niz 3 razy pod rzad - strata punktu
                addScore(false,false); // dodaje punkty graczowi 2
            }
            if(licznik2>2 && !player.isPlayerOne) {
                addScore(true,false);
            }
            
            if(player.isPlayerOne) { // gdy gracz 1 wykona kolizje
              licznik1++; // dodaje jedno odbicie
              licznik2=0; // zeruje licznik gracza 2
            }
            else {
                licznik2++;
                licznik1=0;
            }
        }
        if(System.currentTimeMillis()-lastTime>1000) {
            canI=true;
            lastTime=System.currentTimeMillis();
        }
    }
}