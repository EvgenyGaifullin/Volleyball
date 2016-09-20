package prehistoricvolleyball;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Klasa Game reprezentująca naszą grę, w której wykonujemy funkcję main.
 * Dziedziczy po klasie Canvas, dzięki czemu możemy rysować naszą grę.
 * Implementuje interfejs Runnable, dzięki czemu utworzymy potrzebny wątek.
 * 
 * @author Rafał Migda
 */
public class Game extends Canvas implements Runnable{
    private static final int width = 300;
    private static final int height = width / 16*9;
    public static final int scale = 3;
    public static final Dimension SIZE = new Dimension(width*scale, height*scale);
    private Window frame;
    private String title = "Prehistoric Volleyball";
    private String version ="v1.0.0"; 
    private Thread thread;
    private boolean running;
    private Key key;
    public Player player, playerMP;
    public SimpleBall ball;
    private BufferedImage bgImage,bgImage2,bgImage3,rockImage,ballImage,leftPlayerImage,rightPlayerImage;
    /**
     * Definiuje aktualny stan gry.
     */
    public static enum GameState {
        WELCOME,HOST,JOIN,GAME, END, ERROR
    }
    public static GameState GS = GameState.WELCOME;
    public static Client client;
    public static Server server;
    public String errorMessage = "";

    public Game() {
        setPreferredSize(SIZE);
        setMinimumSize(SIZE);
        setMaximumSize(SIZE);
        
        running = false;
        
        key = new Key();
        addKeyListener(key);
        
        server = new Server(this);
        client = new Client(this,"localhost");
        
         try {
             bgImage = ImageIO.read(getClass().getResource("images/bg.png"));
             bgImage2 = ImageIO.read(getClass().getResource("images/bg2.png"));
             bgImage3 = ImageIO.read(getClass().getResource("images/bg3.png"));
             rockImage = ImageIO.read(getClass().getResource("images/rock.png"));
             ballImage = ImageIO.read(getClass().getResource("images/ball.png"));
             leftPlayerImage = ImageIO.read(getClass().getResource("images/player.png"));
             rightPlayerImage = ImageIO.read(getClass().getResource("images/player2.png"));
         } catch(IOException e ) {
             System.out.println("Blad odczytu obrazka");
         }
         
        frame = new Window();
        frame.setTitle(title);
        frame.add(this);
        frame.init();
    }
    /**
     * Metoda odpowiedzialna za wystartowanie Gry i utworzenie Wątku.
     */
    public synchronized void start() {
        running = true;
        thread = new Thread(this,"Game");
        thread.start();
    }
    /**
     * Metoda odpowiedzialna za zatrzymanie Gry i zakończenie wątku.
     */
    /**
     * Metoda realizująca m. in. funkcje update i render podczas działania wątku.
     * Ma za zadanie wykonać 60 aktualizacji i 60 renderowania klatek na sekundę.
     */
    @Override
    public void run() {
        
        long lastTime = System.currentTimeMillis();
        
       // long timer = System.currentTimeMillis();
       // int frames = 0; // liczba klatek podczas renderowania
       // int updates = 0; // liczba wywołań aktualizacji
        
        while(running) {
            if(System.currentTimeMillis()-lastTime>15) {
                update();
                render();
                lastTime=System.currentTimeMillis();
             //   frames++;
             //   updates++;
            }
            try {Thread.sleep(2); } catch (Exception e) {}
            /*if(System.currentTimeMillis()-timer > 1000) { // na sekunde
                System.out.println("fps:"+frames+", updates:"+updates);
                timer+=1000;
                updates = 0;
                frames = 0;
            }*/
        }
    }
    /**
     * Metoda aktualizująca logikę gry.
     */
    public void update() {
        if(GS == GameState.GAME) {
            key.update();
            player.update();
            playerMP.update();
            if(player.isPlayerOne) {
                ball.update();
                ball.checkCollisionWith(player);
                ball.checkCollisionWith(playerMP);
            }
        }
        if(GS == GameState.END && count==0) { // kiedy koniec gry
            saveScore(); // zapisuje wynik w historii spotkań
            count++;
        }
    }
    int count = 0;
    /**
     * Metoda odpowiedzialna za rysowanie gry.
     */
    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if(bs == null) {
            createBufferStrategy(2);
            return;
        }
        Graphics g  = bs.getDrawGraphics();
        Graphics2D g2 = (Graphics2D)g;
        
        g2.setColor(Color.BLACK);
        g2.fillRect(0,0,SIZE.width,SIZE.height);
        g2.drawImage(bgImage,0, 0,this); // tło
        
        g2.drawImage(rockImage,(SIZE.width/2)-30, 235, this); // siatka
        
        if(GS != GameState.GAME)  //gdy nie trwa gra
            g2.drawImage(bgImage3,260,225,this); // tło pod napisy na środku
        
        g2.setColor(Color.WHITE);
        
