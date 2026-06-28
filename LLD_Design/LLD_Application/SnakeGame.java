package LLD_Application;


import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Random;

class GamePanel extends JPanel {
    private final GameEngine engine;
    private final int TILE_SIZE = 20;

    public GamePanel(GameEngine engine) {
        this.engine = engine;

        // Set the physical pixel size of the window
        setPreferredSize(new Dimension(engine.board.width * TILE_SIZE, engine.board.height * TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Input Handling
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> engine.changeDirection(DIRECTION.UP);
                    case KeyEvent.VK_DOWN -> engine.changeDirection(DIRECTION.DOWN);
                    case KeyEvent.VK_LEFT -> engine.changeDirection(DIRECTION.LEFT);
                    case KeyEvent.VK_RIGHT -> engine.changeDirection(DIRECTION.RIGHT);
                }
            }
        });
    }

    // The Rendering Engine
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Clears the screen

        if (engine.gameState == GameState.GAME_OVER) {
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER - Score: " + engine.score, 10, 20);
            return;
        }

        // Draw Fruit
        g.setColor(Color.RED);
        if (engine.board.fruit != null) {
            g.fillRect(engine.board.fruit.x * TILE_SIZE, engine.board.fruit.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // Draw Obstacles
        g.setColor(Color.GRAY);
        for (Cell obs : engine.board.obstacles) {
            g.fillRect(obs.x * TILE_SIZE, obs.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // Draw Snake
        g.setColor(Color.GREEN);
        for (Cell part : engine.snake.snake) {
            g.fillRect(part.x * TILE_SIZE, part.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }
}
/**
 1. Data Models (state)
 Class Cell
 - int x, int y
 Enums:
 - Direction(UP, DOWN, LEFT, RIGHT)
 - GameState (RUNNING, GAME_OVER)

 2. Entities
 Class Snake
 - Deque<cell> body;
 - Direction currDirection;
 + getNextHeadPostion() // without actually moving snake, just x,y
 + void move (cell newHead)
 + void grow (cell newHead)
 + boolean contains (Cell c) // to checkfor collision with itself

 Class Board
 - int width, height
 - Cell fruit
 - HashSet<Cell> Obstacles;
 + generateFruit (Snake snake)
 + isOutOfBounds (Point p)

 Class GameEngine
 - Board board
 - Snake snake
 - int score
 - GameState state

 +changeDirection(Direction d)
 + tick()
 - call nextCell = snake.getHeadPosition()
 - check if out of bound  self obstacl collision
 - check fruit
 - default : move to next cell
 */

class Cell{
    int x;
    int y;
    Cell(int x, int y){
        this.x= x;
        this.y= y;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return  false;
        if(obj.getClass() != this.getClass()) return  false;
        Cell temp = (Cell)obj;
        return temp.x == this.x && temp.y == this.y;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }
}

enum DIRECTION {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

enum GameState {
    RUNNING,
    GAME_OVER
}

class Snake{
    /**
     * - Deque<cell> body;
     *  - Direction currDirection;
     *  + getNextHeadPostion() // without actually moving snake, just x,y
     *  + void move (cell newHead)
     *  + void grow (cell newHead)
     *  + boolean contains (Cell c) // to checkfor collision with itself
     */
    Deque<Cell> snake;
    DIRECTION currDirection;

    Snake(Cell head, DIRECTION direction){
        snake = new ArrayDeque<>();
        snake.push(head);
        currDirection = direction;
    }

    Cell getNextHeadPosition(){
        Cell head = snake.getFirst();
        Cell ans = new Cell(head.x, head.y); // Deep copy
        switch (currDirection){
            case UP -> ans.y = ans.y-1;
            case DOWN -> ans.y = ans.y+1;
            case LEFT -> ans.x = ans.x-1;
            case RIGHT -> ans.x = ans.x+1;
        }

        return  ans;
    }

    void move(Cell newHead) {
        snake.addFirst(newHead); // Add new head to the front
        snake.removeLast();      // Drop the tail from the back
    }
    void grow(Cell newHead) {
        snake.addFirst(newHead); // Add head, keep tail
    }

    Boolean contains(Cell newHead){
        return snake.contains(newHead);
    }
}

class Board {
    /**
     Class Board
     - int width, height
     - Cell fruit
     - HashSet<Cell> Obstacles;
     + generateFruit (Snake snake)
     + generateObstacles(int n)
     + isOutOfBounds (Point p)

     Let's not keep generate fruit here as that needs game understanding, moving it to main game engine.
     That leads to God Object, more on that later.
     */

    int width, height;
    Cell fruit;
    HashSet<Cell> obstacles;

    Board(int w, int h){
        width = w;
        height= h;
        obstacles = new HashSet<>();
        fruit = new Cell(0,0);
    }
    void generateObstacle (int n, Cell snakeHead){
        while(n-- > 0){
            Random rand = new Random();
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            Cell cell = new Cell(x,y);
            if(cell.equals(snakeHead)) n++;
            else obstacles.add(cell);
        }
    }

    Boolean isOutOfBound(Cell cell){
        if(cell.x <0 || cell.x >=width) return  true;
        if(cell.y<0 || cell.y>=height) return  true;
        return  false;
    }
}

class GameEngine{
    /**
     Class GameEngine
     - Board board
     - Snake snake
     - int score
     - GameState state

     +changeDirection(Direction d)
     + GenerateFruit
     + tick()
     - call nextCell = snake.getHeadPosition()
     - check if out of bound  self obstacl collision
     - check fruit
     - default : move to next cell
     */

    Board board;
    Snake snake;

    Integer score;
    GameState gameState;

    GameEngine(int width, int height){
        board = new Board(width,height);
        Cell headCell = new Cell(width/2, height/2);
        snake = new Snake(headCell,DIRECTION.RIGHT);
        score =0;
        gameState= GameState.RUNNING;
        board.generateObstacle(Integer.max(width,height),headCell);
        generateFruit();
    }

    void changeDirection(DIRECTION d){
        // Guard clauses to prevent 180-degree instant death
        if (snake.currDirection == DIRECTION.UP && d == DIRECTION.DOWN) return;
        if (snake.currDirection == DIRECTION.DOWN && d == DIRECTION.UP) return;
        if (snake.currDirection == DIRECTION.LEFT && d == DIRECTION.RIGHT) return;
        if (snake.currDirection == DIRECTION.RIGHT && d == DIRECTION.LEFT) return;

        snake.currDirection = d;
    }

    void tick(){
        Cell nextHead=  snake.getNextHeadPosition();
        if(board.isOutOfBound(nextHead)) gameState= GameState.GAME_OVER;
        else if(snake.contains(nextHead)) gameState= GameState.GAME_OVER;
        else if(board.obstacles.contains(nextHead)) gameState= GameState.GAME_OVER;
        else if(board.fruit.equals(nextHead)) {
            snake.grow(nextHead);
            score+=10;
            generateFruit();
        }
        else snake.move(nextHead);
    }

    void generateFruit(){
        Cell cell;
        do{
            Random rand = new Random();
            int x = rand.nextInt(board.width);
            int y = rand.nextInt(board.height);
            cell = new Cell(x,y);
        }while(snake.contains(cell)|| board.obstacles.contains(cell));

        board.fruit = cell;
    }
}
public class SnakeGame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("1. Initializing Engine...");
                GameEngine engine = new GameEngine(30, 30);

                System.out.println("2. Engine Built. Creating UI...");
                JFrame frame = new JFrame("Snake - Pure LLD");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                GamePanel panel = new GamePanel(engine);
                frame.add(panel);

                frame.pack();
                frame.setLocationRelativeTo(null);

                System.out.println("3. Showing Window...");
                frame.setVisible(true);
                frame.setAlwaysOnTop(true);
                panel.requestFocusInWindow();
                frame.setAlwaysOnTop(false);

                Timer timer = new Timer(150, e -> {
                    if (engine.gameState == GameState.RUNNING) {
                        engine.tick();
                        panel.repaint();
                    }
                });
                timer.start();

                System.out.println("4. Game Loop Started Successfully!");

            } catch (Exception e) {
                // If the code is crashing silently, THIS will catch it and print exactly why.
                System.err.println("CRITICAL CRASH DURING STARTUP:");
                e.printStackTrace();
            }
        });
    }
    /**
     System:
     - board (n,m) - arraylist of Cells
     - Snake -> deque of cells
     - score
     + putFruit()
     + removeFruit()
     + setObstacles (int n) // randomly sets obstacles within board for n cells
     + userInput(Direction)
     + tick()

     Class Cell ->
        - cellType - ENUM (SNAKE, EMPTY, OBSTACLE, FRUIT)
     Class Snake
        - currentDirection ENUM (UP,DOWN, LEFT, RIGHT)
        - Deque<Cells> snakeBody
        + move(Direction, fruitCell)
            // if find fruit, then increase, if find itself/obstacle over


     // in this separtion of concern is not there, snake still needs to about board status while moving
     */

    /**
     Better: only keep coordinates, let board, snake, cell just manage objects, enums handle data and the system owns all of these and handle movement.



     1. Data Models (state)
     Class Cell
     - int x, int y
     Enums:
     - Direction(UP, DOWN, LEFT, RIGHT)
     - GameState (RUNNING, GAME_OVER)

     2. Entities
      Class Snake
        - Deque<cell> body;
        - Direction currDirection;
        + getNextHeadPostion() // without actually moving snake, just x,y
        + void move (cell newHead)
        + void grow (cell newHead)
        + boolean contains (Cell c) // to checkfor collision with itself

     Class Board
        - int width, height
        - Cell fruit
        - HashSet<Cell> Obstacles;
        + generateFruit (Snake snake)
        + isOutOfBounds (Point p)

     Class GameEngine
        - Board board
        - Snake snake
        - int score
        - GameState state

        +changeDirection(Direction d)
        + tick()
            - call nextCell = snake.getHeadPosition()
            - check if out of bound  self obstacl collision
            - check fruit
            - default : move to next cell

     */




}


