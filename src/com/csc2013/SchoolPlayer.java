package com.csc2013;

import org.newdawn.slick.SlickException;
import com.csc2013.DungeonMaze.*;
import java.util.ArrayList;
import java.util.Collections;
public class SchoolPlayer {
	/**
	 * A class to represent map tiles to add functionality.
	 */
	public class Tile implements Comparable<Tile>
	{
		int x;
		int y;
		int priority;
		boolean hasTraversed = false;
		boolean isOpen;
		BoxType boxType;
		public Tile(int x, int y, boolean hT, BoxType bT)
		{
			this.x = x;
			this.y = y;
			hasTraversed = hT;
			boxType = bT;
			isOpen = this.boxType == BoxType.Open || 
					 this.boxType == BoxType.Key || 
					 this.boxType == BoxType.Exit;
			
			switch(this.boxType){
				case Exit: priority = 0;break;
				case Key: priority = 1;break;
				case Door: priority = 2;break;
				case Open: priority = 3;break;
				case Blocked: priority = 4;break;
			}
		}
		public Tile()
		{
		}
		
		 /**
		 * @return An ArrayList of Tiles that are adjacent to this point.
		 */
		public ArrayList<Tile> getAdjacent(){
			ArrayList<Tile> adjacent = new ArrayList<Tile>();
			if(getTile(this.x + 1, this.y) != null && 
					getTile(this.x + 1, this.y).isOpen)
				adjacent.add(getTile(this.x - 1, this.y));
			if(getTile(this.x - 1, this.y) != null && 
					getTile(this.x - 1, this.y).isOpen)
				adjacent.add(getTile(this.x - 1, this.y));
			if(getTile(this.x, this.y + 1) != null && 
					getTile(this.x, this.y + 1).isOpen)
				adjacent.add(getTile(this.x, this.y + 1));
			if(getTile(this.x, this.y - 1) != null && 
					getTile(this.x, this.y - 1).isOpen)
				adjacent.add(getTile(this.x, this.y - 1));
			return adjacent;
		}
		
		/**
		 * @param path An ArrayList<Tile> containing just the starting point.
		 * @param tar The target Tile.
		 * @return The shortest path to a point
		 */
		public ArrayList<Tile> pathToTarget(ArrayList<Tile> path, Tile tar){
			if (this.equals(tar) || path.size() == 0)
				return path;
			ArrayList<Tile> paths[] = 
					(ArrayList<Tile>[])new ArrayList[this.getAdjacent().size()];
			for (int m = 0; m < this.getAdjacent().size(); m++){
				if (this.getAdjacent().get(m).isOpen && 
						!path.contains(this.getAdjacent().get(m))){
					ArrayList<Tile> tempTraversed = 
							(ArrayList<Tile>)path.clone();
					tempTraversed.add(this.getAdjacent().get(m));
					paths[m] = this.getAdjacent().get(m).pathToTarget(tempTraversed, tar);
				}
				else
					paths[m] = new ArrayList<Tile>();
			}
			int shortest = Integer.MAX_VALUE;
			int bestPath = 0;
			for (int n = 0; n < this.getAdjacent().size(); n++){
				if (paths[n].size() < shortest && paths[n].size() > 0) {
					shortest = paths[n].size();
					bestPath = n;
				}
			}
			return paths[bestPath];
		}
		@Override
		public int compareTo(Tile o)
		{
			if (o.y > this.y)
			{
				return 1;
			}
			else if (o.y < this.y)
			{
				return -1;
			}
			else if (o.y == this.y)
			{
				if (o.x > this.x)
					return 1;
				if (o.x < this.x)
					return -1;
			}
			return 0;
		}
		/**
		 * Checks if two tiles are equal to each other based on their
		 * Cartesian coordinates.
		 * @param The tile you are comparing to.
		 */
		public boolean equals(Tile o)
		{
			if (o.x == this.x && o.y == this.y)
				return true;
			return false;
		}
		
		@Override
		public String toString(){
			return "x:" + this.x + " y:" + this.y;
		}
		
	}
	int curX = 0;
	int curY = 0;
	Action lastAct = Action.East;
	ArrayList<Tile> tList = new ArrayList<Tile>();
	ArrayList<Tile> currPath = new ArrayList<Tile>();
	PlayerVision vision;
	Tile target;
	public SchoolPlayer() throws SlickException {
		// complete
	}

