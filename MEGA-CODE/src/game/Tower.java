package game;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;

import mvc.ExtrudeSprite;
import mvc.Model;
import mvc.TextureSprite;
import game.Map;

public class Tower extends ExtrudeSprite{
	
	public double range;
	public double damage;
	public double cooldown;
	
	public int evolution;
	public double evolutionScalar;
	
	String[] texturePaths;
	TextureSprite base, guns, tower;
	
	public int beamType;
	public char towerType;
	public int uniqueId;
	
	public Tower(Map map, float x, float y) {
		super(x * map.tileWidth + 5, y * map.tileHeight + 5, map.tileWidth - 6, map.tileHeight - 6, 20, Color.BLUE);
		
		range = 150;
		damage = 20;
		cooldown = 30;
		
		evolution = 0;
		evolutionScalar = 1;
		
	}
	
	public Tower(Map map, float x, float y, float width, float height, double range, double damage, double cooldown, double evolutionScalar, Color color, String[] texturePaths) {
		super(x * map.tileWidth + 5, y * map.tileHeight + 5, width, width, height, color);
		
		this.range = range;
		this.damage = damage;
		this.cooldown = cooldown;
		
		this.evolutionScalar = evolutionScalar;
		
		this.texturePaths = texturePaths;
		
		base = new TextureSprite(this.x, this.y, w, h, 20, texturePaths[0]);
		guns = new TextureSprite(this.x, this.y, w, h, 30, texturePaths[1]);
		tower = new TextureSprite(this.x, this.y, w, h, 40, texturePaths[2]);
		
	}
	
	public Monster target(ArrayList<Monster> monsters, Point end){
		int minTaxiDistance = Integer.MAX_VALUE;
		Monster targeted = null;
		
		for (int i = 0; i < monsters.size(); i++){
			Monster monster = monsters.get(i);
			
			if (Math.sqrt(Math.pow(monster.x + monster.w / 2 - x - w / 2, 2) +
						  Math.pow(monster.y + monster.h / 2 - y - h / 2, 2)) < range){
				int taxiDistance = (int) Math.abs(end.x - (monster.x + monster.w / 2) + Math.abs(end.y - (monster.y + monster.h / 2)));
				if (taxiDistance < minTaxiDistance){
					minTaxiDistance = taxiDistance;
					targeted = monster;
				}
			}
		}
		
		if (targeted != null){
			System.out.println("TARGET!");
			r = - Math.atan2(targeted.x + targeted.w / 2 - x - w / 2, targeted.y + targeted.h / 2 - y - h / 2) * 180 / Math.PI;
		}
		
		return targeted;
	}
	
	public String getClassName() {
		return "Tower";
	}
	
	public String toString() {
		return this.getClassName() + this.texturePaths[0] + this.texturePaths[1] + this.texturePaths[2]; 
	}
	
	public void draw(){
		base.setRot((float) r);
		guns.setRot((float) r);
		tower.setRot((float) r);
		
		base.draw();
		guns.draw();
		tower.draw();
		
		r += 1;
	}
}
