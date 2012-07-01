package mvc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import server.MessageType;

public class Client extends Thread{
	private Socket client;
	private ObjectOutputStream out;
	public ObjectInputStream in;
	
	String host = "localhost";
	int port = 12345;
	
	int userId;
	
	Model model;
	View view;
	
	float energy;
	
	public Client(Model model, View view, float energy){
		try {
			this.model = model;
			this.view = view;
			
			client = new Socket(host, port);
			client.setSoTimeout(1000);
			
			out = new ObjectOutputStream(client.getOutputStream());
			in = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
			
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			start();
			
			System.out.println("Connecting to server...");
			
			sendMessage(MessageType.CONNECT, "user");
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Failed to connect to client");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void run(){
		while (true){
			try {
				String val = (String) in.readObject();
				
				MessageType type = MessageType.translate(val.charAt(0));
				String message = val.substring(1);
				
				switch (type){
				case SERVER_MESSAGE:
					System.out.println("SERVER: " + message);
					break;
				case CONNECT:
					System.out.println("Connection with server confirmed. Connected as user " + message + ".");
					userId = Integer.parseInt(message);
					sendMessage(MessageType.SERVER_MESSAGE, "Hello server!");
					break;
				case ADD_TOWER:
					int x = message.charAt(0);
					int y = message.charAt(1);
					int towerType = message.charAt(2);
					if (this.canPlaceTower(x, y)) {
						this.subtractEnergy();
						System.out.println("Adding tower at " + x + ", " + y);
						model.map.addTower(x, y, towerType);	
					} else {
						System.out.println("Cannot place tower at " + x + ", " + y);
					}
				case PROVIDE_STATE:
					String delimiter = "#@#@#@#";
					// Load state from this
					String[] split = message.split("\n");
					
					
				}
				
			} catch (IOException e){
				if (!client.isConnected()) {	// If server has disconnected, attempt to reconnect.
					try {
						client = new Socket(host, port);
						client.setSoTimeout(1000);
						
						out = new ObjectOutputStream(client.getOutputStream());
						in = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
						
					} catch (Exception e1) {
						e1.printStackTrace();
						System.exit(0);
					}
				}
				try {
					Thread.sleep(5);	// Wait for a little bit if no message has been received.
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}	
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void subtractEnergy() {
		float scalar  = 1f;
		
		this.energy *= scalar;
	}
	
	public boolean canPlaceTower(int x, int y) {
		return true;
	}
	
	public void sendMessage(MessageType type, String message) {
		try {
			out.writeObject(type.value() + message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addTower(int x, int y, int towerType){
		sendMessage(MessageType.ADD_TOWER, "" + (char) x + (char) y + (char) towerType);
	}
}