/**

 ```
 Why passing snake to Board is acceptable but passing board to snake is not?

 Structural Coupling vs Method Injection
 - board needed to be pass into constuctor or held as class level field in snake so that snake could traverse board and figure out where the fruit is and where the snakes are
 - VS
 - Board doesn't hold permanent referece to snake, only as argument at that moment

 NOTE: If a class only needs data, pass the data, not the object that holds the data.
 ```

 ```
 why can't we just pass the fruit cell if that's why need to pass the board?

 - It violates SRP (single responsibility), in addition to move, snake now also has to make decisons for the game. In future if we introduce different kind of fruits and obstacles, we need to pass everything to snake as an argument, violation Open close.

 - Rich Domain Model : mimic exactly how snake is IRL (it eats, grows, moves)
 - Anemic Domain Model: only data and it's internal manipulation, all loigc outside.

 When entities govern their own logic, they are blind to the rest of the world (two player, both snake eating same fruit at same time, before fruit is updated to be eaten by one snake, resulting both snake having head at same position).

 NOTE: Entities own how they move; the Engine owns why they move and what happens when they do.

 ## Entity- Component-System
 the game development industry largely abandoned classic OOP in favor of a pattern called ECS.

 In this architecture:

 Entities (Snake, Fruit, Walls): Are just pure data. They hold coordinates and states, nothing else. They hold pure mechanics (slithering of snake)

 Systems (The Engine): Hold 100% of the logic. (eating red square gives 10 point increase)
 ```

 3. Game Engine: Orchestrator
 -
 */