	/** 
	 * To properly implement this class you simply must return an Action
	 *  in the function nextMove below.
	 * 
	 * You are allowed to define any helper variables or methods as you see fit
	 * 
	 * For a full explanation of the variables please reference the instruction
	 * manual provided
	 * 
	 * @param vision
	 * @param keyCount
	 * @param lastAction
	 * @return Action
	 */
	public Action nextMove(final PlayerVision vision, final int keyCount,
			final boolean lastAction) {
		this.vision = vision;
		buildTileMap(vision);
		printTileMap();
		ArrayList<Tile> temp = new ArrayList<Tile>();
		temp.add(getTile(curX,curY));
		System.out.println(updateTarget(keyCount));
		return nextAction(temp.get(0).pathToTarget(temp,updateTarget(keyCount)));
	}
	/**
	 * Where we do our map exploration code.
	 * @param The last Action
	 * @return The next action.
	 */
	public Action nextAction(ArrayList<Tile> path)
	{
		if(path.get(0).boxType == BoxType.Key) return Action.Pickup;
		if(path.get(1).boxType == BoxType.Door) return Action.Use;
		if(path.get(1).y > path.get(0).y) return Action.South;
		if(path.get(1).y < path.get(0).y) return Action.North;
		if(path.get(1).x > path.get(0).x) return Action.East;
		if(path.get(1).x < path.get(0).y) return Action.West;
		return null;
	}
	public boolean isHallway(Tile posTarget) {
		if (getTile(posTarget.x+1, posTarget.y) == null 
				|| getTile(posTarget.x-1, posTarget.y) == null 
				|| getTile(posTarget.x, posTarget.y+1) == null 
				|| getTile(posTarget.x, posTarget.y-1) == null)
			return true;
		return false;
	}
	/**
	 * Updates our target for path finding.
	 */
	public Tile updateTarget(int keyCount){
		ArrayList<Tile> possible = new ArrayList<Tile>();
		for(Tile t : tList){
			if (t.boxType == BoxType.Exit){
				possible.add(t);
			}
			if (t.boxType == BoxType.Key && shouldAdd(t, possible)){
				possible.add(t);
			}
			if (t.boxType == BoxType.Door 
					&& keyCount > 0 && shouldAdd(t, possible)){
				possible.add(t);
			}
			if (t.boxType == BoxType.Open 
					&& shouldAdd(t, possible)){
				possible.add(t);
			}
		}
		for (int m = 0; m < possible.size(); m++){
			if (possible.get(m).priority > 
					possible.get(possible.size() -1).priority)
			{
				possible.remove(m);
				m--;
			}
		}
		if (possible.get(possible.size()-1).priority == 3)
		{
			for (int i = 0; i < possible.size(); i++)
			{
				if (!isHallway(possible.get(i)))
				{
					possible.remove(i);
					i--;
				}
			}
		}
		return getClosest(possible);
	}
	public Tile getClosest(ArrayList<Tile> possible)
	{
		Tile closestTile = new Tile();
		int shortest = Integer.MAX_VALUE;
		for (Tile t: possible){
			ArrayList<Tile> temp = new ArrayList<Tile>();
			temp.add(getTile(curX,curY));
			int dist = temp.get(0).pathToTarget(temp, t).size();
			if (dist < shortest)
			{
				closestTile = t;
				shortest = dist;
			}
			
		}
		return closestTile;
	}
	/**
	 * Prints our tile map to the console for testing purposes.
	 */
	public void printTileMap()
	{
		Collections.sort(tList);
		int lowX = 0, highX = 0;
		int lowY = 0, highY = 0;
		for (int i = 0; i < tList.size(); i++)
		{
			if (tList.get(i).x < lowX) lowX = tList.get(i).x;
			if (tList.get(i).x > highX) highX = tList.get(i).x;
			if (tList.get(i).y < lowY) lowY = tList.get(i).y;
			if (tList.get(i).y > highY) highY = tList.get(i).y;
		}
		for (int i = lowY; i <= highY; i++)
		{
			for (int j = lowX; j <= highX; j++)
			{
				if (j == lowX)
					System.out.println();
				if (j == curX && i == curY)
					System.out.print("M");
				else if (getTile(j,i) == null)
				{
					System.out.print("-");
				}
				else
				{
					Tile muaiThai = getTile(j,i);
					if (muaiThai.boxType == BoxType.Blocked 
							|| muaiThai.boxType == BoxType.Door)
						System.out.print("X");
					else if (muaiThai.boxType == BoxType.Open 
							|| muaiThai.boxType ==  BoxType.Key 
							|| muaiThai.boxType ==  BoxType.Exit)
						System.out.print("O");
				}
			}
		}
		System.out.println("\n");
	}
	