        /* 1. EKRAN POWITALNY */
        if(GS == GameState.WELCOME)
            g2.drawString("\""+title+"\"", 400, 250);
        /* 2. EKRAN POSZUKIWANIA KLIENTA */
        if(GS == GameState.HOST)
            g2.drawString("Oczekuję na przyłączenie nowego klienta...", 350, 250);
       /* 3. EKRAN NIE ZNALEZIONO SERWERA */
        if(GS == GameState.JOIN) 
            g2.drawString("Brak serwera w sieci lub serwer jest zajęty...", 330, 250);
        /* 4. EKRAN GRY */
        if(GS == GameState.GAME) {
            g2.drawImage(bgImage2, 46, 2,this); // redneruje dodatkowe tło (zaciemnienie pod wynikiem)
            ball.renderScore(g2); // narysuj wynik na ekran
            ball.render(g2);
            player.render(g2);
            playerMP.render(g2);
        }
        /* 5. EKRAN KOŃCA MECZU */
        if(GS == GameState.END) {
            g2.drawString("Koniec spotkania!", 400, 240);
            if(player.isPlayerOne)
                g2.drawString("TY " +ball.getSets1()+":"+ball.getSets2()+" PRZECIWNIK", 400, 260);
            else
                g2.drawString("PRZECIWNIK " +ball.getSets1()+":"+ball.getSets2()+" TY", 400, 260);
        }
        /* 6. EKRAN BŁĘDU */
        if(GS == GameState.ERROR) {
            int startX = (int) ( (SIZE.width/2) - (errorMessage.length()*2.5) ); // połowa ekranu - długość stringa * k
            g2.drawString(errorMessage, startX, 250);
        }
          
