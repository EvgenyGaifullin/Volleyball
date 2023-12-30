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
 * Класс Game представляет нашу игру, в которой содержится функция main.
 * Он наследуется от класса Canvas, что позволяет нам рисовать нашу игру.
 * Реализует интерфейс Runnable для создания необходимого потока.
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
     * Определяет текущее состояние игры.
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
             System.out.println("Ошибка чтения изображения");
         }
         
        frame = new Window();
        frame.setTitle(title);
        frame.add(this);
        frame.init();
    }
    /**
     * Метод, отвечающий за запуск игры и создание потока.
     */
    public synchronized void start() {
        running = true;
        thread = new Thread(this,"Game");
        thread.start();
    }
    /**
     * Метод, отвечающий за остановку игры и завершение потока.
     */
    /**
     * Метод, выполняющий, среди прочего, функции update и render во время работы потока.
     * Его задачей является выполнение 60 обновлений и отображение 60 кадров в секунду.
     */
    @Override
    public void run() {
        
        long lastTime = System.currentTimeMillis();

        // long timer = System.currentTimeMillis();
        // int frames = 0; // количество кадров во время отрисовки
        // int updates = 0; // количество вызовов обновлений
        
        while(running) {
            if(System.currentTimeMillis()-lastTime>15) {
                update();
                render();
                lastTime=System.currentTimeMillis();
             //   frames++;
             //   updates++;
            }
            try {Thread.sleep(2); } catch (Exception e) {}
            /*if(System.currentTimeMillis()-timer > 1000) { // на секунде
                System.out.println("fps:"+frames+", updates:"+updates);
                timer+=1000;
                updates = 0;
                frames = 0;
            }*/
        }
    }
    /**
     * Метод обновления логики игры.
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
        if(GS == GameState.END && count==0) { // когда игра закончена
            saveScore(); // сохраняет результат в истории игр
            count++;
        }
    }
    int count = 0;
    /**
     * Метод, отвечающий за отрисовку игры.
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
        
        g2.drawImage(rockImage,(SIZE.width/2)-30, 235, this); // сетка
        
        if(GS != GameState.GAME)  // если игра не активна
            g2.drawImage(bgImage3,260,225,this); // фон под текстом в центре
        
        g2.setColor(Color.WHITE);


        /* 1. ЭКРАН ПРИВЕТСТВИЯ */
        if (GS == GameState.WELCOME)
            g2.drawString( "\"" + title + "\"", 400, 250);

        /* 2. ЭКРАН ПОИСКА КЛИЕНТА */
        if (GS == GameState.HOST)
            g2.drawString("Ожидание подключения нового клиента...", 350, 250);

        /* 3. ЭКРАН ОЖИДАНИЯ СЕРВЕРА */
        if (GS == GameState.JOIN)
            g2.drawString("Нет сервера в сети или сервер занят...", 330, 250);

        /* 4. ЭКРАН ИГРЫ */
        if (GS == GameState.GAME) {
            g2.drawImage(bgImage2, 46, 2, this); // рисует дополнительный фон (затемнение под результатом)
            ball.renderScore(g2); // отобразить результат на экране
            ball.render(g2);
            player.render(g2);
            playerMP.render(g2);
        }

        /* 5. ЭКРАН КОНЦА МАТЧА */
        if (GS == GameState.END) {
            g2.drawString("Конец матча!", 400, 240);
            if (player.isPlayerOne)
                g2.drawString("ТЫ " + ball.getSets1() + ":" + ball.getSets2() + " СОПЕРНИК", 400, 260);
            else
                g2.drawString("СОПЕРНИК " + ball.getSets1() + ":" + ball.getSets2() + " ТЫ", 400, 260);
        }

        /* 6. ЭКРАН ОШИБКИ */
        if (GS == GameState.ERROR) {
            int startX = (int) ((SIZE.width / 2) - (errorMessage.length() * 2.5)); // половина экрана - длина строки * k
            g2.drawString(errorMessage, startX, 250);
        }
          
        g2.dispose();
        bs.show(); 
    }
    public void saveScore() {
        System.out.println("Сохранение результата в истории");
        String info = "";

        if (player.isPlayerOne)
            info = "ТЫ " + ball.getSets1() + ":" + ball.getSets2() + " СОПЕРНИК";
        else
            info = "СОПЕРНИК " + ball.getSets1() + ":" + ball.getSets2() + " ТЫ";

        try (PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new FileWriter("history.txt", true)))) {
            out.write(new SimpleDateFormat("YYYY-MM-dd").format(new Date()) + " " + info + "\r\n");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Вложенный класс Window, объект которого представляет окно.
     * Объект создается в классе Game и в нем размещается игра.
     *
     * Окно содержит меню, с помощью которого мы можем легко
     * находить основные функции нашей игры.
     */
    public class Window extends JFrame implements ActionListener {
        private Game game;
        private JMenuItem host, join, close, history, help, about;

        public Window() {
        }

        /**
         * Метод, в котором мы устанавливаем основные параметры окна,
         * создаем меню и подключаем слушателя событий.
         */
        public void init() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setResizable(false);
            setSize(Game.SIZE.width, Game.SIZE.height + 55); // ширина как у игры, высота +55 (для панели меню)
            setLayout(new FlowLayout());
            setLocationRelativeTo(null); // устанавливаем окно по центру экрана

            /* ПАНЕЛЬ МЕНЮ */
            /* 1. Создаем панель меню */
            JMenuBar menuBar = new JMenuBar();
            /* 2. Создаем меню в панели меню "menuBar" */
            JMenu menuGame = new JMenu("Игра");
            /* 3. Добавляем первое меню "menuGame" в панель меню "menuBar" */
            menuBar.add(menuGame);
            /* 4. Создаем контейнер ArrayList с элементами JMenuItem, которые
            будут связаны с меню "menuGame" */
            ArrayList<JMenuItem> itemsMenuGame = new ArrayList<>();
            /* 5. Инициализируем кнопки в меню */
            host = new JMenuItem("Создать сервер");
            join = new JMenuItem("Присоединиться к существующему серверу");
            history = new JMenuItem("История игр");
            close = new JMenuItem("Закрыть");
            /* 6. Добавляем кнопки в список */
            itemsMenuGame.add(host);
            itemsMenuGame.add(join);
            itemsMenuGame.add(history);
            itemsMenuGame.add(close);

            /* 7. Выполняем цикл, который добавляет кнопки из списка itemsMenuGame в меню menuGame
        и добавляет слушателя ActionListener к объектам */
            for (JMenuItem object : itemsMenuGame) {
                menuGame.add(object);
                object.addActionListener(this);
                if (object == join)
                    menuGame.addSeparator();
                if (object == history)
                    menuGame.addSeparator();
            }
            /* 8. Аналогичные действия выполняем для следующего меню */
            JMenu menuHelp = new JMenu("Помощь");
            menuBar.add(menuHelp);
            ArrayList<JMenuItem> itemsHelp = new ArrayList<>();
            help = new JMenuItem("Помощь");
            about = new JMenuItem("Об игре");
            itemsHelp.add(help);
            itemsHelp.add(about);
            for (JMenuItem object : itemsHelp) {
                menuHelp.add(object);
                object.addActionListener(this);
                if (object == help)
                    menuHelp.addSeparator();
            }
            /* 9. Добавляем панель меню к окну */
            setJMenuBar(menuBar);

            setVisible(true); // устанавливаем окно видимым
        }

        /**
         * Метод, прослушивающий назначенные действия, выполняемые в окне.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            /* Обработка МЕНЮ */
            /* 1. Объявляю объект, чтобы определить, какой объект был нажат */
            Object who = e.getSource();
            /* 2. Обработка кнопки host - "Запуск сервера" */
            if (who == host) {
                /* Создание соответствующего игрока и игрока MP */
                player = new Player(leftPlayerImage, true, key);
                playerMP = new Player(rightPlayerImage, false, null);
                ball = new Ball(ballImage);

                host.setEnabled(false); // блокирую возможность создания сервера
                join.setEnabled(false); // блокирую возможность последующего создания клиента

                Game.server.start(); // запуск сервера
                Game.GS = GameState.HOST; // установка статуса игры на "HOST", ожидание подключения клиента
            }

            /* 3. Обработка кнопки join - "Присоединиться к существующему серверу" */
            if (who == join) {
                /* Создание соответствующего игрока playeraMP и мяча */
                player = new Player(rightPlayerImage, false, key);
                playerMP = new Player(leftPlayerImage, true, null);
                ball = new SimpleBall(ballImage);

                /* Настройка меню */
                join.setEnabled(false); // блокировка возможности создания следующего клиента
                host.setEnabled(false); // блокировка возможности создания сервера

                /* Передача информации о готовности */
                Game.client.start(); // запуск клиента
                Game.GS = GameState.JOIN; // установка статуса игры на "JOIN", поиск сервера
                Game.client.sendData("00*00".getBytes()); // отправка сообщения серверу о желании присоединиться
            }

            /* 4. Обработка кнопки close - "Закрыть" */
            if (who == close) {
                /* Отправляю сообщение о закрытии окна в системную очередь событий */
                java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING));
            }
            /* 5. Обработка кнопки history - "История встреч" */
            if (who == history) {
                /* Проверяю существование файла */
                String fileName = "history.txt";
                File file = new File(fileName);
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(this,
                            "Вы еще не проводили ни одной игры.",
                            "История встреч",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        // Подготовка потока для чтения
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                        new FileInputStream(fileName)));

                        // Чтение из файла
                        String line;
                        String matchesHistory = "";
                        while (true) {
                            line = in.readLine();
                            if (line == null) break;
                            matchesHistory += line + "\r\n";
                        }
                        // Вывод истории в окне диалога
                        JOptionPane.showMessageDialog(this, matchesHistory,
                                "История встреч", JOptionPane.INFORMATION_MESSAGE);
                        in.close();
                    } catch (IOException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }

            /* 6. Обработка кнопки help - "Об игре" */
            if (who == help) {
                JOptionPane.showMessageDialog(this, "УПРАВЛЕНИЕ:\n"
                                + "Прыжок - стрелка вверх / \"W\"\n"
                                + "Влево - стрелка влево / \"A\"\n"
                                + "Вправо - стрелка вправо / \"D\"\n\n"
                                + "ПРАВИЛА ИГРЫ:\n"
                                + "1. Игра до двух выигранных партий до 21 очка.\n"
                                + "2. Победная команда должна иметь два очка преимущества.\n"
                                + "3. Нет финального очка.\n"
                                + "4. При состоянии 1:1 в партиях, играется третья партия,\n"
                                + "которая ведется по тем же правилам, но до 15 очков.\n\n",
                        "Управление и правила игры", JOptionPane.INFORMATION_MESSAGE);
            }
            /* 7. Обработка кнопки about - "Об игре" */
            if (who == about) {
                JOptionPane.showMessageDialog(this, "" + title.toUpperCase() + " "
                                + version + "\n\n"
                                + "Авторы:\n"
                                + "<Александр Борцов, Никита Бубнов, Евгений Гайфуллин>\n\n"
                                + "Лабораторная работа:\n"
                                + "Разработка распределенных приложений\n\n"
                                + "2023",
                        "Об игре", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }
}