	/**
	 * 
	 */
	public boolean shouldAdd(Tile o, ArrayList<Tile> l){
		return l.isEmpty()? true : o.priority <= l.get(l.size() - 1).priority;
	}
	
	/**
	 * Constructs a map of the maze in Tile Objects based on what we have seen.
	 * The origin (0,0) is our starting location.
	 * @param The PlayerVision Object we get each turn.
	 */
	public void buildTileMap(PlayerVision v)
	{
		//Lots of copy paste here check to make sure all directions are correct
		//Add All East Blocks To Vision
		tList.add(new Tile(curX, curY, false, BoxType.Open));
		for (int i = 0; i < v.mEast; i++)
		{
			if (i == 0)
			{
				tList.add(new Tile(curX+1, curY, false, v.CurrentPoint.East)); //Block Directly To Right
			}
			else
			{
				tList.add(new Tile(curX+(i+1), curY, false, v.East[i-1].East));
			}
			
			//This Code Checks the MoveType of the Tiles North South and East of Each Tile
			tList.add(new Tile(curX+(i+1), curY-1, false, v.East[i].North));
			tList.add(new Tile(curX+(i+1), curY+1, false, v.East[i].South));
			tList.add(new Tile(curX+(i+2), curY, false, v.East[i].East));
		}
		//Add All West Blocks To Vision
		for (int i = 0; i < v.mWest; i++)
		{
			if (i == 0)
			{
				tList.add(new Tile(curX-1, curY, false, v.CurrentPoint.West)); //Block Directly To Left
			}
			else
			{
				tList.add(new Tile(curX-(i+1), curY, false, v.West[i-1].West));
			}
			
			//This Code Checks the MoveType of the Tiles North South and West of Each Tile
			tList.add(new Tile(curX-(i+1), curY-1, false, v.West[i].North));
			tList.add(new Tile(curX-(i+1), curY+1, false, v.West[i].South));
			tList.add(new Tile(curX-(i+2), curY, false, v.West[i].West));
		}
		//Add All North Blocks To Vision
		for (int i = 0; i < v.mNorth; i++)
		{
			if (i == 0)
			{
				tList.add(new Tile(curX, curY-1, false, v.CurrentPoint.North)); //Block Directly Up
			}
			else
			{
				tList.add(new Tile(curX, curY-(i+1), false, v.North[i-1].North));
			}
			
			//This Code Checks the MoveType of the Tiles East West and North of Each Tile
			tList.add(new Tile(curX+1, curY-(i+1), false, v.North[i].East));
			tList.add(new Tile(curX-1, curY-(i+1), false, v.North[i].West));
			tList.add(new Tile(curX, curY-(i+2), false, v.North[i].North));
		}
		//Add All South Blocks To Vision
		for (int i = 0; i < v.mSouth; i++)
		{
			if (i == 0)
			{
				tList.add(new Tile(curX, curY+1, false, v.CurrentPoint.South)); //Block Directly Down
			}
			else
			{
				tList.add(new Tile(curX, curY+(i+1), false, v.South[i-1].South));
			}
			
			//This Code Checks the MoveType of the Tiles East West and South of Each Tile
			tList.add(new Tile(curX+1, curY+(i+1), false, v.South[i].East));
			tList.add(new Tile(curX-1, curY+(i+1), false, v.South[i].West));
			tList.add(new Tile(curX, curY+(i+2), false, v.South[i].South));
		}
		for (int i = 0; i < tList.size(); i++)
		{
			Tile t = tList.get(i);
			for (int j = 0; j < tList.size(); j++)
			{
				Tile t2 = tList.get(j);
				if (i != j && t.x == t2.x && t.y == t2.y)
				{
					tList.remove(j);
				}
			}
		}
	}
	
	/**
	 * @param The Cartesian coordinates of the Tile object you want.
	 * @return The Tile Object located at (tX,tY).
	 */
	public Tile getTile(int tX, int tY)
	{
		for (int i = 0; i < tList.size(); i++)
		{
			if (tList.get(i).x == tX && tList.get(i).y == tY)
				return tList.get(i);
		}
		return null;
	}
}