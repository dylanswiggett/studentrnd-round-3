package mvc;

import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import server.MessageType;

/**
 * 
 * Controller component of the MVC system. This handles all user input, and
 * transmits it to the engine. The primary functionality should be setting up
 * all event listeners.
 * 
 * @author Dylan Swiggett
 * 
 */
public class Controller extends Thread {
	Model model;	//Has one way access to the Model
	View view;		//Has one way access to the View
	
	KeyboardFocusManager keyManager;
	
	boolean mouseDown;
	
	boolean[] keysPressed;
	
	public Controller(Model model, View view){
		this.model = model;
		this.view = view;
		/*
		 * A Key Dispatcher doesn't rely on the focus.
		 * All key events will be captured.
		 */
		keyManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		
		keysPressed = new boolean[255];
		
		mouseDown = false;
	}
	
	public void run(){
		while (true){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/*
			 * Catch all keyboard events
			 */
			while (Keyboard.next()){
				if (Keyboard.getEventKeyState()){
					keyPressed(Keyboard.getEventKey());
				} else {
					keyReleased(Keyboard.getEventKey());
				}
			}
			
			Point mousePos = view.pickPointOnScreen(new Point(Mouse.getX(), Mouse.getY()), 0);
			
			/*
			 * Catch mouse events
			 */
			if (Mouse.isButtonDown(0)){
				if (!mouseDown){
					mouseDown = true;
					mousePressed((int) mousePos.getX(), (int) mousePos.getY());
				}
			} else if (mouseDown){
				mouseDown = false;
				mouseReleased((int) mousePos.getX(), (int) mousePos.getY());
			}
			
			/* DON'T REMOVE THIS LINE WITHOUT SOMEHOW COPYING VIEWTRANSLATION TO NEWVIEWTRANSLATION */
			view.newViewTranslation = view.viewTranslation.add(new Vector3D(0, 0, - Mouse.getDWheel() * (view.viewTranslation.getZ() / 2000)));
			
			if (mousePos.x > 0 && mousePos.y > 0 && mousePos.x < model.map.width * model.map.tileWidth && mousePos.y < model.map.height * model.map.tileHeight){
				model.map.tiles[mousePos.x / model.TILEW][mousePos.y / model.TILEH].mouseOver = true;
			}
			
			if (keysPressed[Keyboard.KEY_LEFT]){
				view.newViewTranslation = view.newViewTranslation.add(new Vector3D(-4 * (view.viewTranslation.getZ() / 1000), 0, 0));
			}
			if (keysPressed[Keyboard.KEY_UP]){
				view.newViewTranslation = view.newViewTranslation.add(new Vector3D(0, 4 * (view.viewTranslation.getZ() / 1000), 0));
			}
			if (keysPressed[Keyboard.KEY_RIGHT]){
				view.newViewTranslation = view.newViewTranslation.add(new Vector3D(4 * (view.viewTranslation.getZ() / 1000), 0, 0));
			}
			if (keysPressed[Keyboard.KEY_DOWN]){
				view.newViewTranslation = view.newViewTranslation.add(new Vector3D(0, -4 * (view.viewTranslation.getZ() / 1000), 0));
			}
		}
	}
	
	/**
	 * Called by the Mouse Listener when the mouse is pressed.
	 * 
	 * @param evt
	 */
	public void mousePressed(int x, int y){
		System.out.println(x + ", " + y);
	}
	
	/**
	 * Called by the Mouse Listener when the mouse is released.
	 * 
	 * @param evt
	 */
	public void mouseReleased(int x, int y){
		model.client.addTower((int) (x / model.map.tileWidth), (int) (y / model.map.tileHeight));
	}
	
	/**
	 * Called by the Mouse Listener when the mouse is clicked.
	 * 
	 * @param evt
	 */
	public void mouseClicked(MouseEvent evt){
		
	}
	
	/**
	 * Called by the Mouse Listener when the mouse is dragged.
	 * 
	 * @param evt
	 */
	public void mouseDragged(MouseEvent evt){
		
	}
	
	/**
	 * Called by the Key Listener when a key is pressed.
	 * 
	 * @param evt
	 */
	public void keyPressed(int key){
		keysPressed[key] = true;
	}
	
	/**
	 * Called by the Key Listener when a key is released.
	 * 
	 * @param evt
	 */
	public void keyReleased(int key){
		keysPressed[key] = false;
	}
}
