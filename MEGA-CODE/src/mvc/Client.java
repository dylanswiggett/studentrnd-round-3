package mvc;

import game.GatlingTower;
import game.LaserTower;
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
				String val = (String) in.readObject();
				
				MessageType type = MessageType.translate(val.charAt(0));
				String message = val.substring(1);
				
				int x, y;
				switch (type) {
				case SERVER_MESSAGE:
					System.out.println("SERVER: " + message);
					break;
				case CONNECT:
					switch ((int) message.charAt(0)){	// Determine player type
					case 0:
						model.plantMode = true;
						break;
					case 1:
						model.plantMode = false;
						break;
					default:
						System.out.println("Uh oh! Connection refused by a full server.");
						System.exit(0);
					}
					System.out.println("Connection with server confirmed. Connected as user " + message + ".");
					userId = Integer.parseInt(message.substring(1));
					sendMessage(MessageType.SERVER_MESSAGE, "Hello server!");
					break;
				/*
				case ADD_TOWER:
					x = message.charAt(0);
					y = message.charAt(1);
					int towerType = message.charAt(2);
					if (this.canPlaceTower(x, y)) {
						this.subtractEnergy();
						System.out.println("Adding tower at " + x + ", " + y);
						model.map.addTower(x, y, towerType);	
					} else {
						System.out.println("Cannot place tower at " + x + ", " + y);
					}
					break;
				case ADD_MONSTER:
					System.out.println("monster - client incoming");
					x = message.charAt(0);
					y = message.charAt(1);
					int monsterType = message.charAt(2);
					int monsterId = Integer.parseInt(message.substring(3));
					if (this.canPlaceTower(x, y)) {
						this.subtractEnergy();
						System.out.println("Adding monster at " + x + ", " + y);
						model.map.addMonster(x, y, monsterType, monsterId);	
					} else {
						System.out.println("Cannot place monster at " + x + ", " + y);
					}
					break;
					*/
				case PROVIDE_STATE:
					try {
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
							//System.out.println(splitl.toString());
							int towerX = Integer.parseInt(splitl[1]);
							int towerY = Integer.parseInt(splitl[2]);
							char towerType = splitl[10].charAt(0);
							Tower temp;
							if (towerType == 0) {
								temp = new GatlingTower(model.map, towerX, towerY);
							} else if (towerType == 1) {
								temp = new GatlingTower(model.map, towerX, towerY);
							} else {
								System.out.println("Unknown tower type: " + towerType);
								temp = new LaserTower(model.map, towerX, towerY);
							}
							//System.out.println(towerX + ", " + towerY);
							temp.beamType = Integer.parseInt(splitl[9]);
							temp.cooldown = Double.parseDouble(splitl[4]);
							temp.damage = Double.parseDouble(splitl[3]);
							temp.depth = 0;
							temp.evolution = Integer.parseInt(splitl[6]);
							temp.evolutionScalar = Double.parseDouble(splitl[7]);
							temp.r = Double.parseDouble(splitl[5]);
							temp.range = Double.parseDouble(splitl[8]);
							temp.uniqueId = Integer.parseInt(splitl[11]);
							model.map.tiles[(int) towerX][(int) towerY].tower = temp;
							newTowers.add(temp);
							System.out.println(temp.toString());
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
					//System.out.println(newTowers.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
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
		//System.out.println("monster - client outgoing");
		sendMessage(MessageType.ADD_MONSTER, "" + (char) x + (char) y + (char) monsterType);
	}
}