        g2.dispose();
        bs.show(); 
    }
    public void saveScore() {
        System.out.println("zapisuje wynik w historii");
        String info="";
        
        if(player.isPlayerOne) 
                info="TY " +ball.getSets1()+":"+ball.getSets2()+" PRZECIWNIK";
        else 
           info="PRZECIWNIK " +ball.getSets1()+":"+ball.getSets2()+" TY";
        
            try ( PrintWriter out = new PrintWriter(
                                   new BufferedWriter(
                                   new FileWriter("history.txt", true))) ) {
                out.write(new SimpleDateFormat("YYYY-MM-dd").format(new Date())+" "+info+"\r\n");
            }
            catch(IOException ex) {
                System.out.println(ex.getMessage());
            }
    }
    /**
     * Zagnieżdżona klasa Window, której obiekt reprezentuje Okno.
     * Obiekt tworzę w Grze, która zostaje w nim umieszczona.
     * 
     * Okno zawiera pasek menu, dzięki któremu możemy w prosty sposób
     * odnaleźć podstawowe funkcje naszej gry.
     */  
    public class Window extends JFrame implements ActionListener{
        private Game game;
        private JMenuItem host,join,close,history,help,about;

        public Window() {} 
        /**
         * Metoda, w której ustawiamy podstawowe parametry Okna,
         * utworzenie Menu i podpięcia Nasłuchiwacza.
         */
        public void init() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setResizable(false);
            setSize(Game.SIZE.width,Game.SIZE.height+55); // szerokość taka jak gra, wysokość +55 (potrzebne dla paska menu)
            setLayout(new FlowLayout());
            setLocationRelativeTo(null); // ustawiam okno na srodku ekranu

            /* PASEK MENU */
            /* 1. Tworzę pasek menu */
            JMenuBar menuBar = new JMenuBar();
            /* 2. Tworzę menu w pasku menu "menuBar" */
            JMenu menuGame = new JMenu("Gra");
            /* 3.Dodaję pierwsze menu "menuGame" do paska menu "menuBar" */
            menuBar.add(menuGame);
            /* 4. Tworzę kontener ArrayList z elementami JMenuItem, które 
                    będą powiązane z menu "menuGame" */
            ArrayList<JMenuItem> itemsMenuGame = new ArrayList<>();
            /* 5. Inicjalizuje przyciski w menu */
            host = new JMenuItem("Załóż serwer");
            join = new JMenuItem("Dołącz do isniejącego serwera");
            history = new JMenuItem("Historia spotkań");
            close = new JMenuItem("Zamknij");    
           /* 6. Dodaję przyciski do listy */
            itemsMenuGame.add(host);
            itemsMenuGame.add(join);
            itemsMenuGame.add(history);
            itemsMenuGame.add(close);

            /* 7. Wykonuję pętle, która doda przyciski z listy itemsMenuGame do menu menuGame
                    oraz doda ActionListenera do obiektów*/
            for(JMenuItem object:itemsMenuGame) {
                menuGame.add(object);
                object.addActionListener(this);
                if(object==join)
                    menuGame.addSeparator();
                if(object==history)
                    menuGame.addSeparator();
            }
            /* 8. Analogiczne czynności wykonuję w przypadku kolejnego menu */
            JMenu menuHelp = new JMenu("Pomoc");
            menuBar.add(menuHelp);
            ArrayList<JMenuItem> itemsHelp = new ArrayList<>();
            help = new JMenuItem("Pomoc");
            about = new JMenuItem("O grze");
            itemsHelp.add(help);
            itemsHelp.add(about);
            for(JMenuItem object:itemsHelp) {
                menuHelp.add(object);
                object.addActionListener(this);
                if(object==help)
                    menuHelp.addSeparator();
            }
            /* 9. Dodaję pasek menu do okna */
            setJMenuBar(menuBar);
            
            setVisible(true); // ustawiam okno na widzialne
        }
        /**
         * Metoda nasłuchująca przypisane akcje, które wykonujemy w Oknie.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            /* Obsługa PASKA MENU */
            /* 1. Deklaruję zmienną Object aby dowiedzieć się, który object został kliknięty */
            Object who = e.getSource();
            /* 2. Obsługuję przycisk host - "Uruchom serwer" */
            if(who==host) {
                /* Tworze odpowiedniego playera i playera MP */
                player = new Player(leftPlayerImage, true, key); 
                playerMP = new Player(rightPlayerImage, false, null);
                ball = new Ball(ballImage);

                host.setEnabled(false); // blokuję możliwość utworzenia serwera
                join.setEnabled(false); // blokuję możliwość kolejnego utworzenia klienta

                Game.server.start(); // startuje serwer
                Game.GS = GameState.HOST; // ustawiam status gry na "HOST", oczekuję na dołączenie klienta
            }
            /* 3. Obsługuję przycisk join - "Dołącz do istniejącego serwera" */
            if(who==join) {
                    /* Tworze odpowiedniego playera playeraMP oraz piłkę */
                    player = new Player(rightPlayerImage, false, key); 
                    playerMP = new Player(leftPlayerImage, true, null);
                    ball = new SimpleBall(ballImage);
                    
                    /* Ustawiam odpowiednio menu */
                    join.setEnabled(false); // blokuję możliwość utworzenia kolejnego klienta
                    host.setEnabled(false); // blokuję możliwość utworzenia serwera
                    
                    /* Przekazuje informacje o gotowści */
                    Game.client.start(); // startuje klienta
                    Game.GS = GameState.JOIN; // ustawiam status gry na "JOIN", szukam serwera
                    Game.client.sendData("00*00".getBytes()); // wysyłam komunikat do serwera o chęci dołączenia
            }
            /* 4. Obsługuję przycisk close - "Zamknij"*/
            if(who == close) {
                /* Wysyłam komunikat o chęci zamknięcia okna do systemowej kolejki zdarzeń */
                java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING));
            }
            /* 5. Obsługuję przycisk history - "Historia spotkań" */
            if(who==history) {
                /* Sprawdzam czy plik istnieje */
                 String fileName = "history.txt";
                 File plik = new File(fileName);
                 if(!plik.exists())
                 {
                     JOptionPane.showMessageDialog(this,
                     "Nie rozegrałeś jeszcze żadnego meczu.",
                     "Historia spotkań",
                     JOptionPane.ERROR_MESSAGE);
                 }
                 else {
                    try{ 
                        // przygotowanie strumienia
                         BufferedReader in = new BufferedReader(
                                 new InputStreamReader(
                                 new FileInputStream(fileName)));

                         //odczyt z pliku
                         String line;
                         String matchesHistory = "";
                         while(true){
                            line = in.readLine();
                            if(line == null) break;
                            matchesHistory += line + "\r\n";
                         }
                         // umieszczam historię moich spotkań w okienku dialogowym
                        JOptionPane.showMessageDialog(this,matchesHistory,
                        "Historia spotkań",JOptionPane.INFORMATION_MESSAGE);
                        in.close();
                    } catch(IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }
            /* 6. Obsługuję przycisk help - "O grze" */
            if(who==help) {
                JOptionPane.showMessageDialog(this,"STEROWANIE:\n"
                        + "Skok - strzałka w górę / \"W\"\n"
                        + "Lewo - strzałka w lewo / \"A\"\n"
                        + "Prawo - strzałka w prawo / \"D\"\n\n"
                        + "ZASADY GRY:\n"
                        + "1. Gra się: do dwóch wygranych setów do 21 pkt..\n"
                        + "2. Wygrana drużyna musi posiadać dwa punkty przewagi.\n"
                        + "3. Nie ma punktu końcowego.\n"
                        + "4. W razie stanu po 1 w setach, rozgrywana jest trzecia partia,\n"
                        + "którą toczy się na takich samych zasadach, tyle że do 15 pkt.\n\n",
                        "Sterowanie i zasady gry",JOptionPane.INFORMATION_MESSAGE);
            }
            /* 7. Obsługuję przycisk help - "O grze" */
            if(who==about) {
                JOptionPane.showMessageDialog(this,""+title.toUpperCase()+" "
                        + version+"\n\n"
                        + "Autor:\n"
                        + "Rafał Migda, student WSB-NLU\n\n"
                        + "PROJEKT ZALICZENIOWY:\n"
                        + "Programowanie w sieci internet\n\n"
                        + "2013",
                        "O grze",JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    public static void main(String[] args) {
         Game game = new Game();
         game.start();
    }
}