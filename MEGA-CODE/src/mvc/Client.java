package mvc;

import game.Monster;
import game.Tile;
import game.Tower;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
	
	public Client(Model model, View view){
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
				System.out.println("ALIVE 1!");
				String val = (String) in.readObject();
				
				MessageType type = MessageType.translate(val.charAt(0));
				String message = val.substring(1);
				
				int x, y;
				System.out.println("ALIVE 2!");
				switch (type) {
				case SERVER_MESSAGE:
					System.out.println("SERVER: " + message);
					break;
				case CONNECT:
					System.out.println("Connection with server confirmed. Connected as user " + message + ".");
					userId = Integer.parseInt(message);
					sendMessage(MessageType.SERVER_MESSAGE, "Hello server!");
					break;
				case PROVIDE_STATE:
					model.map.monsters = new ArrayList<Monster>();
					ArrayList<Tower> newTowers = new ArrayList<Tower>();
					// BIG NOTE: THIS DOES NOT REMOVE ANY TURRETS THAT ARE REMOVED!!!!!!
					String delimiter = "##";
					// Load state from this
					String[] split = message.split("\n");
					for (String l : split) {
						String[] splitl = l.split(delimiter);
						if (splitl[0].equals("tile")) { // For now, let's just leave the poor tiles alone.
							float tileX = Float.parseFloat(splitl[1]);
							float tileY = Float.parseFloat(splitl[2]);
						} else if (splitl[0].equals("tower")) {
							int towerX = Integer.parseInt(splitl[1]);
							int towerY = Integer.parseInt(splitl[2]);
							Tower temp = new Tower(model.map, towerX, towerY);
							temp.beamType = Integer.parseInt(splitl[9]);
							temp.cooldown = Double.parseDouble(splitl[4]);
							temp.damage = Double.parseDouble(splitl[3]);
							temp.depth = 0;
							temp.evolution = Integer.parseInt(splitl[6]);
							temp.evolutionScalar = Double.parseDouble(splitl[7]);
							temp.r = Double.parseDouble(splitl[5]);
							temp.range = Double.parseDouble(splitl[8]);
							temp.towerType = splitl[10].charAt(0);
							temp.uniqueId = Integer.parseInt(splitl[11]);
							model.map.tiles[(int) towerX][(int) towerY].tower = temp;
							newTowers.add(temp);
						} else if (splitl[0].equals("monster")) {
							//TODO: fix
							float monsterX = Float.parseFloat(splitl[1]);
							float monsterY = Float.parseFloat(splitl[2]);
							float health = Float.parseFloat(splitl[5]);
							Monster m = new Monster(monsterX, monsterY, model.map.tileWidth, model.map.tileHeight, health);
							m.evolution= 0; //fix me!
							m.maxHealth = Double.parseDouble(splitl[4]);
							m.r = Float.parseFloat(splitl[8]);
							m.speed = Float.parseFloat(splitl[3]);
							m.evolutionScalar = 1; //fix me!
							m.uniqueId = Integer.parseInt(splitl[9]);
							model.map.monsters.add(m);
						}
					}
					model.map.towers = newTowers;
					System.out.println(newTowers.toString());
					
					break;
				}
			} catch (IOException e) {
				if (!client.isConnected()) {	// If server has disconnected, attempt to reconnect.
					try {
						client = new Socket(host, port);
						client.setSoTimeout(1000);
						out = new ObjectOutputStream(client.getOutputStream());
						in = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
					} catch (SocketException e1) {
						e1.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				}
			} catch (Exception e3) {
				e3.printStackTrace();
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
	
	public void addMonster(int x, int y, int monsterType){
		sendMessage(MessageType.ADD_MONSTER, "" + (char) x + (char) y + (char) monsterType);
	}
}
