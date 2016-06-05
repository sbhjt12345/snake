package snakeGame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

public class snakeCanvas extends Canvas implements Runnable, KeyListener {

	private final int bh = 15;
	private final int bw = 15;
	private final int gw = 25;
	private final int gh = 25;
	private LinkedList<Point> snake;
	private Point fruit;
	private int direction = Direction.no_direction;
	private Thread runThread;
	// private Graphics globalGraphics;
	private int score = 0;
	private String highscore = "";

	public void init() {

	}

	public void paint(Graphics g) {
		// System.out.println("running");
		
		if (snake == null) {
			snake = new LinkedList<Point>();
			generatesnake();
			placeFruit();
		}

		// globalGraphics = g.create();

		if (runThread == null) {
			this.setPreferredSize(new Dimension(640, 480));
			this.addKeyListener(this);
			runThread = new Thread(this);
			runThread.start();
		}
		if (highscore.equals("")) {
			// init the highscore
            highscore = this.getHighScore();
		}

		drawFruit(g);
		DrawGrid(g);
		drawSnake(g);
		drawScore(g);
	}

	public String getHighScore() {
		FileReader readFile = null;
		BufferedReader reader = null;
		try {
			readFile = new FileReader("highscore.dat");
            reader = new BufferedReader(readFile);
            return reader.readLine();
		} catch (Exception e) {
			return "Nobody:0";
		}
		finally{
			try{
				if (reader!=null) reader.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}

	public void update(Graphics g) {
		Graphics offscreenGraphics;
		BufferedImage offscreen = null;
		Dimension d = this.getSize();

		offscreen = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
		offscreenGraphics = offscreen.getGraphics();
		offscreenGraphics.setColor(this.getBackground());
		offscreenGraphics.fillRect(0, 0, d.width, d.height);
		offscreenGraphics.setColor(this.getForeground());
		paint(offscreenGraphics);

		// flip
		g.drawImage(offscreen, 0, 0, this);
	}

	public void move() {
		Point head = snake.peekFirst();
		Point newPoint = head;
		switch (direction) {
		case (Direction.north):
			newPoint = new Point(head.x, head.y - 1);
			break;
		case Direction.south:
			newPoint = new Point(head.x, head.y + 1);
			break;
		case Direction.west:
			newPoint = new Point(head.x - 1, head.y);
			break;
		case Direction.east:
			newPoint = new Point(head.x + 1, head.y);
			break;
		}
		snake.remove(snake.peekLast());
		if (newPoint.equals(fruit)) {
			score += 10;
			Point addPoint = (Point) newPoint.clone();
			switch (direction) {
			case (Direction.north):
				newPoint = new Point(head.x, head.y - 1);
				break;
			case Direction.south:
				newPoint = new Point(head.x, head.y + 1);
				break;
			case Direction.west:
				newPoint = new Point(head.x - 1, head.y);
				break;
			case Direction.east:
				newPoint = new Point(head.x + 1, head.y);
				break;
			}
			snake.push(addPoint);
			placeFruit();

		} else if (newPoint.x < 0 || newPoint.x > (gw - 1)) {
			checkscore();
			generatesnake();
			return;
		} else if (newPoint.y < 0 || newPoint.y > (gh - 1)) {
			checkscore();
			generatesnake();
			return;
		} else if (snake.contains(newPoint)) {
			checkscore();
			generatesnake();
			return;
		}
		snake.push(newPoint);

	}
	


	public void generatesnake() {
		score = 0;
		snake.clear();
		snake.add(new Point(0, 2));
		snake.add(new Point(0, 1));
		direction = Direction.no_direction;
	}

	public void drawScore(Graphics g) {
		g.drawString("Score:" + score, 0, bh * gh + 10);
		g.drawString("Highscore:"+highscore, 0, bh*gh+20);
	}
	
	public void checkscore(){
		if (highscore=="") return;
		if (score > Integer.parseInt(highscore.split(":")[1])){
			String name = JOptionPane.showInputDialog("you set a new highscore. What is your name?");
		    highscore = name + ":" + score;
		    
		    File scoreFile = new File("highscore.dat");
		    if (!scoreFile.exists()){
		    	try{
		    		scoreFile.createNewFile();
		    	} catch (IOException e){
		    		e.printStackTrace();
		    	}
		    }
		    FileWriter writeFile = null;
		    BufferedWriter writer = null;
		    try{
		    	writeFile = new FileWriter(scoreFile);
		    	writer = new BufferedWriter(writeFile);
		    	
		    	writer.write(this.highscore);
		    }
		    catch (Exception e){
		    	
		    }
		    finally{
		    	try{
		    	if (writer != null){
		    		writer.close();
		    	}
		    	}
		    	catch (Exception e){}
		    }
		}
	}

	public void DrawGrid(Graphics g) {
		g.drawRect(0, 0, gw * bw, bh * gh);

		// vertical line
		for (int i = bw; i < gw * bw; i += bw) {
			g.drawLine(i, 0, i, bh * gh);
		}

		// horizontal line
		for (int j = bh; j < gh * bh; j += bh) {
			g.drawLine(0, j, bh * gh, j);
		}
	}

	public void drawSnake(Graphics g) {
		g.setColor(Color.green);
		for (Point p : snake) {
			g.fillRect(p.x * bw, p.y * bh, bw, bh);
		}
		g.setColor(Color.black);
	}

	public void drawFruit(Graphics g) {
		g.setColor(Color.red);
		g.fillOval(fruit.x * bw, fruit.y * bh, bw, bh);
		g.setColor(Color.black);
	}

	public void placeFruit() {
		Random rand = new Random();
		int randomX = rand.nextInt(gw), randomY = rand.nextInt(gh);
		Point randomPoint = new Point(randomX, randomY);
		while (snake.contains(randomPoint)) {
			randomX = rand.nextInt(gw);
			randomY = rand.nextInt(gh);
			randomPoint = new Point(randomX, randomY);
		}
		fruit = randomPoint;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			move();
			// Draw(globalGraphics);
			repaint();

			try {
				Thread.currentThread();
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			if (direction != Direction.south)
				direction = Direction.north;
			break;
		case KeyEvent.VK_DOWN:
			if (direction != Direction.north)
				direction = Direction.south;
			break;
		case KeyEvent.VK_RIGHT:
			if (direction != Direction.west)
				direction = Direction.east;
			break;
		case KeyEvent.VK_LEFT:
			if (direction != Direction.east)
				direction = Direction.west;
			break;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}
}
