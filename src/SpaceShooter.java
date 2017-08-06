import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.util.Random;

import javax.swing.JFrame;


public class SpaceShooter implements Runnable{
	int score=0;
	private JFrame frame;
	private Canvas canvas;
	private Thread thread;
	
	private BufferStrategy bs;
	private Graphics g;
	
	private Key key;
	
	int px=100,py=100;
	
	Ball[] balls;
	byte number=0;
	boolean shoot=false;
	
	NPC[] npcs;
	Random rand;
	public SpaceShooter(){
		frame = new JFrame("Shooter");
		frame.setSize(500,500);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		rand = new Random();
		balls = new Ball[256];
		npcs = new NPC[10];
		key = new Key();
		frame.addKeyListener(key);
		
		for(int i=0;i<npcs.length;i++){
			npcs[i] = new NPC(rand.nextInt(320)+250,rand.nextInt(480)+20,true);
		}
		
		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(500,500));
		canvas.setMaximumSize(new Dimension(500,500));
		canvas.setMinimumSize(new Dimension(500,500));
		frame.add(canvas);
		frame.pack();
	}
	
	public synchronized void start(){
		thread = new Thread(this);
		thread.start();
	}
	
	public void run(){
		
		long now,lastTime= System.nanoTime();
		double delta = 0, nsPerTick = 1000000000/60;
		
		while(true){
			
			now = System.nanoTime();
			delta += (now-lastTime)/nsPerTick;
			lastTime=now;
			if(delta>=1){
				tick();
				render();
				delta--;
			}
			
			
		}
		
	}
	
	public void tick(){
		key.tick();
		
		for(NPC npc:npcs){
			npc.tick();
		}
		if(shoot){	
			balls[number] = new Ball(px+20,py+10,true);
			number++;	
			shoot=false;
		}
		
		if(number>44){
			number=0;
		}
		
		for(int i=0;i<number;i++){
			if(balls[i] !=null){
				balls[i].tick();
			if(balls[i].x > 500){
				balls[i] = null;
				
			}
			}
		}
		
	}
	
	
	public void render(){
		bs = canvas.getBufferStrategy();
		
		if(bs==null){
			canvas.createBufferStrategy(3);
			
			return;
		}
		g=bs.getDrawGraphics();
		
		g.clearRect(0, 0, 500, 500);
		
		g.setColor(Color.BLACK);
		g.fillRect(0,0,500,500);
		
		//render player
		g.setColor(Color.WHITE);
		g.fillRect(px, py, 20, 20);
		
		g.setColor(Color.GRAY);
		g.drawRect(px, py, 20, 20);
		
		for(int i=0;i<number;i++){
			if(balls[i] !=null)
			balls[i].render();
		}
		for(NPC npc:npcs){
			npc.render();
		}
		g.setColor(Color.GRAY);
		g.drawString("Your Score: "+score, 50, 50);
		bs.show();g.dispose();
		
	}
	
	public synchronized void stop(){
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public static void main(String[] args){
		SpaceShooter s = new  SpaceShooter();
		s.start();
	}
	
	class Key implements KeyListener{
		
		boolean[] keys;
		boolean up,down,left,right;
		public Key(){
			keys = new boolean[256];
			
			
		}
		
		public void tick(){
			up = keys[KeyEvent.VK_UP];
			down = keys[KeyEvent.VK_DOWN];
			left = keys[KeyEvent.VK_LEFT];
			right = keys[KeyEvent.VK_RIGHT];
			if(up){
				px+=0;
				if(!(py<=0))
				py+=-3;
			}
			if(down){
				px+=0;
				if(!(py>=480))
				py+=3;
			}
			if(left){
				if(!(px<=0))
					px+=-3;
				py+=0;
			}
			if(right){
				if(!(px>=480))
					px+=3;
				py+=0;
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			keys[e.getKeyCode()]=true;
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER){
				shoot = true;
			}
			keys[e.getKeyCode()]=false;

		}

		@Override
		public void keyTyped(KeyEvent e) {
			
		}
		
	}
	
	class Ball {
		int x,y;
		boolean run;
		
		public Ball(int x,int y,boolean b){
			run=b;
			this.x=x;
			this.y=y;
		} 
		
		public void tick(){
			if(x>500)
				run=false;
			if(run){
				x+=4
						;
				System.out.println("YES");
			}
		}
		
		public void render(){
			g.setColor(Color.GREEN);
			g.fillRect(x, y, 7, 2);
		}
		
	}
	
	class NPC{
		private int x1,y1;
		boolean run;
		
		public NPC(int x,int y,boolean b){
			run=b;
			this.x1=x;
			this.y1=y;
		} 
		
		public void tick(){
			
			
			if(run){
			
				if(px>x1)
					x1+=1;
				if(px<x1)
					x1-=1;
				if(py>y1)
					y1+=1;
				if(py<y1)
					y1-=1;
				
				if(new Rectangle(x1,y1,20,20).intersects(new Rectangle(px,py,20,20))){
					
					System.out.println("PRINCE");
					px= 100;
					score=0;
					py=100;
					for(int i=0;i<npcs.length;i++){
						npcs[i] = new NPC(rand.nextInt(320)+250,rand.nextInt(480)+20,true);
					}
				}
				
				for(int i=0;i<number;i++){
					if(balls[i]!=null){
						if(new Rectangle(x1,y1,20,20).intersects(new Rectangle(balls[i].x,balls[i].y,7,2))){
							run=false;
							respawn();
						}
					}
				}
				
			}
		}
		
		public void respawn(){
			run= true;
			
			
			
			x1 = rand.nextInt(320)+250;
			y1 = rand.nextInt(480)+20;
			score++;
		}
		
		public void render(){
			if(run){
				
				g.setColor(Color.BLUE.brighter());
				g.fillRect(x1, y1, 20, 20);
				g.setColor(Color.BLUE.darker());
				g.drawRect(x1, y1, 20, 20);
				
			}
		}
		
	}
	
}
