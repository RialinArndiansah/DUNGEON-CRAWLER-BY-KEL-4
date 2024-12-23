import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game extends JFrame implements KeyListener {
    private final int TILE_SIZE = 30; // Ukuran tiap tile
    private final int MAP_WIDTH = 20; // Lebar map
    private final int MAP_HEIGHT = 20; // Tinggi map

    private final char[][] map = {
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', 'P', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', ' ', '#', '#', ' ', '#', ' ', '#', '#', '#', '#', '#', ' ', ' ', '#', '#', ' ', ' ', ' ', '#'},
            {'#', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', '#', '#', '#', ' ', '#', '#', '#', '#', '#', ' ', '#', '#', ' ', ' ', '#', '#', '#', '#', '#'},
            {'#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', ' ', '#', '#', '#', '#', '#', '#', ' ', '#', '#', '#', '#', '#', ' ', '#', '#', '#', '#', '#'},
            {'#', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', '#', '#', '#', ' ', '#', '#', '#', '#', '#', ' ', '#', '#', ' ', ' ', '#', '#', '#', '#', '#'},
            {'#', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', ' ', ' ', '#', ' ', '#', '#', '#', '#', '#', '#', '#', ' ', '#', '#', '#', '#', '#', '#', '#'},
            {'#', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', ' ', '#', ' ', '#', '#', '#', '#', '#', '#', '#'},
            {'#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', ' ', '#', '#', ' ', '#', ' ', '#', '#', '#', '#', '#', ' ', ' ', '#', '#', ' ', ' ', ' ', '#'},
            {'#', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', '#', '#', '#', ' ', '#', '#', '#', '#', '#', ' ', '#', '#', ' ', ' ', '#', '#', '#', '#', '#'},
            {'#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', '#'},
            {'#', ' ', '#', '#', '#', '#', '#', '#', ' ', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
            {'#', '#', '#', '#', '#', '#', '#', '#', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'E'}
    };

    private int playerX = 1, playerY = 1; // Posisi pemain
    private int playerHP = 20;
    private int playerXP = 0; // Pengalaman pemain
    private int playerLevel = 1; // Level pemain
    private int playerScore = 0;
    private final int XP_PER_LEVEL = 10; // XP yang dibutuhkan untuk naik level

    private List<int[]> enemyPositions; // Posisi musuh
    private int enemyHP = 4;

    private List<int[]> chestPositions; // Posisi peti
    private Image playerUp, playerDown, playerLeft, playerRight; // Gambar pemain berdasarkan arah
    private Image currentPlayerImage; // Gambar pemain saat ini
    private Image enemyImg, wallImg, floorImg, chestImg, exitImg;

    private Image offscreenBuffer;

    public Game() {
        showStartScreen();
        // Load gambar
        try {
            playerUp = new ImageIcon("res/player_back.png").getImage();
            playerDown = new ImageIcon("res/player.png").getImage();
            playerLeft = new ImageIcon("res/player_left.png").getImage();
            playerRight = new ImageIcon("res/player_right.png").getImage();
            currentPlayerImage = playerDown; // Default gambar pemain menghadap ke bawah

            enemyImg = new ImageIcon("res/enemy.png").getImage();
            wallImg = new ImageIcon("res/wall.png").getImage();
            floorImg = new ImageIcon("res/floor.png").getImage();
            chestImg = new ImageIcon("res/chest.png").getImage();
            exitImg = new ImageIcon("res/pintu_keluar.png").getImage();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // Exit jika gambar tidak ditemukan
        }

        // Random posisi musuh
        enemyPositions = new ArrayList<>();
        randomizeEnemyPositions(new Random().nextInt(6) + 5); // Antara 5 hingga 10 musuh

        // Random posisi peti
        chestPositions = new ArrayList<>();
        randomizeChestPositions(10); // Sepuluh peti

        // Setup JFrame
        this.setTitle("Dungeon Crawler");
        this.setSize(MAP_WIDTH * TILE_SIZE, MAP_HEIGHT * TILE_SIZE + 30);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.addKeyListener(this);
        this.setVisible(true);
    }

    private void showStartScreen() {
        JOptionPane.showMessageDialog(this, "Selamat datang di Dungeon Crawler!\nTekan OK untuk memulai permainan.");
    }

    private void showWinScreen() {
        JOptionPane.showMessageDialog(this, "Kamu telah keluar dari dungeon! Selamat!");
        System.exit(0); // Keluar dari permainan
    }

    private void randomizeEnemyPositions(int numEnemies) {
        Random rand = new Random();
        for (int i = 0; i < numEnemies; i++) {
            spawnNewEnemy(rand);
        }
    }

    private void spawnNewEnemy(Random rand) {
        while (true) {
            int x = rand.nextInt(MAP_HEIGHT);
            int y = rand.nextInt(MAP_WIDTH);
            if (map[x][y] == ' ' && (x != playerX || y != playerY)) {
                enemyPositions.add(new int[]{x, y});
                break;
            }
        }
    }

    private void randomizeChestPositions(int numChests) {
        Random rand = new Random();
        for (int i = 0; i < numChests; i++) {
            spawnNewChest(rand);
        }
    }

    private void spawnNewChest(Random rand) {
        while (true) {
            int x = rand.nextInt(MAP_HEIGHT);
            int y = rand.nextInt(MAP_WIDTH);
            if (map[x][y] == ' ' && (x != playerX || y != playerY) && !isEnemyAtPosition(x, y)) {
                chestPositions.add(new int[]{x, y});
                break;
            }
        }
    }

    private boolean isEnemyAtPosition(int x, int y) {
        for (int[] enemy : enemyPositions) {
            if (enemy[0] == x && enemy[1] == y) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayerAdjacentToEnemy() {
        for (int[] enemy : enemyPositions) {
            if (Math.abs(playerX - enemy[0]) + Math.abs(playerY - enemy[1]) == 1) {
                return true;
            }
        }
        return false;
    }

    private void moveEnemiesTowardsPlayer() {
        for (int[] enemy : enemyPositions) {
            if (Math.abs(playerX - enemy[0]) + Math.abs(playerY - enemy[1]) > 1) {
                int newX = enemy[0];
                int newY = enemy[1];

                if (enemy[0] < playerX) newX++;
                else if (enemy[0] > playerX) newX--;

                if (enemy[1] < playerY) newY++;
                else if (enemy[1] > playerY) newY--;

                if (isValidMove(newX, newY)) {
                    enemy[0] = newX;
                    enemy[1] = newY;
                }
            }
        }
    }

    private void attackPlayer() {
        playerHP -= 1;
        JOptionPane.showMessageDialog(this, "Musuh menyerangmu! -1 HP.");
        if (playerHP <= 0) {
            JOptionPane.showMessageDialog(this, "Kamu kalah! Game over.");
            System.exit(0);
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < MAP_HEIGHT && y >= 0 && y < MAP_WIDTH && map[x][y] != '#';
    }

    private void checkPlayerCollectsChest() {
        for (int i = 0; i < chestPositions.size(); i++) {
            int[] chest = chestPositions.get(i);
            if (chest[0] == playerX && chest[1] == playerY) {
                chestPositions.remove(i);
                playerScore += 5; // Tambahkan skor
                playerXP += 2; // Tambahkan XP
                JOptionPane.showMessageDialog(this, "Kamu mengambil peti! +5 Skor, +2 XP.");
                checkLevelUp(); // Periksa apakah pemain naik level
                spawnNewChest(new Random()); // Spawn peti baru
                break;
            }
        }
    }

    private void checkLevelUp() {
        if (playerXP >= playerLevel * XP_PER_LEVEL) {
            playerLevel++;
            playerXP = 0; // Reset XP setelah naik level
            playerHP += 5; // Tambahkan HP saat naik level
            JOptionPane.showMessageDialog(this, "Selamat! Kamu naik level ke " + playerLevel + ". HP bertambah 5.");
        }
    }

    private void checkExit() {
        if (map[playerX][playerY] == 'E') {
            showWinScreen();
        }
    }

    @Override
    public void update(Graphics g) {
        if (offscreenBuffer == null) {
            offscreenBuffer = createImage(getWidth(), getHeight());
        }
        Graphics offG = offscreenBuffer.getGraphics();
        paint(offG);
        g.drawImage(offscreenBuffer, 0, 0, this);
    }

    @Override
    public void paint(Graphics g) {
        // Ukuran visi pemain (2x2 kotak di sekitar pemain)
        int visionRange = 1;

        // Hitung batas gambar berdasarkan visi pemain
        int startX = playerX - visionRange;
        int endX = playerX + visionRange;
        int startY = playerY - visionRange;
        int endY = playerY + visionRange;

        // Gambar peta dalam batas visi pemain
        for (int row = startX; row <= endX; row++) {
            for (int col = startY; col <= endY; col++) {
                // Pastikan tidak menggambar di luar batas peta
                if (row >= 0 && row < MAP_HEIGHT && col >= 0 && col < MAP_WIDTH) {
                    int x = col * TILE_SIZE;
                    int y = row * TILE_SIZE + 30;

                    if (map[row][col] == '#') {
                        g.drawImage(wallImg, x, y, TILE_SIZE, TILE_SIZE, this);
                    } else {
                        g.drawImage(floorImg, x, y, TILE_SIZE, TILE_SIZE, this);
                    }

                    // Gambar pintu keluar
                    if (map[row][col] == 'E') {
                        g.drawImage(exitImg, x, y, TILE_SIZE, TILE_SIZE, this);
                    }
                }
            }
        }

        // Gambar pemain
        g.drawImage(currentPlayerImage, playerY * TILE_SIZE, playerX * TILE_SIZE + 30, TILE_SIZE, TILE_SIZE, this);

        // Gambar musuh dalam batas visi pemain
        for (int[] enemy : enemyPositions) {
            if (enemy[0] >= startX && enemy[0] <= endX && enemy[1] >= startY && enemy[1] <= endY) {
                int x = enemy[1] * TILE_SIZE;
                int y = enemy[0] * TILE_SIZE + 30;
                g.drawImage(enemyImg, x, y, TILE_SIZE, TILE_SIZE, this);
            }
        }

        // Gambar peti dalam batas visi pemain
        for (int[] chest : chestPositions) {
            if (chest[0] >= startX && chest[0] <= endX && chest[1] >= startY && chest[1] <= endY) {
                int x = chest[1] * TILE_SIZE;
                int y = chest[0] * TILE_SIZE + 30;
                g.drawImage(chestImg, x, y, TILE_SIZE, TILE_SIZE, this);
            }
        }

        // Gambar status pemain
        g.setColor(Color.BLACK);
        g.drawString("HP: " + playerHP, 10, MAP_HEIGHT * TILE_SIZE + 20);
        g.drawString("Score: " + playerScore, 100, MAP_HEIGHT * TILE_SIZE + 20);
        g.drawString("Level: " + playerLevel, 200, MAP_HEIGHT * TILE_SIZE + 20);
        g.drawString("XP: " + playerXP, 300, MAP_HEIGHT * TILE_SIZE + 20);
    }

    private void handleCombat() {
        for (int i = 0; i < enemyPositions.size(); i++) {
            int[] enemy = enemyPositions.get(i);
            if (Math.abs(playerX - enemy[0]) + Math.abs(playerY - enemy[1]) == 1) {
                enemyHP -= 2;
                JOptionPane.showMessageDialog(this, "Kamu menyerang musuh! -2 HP musuh.");
                if (enemyHP <= 0) {
                    JOptionPane.showMessageDialog(this, "Musuh dikalahkan! +10 Skor, +5 XP.");
                    playerScore += 10;
                    playerXP += 5;
                    checkLevelUp();
                    enemyPositions.remove(i); // Hapus musuh yang dikalahkan
                    spawnNewEnemy(new Random()); // Spawn musuh baru
                    enemyHP = 4;
                } else {
                    attackPlayer();
                }
                break;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int newX = playerX, newY = playerY;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> {
                newX--;
                currentPlayerImage = playerUp;
            }
            case KeyEvent.VK_A -> {
                newY--;
                currentPlayerImage = playerLeft;
            }
            case KeyEvent.VK_S -> {
                newX++;
                currentPlayerImage = playerDown;
            }
            case KeyEvent.VK_D -> {
                newY++;
                currentPlayerImage = playerRight;
            }
        }

        if (isValidMove(newX, newY)) {
            playerX = newX;
            playerY = newY;

            // Pindahkan musuh setelah pemain bergerak
            moveEnemiesTowardsPlayer();

            // Periksa apakah pemain menyerang musuh
            handleCombat();

            // Periksa apakah pemain mengambil peti
            checkPlayerCollectsChest();

            // Periksa apakah pemain keluar dari dungeon
            checkExit();

            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}