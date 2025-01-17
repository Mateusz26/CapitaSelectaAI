package tracks.levelGeneration.geneticLevelGenerator.Improved;

import java.util.ArrayList;

public class LevelData {

	/**
	 * a 2D matrix hold data about the level
	 */
	private char[][] level;
	
	/**
	 * construct data for a level
	 * @param width		level width
	 * @param height	level height
	 */
	public LevelData(int width, int height) {
		level = new char[width][height];
	}
	

	/**
	 * get the 2D level in form of string
	 * @return	string describe the level
	 */
	public String getLevel() {
		String result = "";
		
		for(int y=0; y<level[0].length; y++) {
			for(int x=0; x<level.length; x++) {
				if(level[x][y] == 0){
					result += " ";
				}
				else{
					result += level[x][y];
				}
			}
			result += "\n";
		}
		result = result.substring(0, result.length() - 1);
		
		return result;
	}
	

	/**
	 * set a position in the map to a certain type
	 * @param x		x position on the map
	 * @param y		y position on the map
	 * @param key	the block to be added
	 */
	public void set(int x, int y, char key) {
		level[x][y] = key;
	}
	

	/**
	 * get the sprite at a certain position
	 * @param x	x position on the map
	 * @param y	y position on the map
	 * @return	sprite at a certain position on the map
	 */
	public char get(int x, int y) {
		return level[x][y];
	}
	

	/**
	 * get the width of the level
	 * @return	return the width of the level
	 */
	public int getWidth() {
		return level.length;
	}
	

	/**
	 * get the height of the level
	 * @return	return the height of the level
	 */
	public int getHeight() {
		return level[0].length;
	}
	

	/**
	 * check if the two positions are connected
	 * @param x1	x position of the first point
	 * @param y1	y position of the first point
	 * @param x2	x position of the second point
	 * @param y2	y position of the second point
	 * @return	true if they are connected and false otherwise
	 */
	public boolean checkConnectivity(int x1, int y1, int x2, int y2) {
		if(level[x1][y1] != 0 || level[x2][y2] != 0) {
			return false;
		}
		ArrayList<Point> queue = new ArrayList<Point>();
		boolean[][] visited = new boolean[getWidth()][getHeight()];
		Point[] directions = new Point[]{new Point(0, 1), new Point(1, 0), new Point(0, -1), new Point(-1, 0)};
		queue.add(new Point(x1, y1));
		while(queue.size() > 0){
			Point current = queue.remove(0);
			if(current.x == x2 && current.y == y2) {
				return true;
			}
			for(int i=0; i<directions.length; i++) {
				Point newPoint = new Point(current.x + directions[i].x, current.y + directions[i].y);
				if(!checkInLevel(newPoint.x, newPoint.y)){
					continue;
				}
				if(!visited[newPoint.x][newPoint.y] && level[newPoint.x][newPoint.y] == 0) {
					visited[newPoint.x][newPoint.y] = true;
					queue.add(newPoint);
				}
			}
		}
		return false;
	}
	

	/**
	 * check if this wall will cause the world not to be connected any more
	 * @param x x position for the point
	 * @param y	y position for the point
	 * @return	true if the wall didn't split the world into two halves and false otherwise
	 */
	public boolean checkConnectivity(int x, int y) {
		boolean result = false;
		set(x, y, 'w');
		if(x + 1 < getWidth() - 1 && x - 1 > 0) {
			result |= checkConnectivity(x + 1, y, x - 1, y);
		}
		if(y + 1 < getHeight() - 1 && y - 1 > 0) {
			result |= checkConnectivity(x, y + 1, x, y - 1);
		}
		set(x, y, (char)0);
		return result;
	}
	

	/**
	 * check if the point is inside the borders of the level
	 * @param x	x position for the point
	 * @param y	y position for the point
	 * @return	true if the point in the level and false otherwise
	 */
	public boolean checkInLevel(int x, int y) {
		return (x >= 0 && y >=0 && x < getWidth() && y < getHeight());
	}
	

	/**
	 * get all empty locations in the level
	 * @return	array of points contains all empty locations
	 */
	public ArrayList<Point> getAllFreeSpots() {
		ArrayList<Point> result = new ArrayList<Point>();
		for(int x=0; x<level.length; x++) {
			for(int y=0; y<level[0].length; y++){
				if(level[x][y] == 0){
					result.add(new Point(x, y));
				}
			}
		}
		
		return result;
	}
	

	/**
	 * Special point class that is used to get neighbors and handle
	 * other useful functions
	 * @author AhmedKhalifa
	 */
	public static class Point{
		public int x;
		public int y;
		
		public Point(){
			this.x = 0;
			this.y = 0;
		}
		
		public Point(int x, int y){
			this.x = x;
			this.y = y;
		}
		

		/**
		 * get a list of all 4 neighbor points
		 * @return	array of the 4 neighboring points
		 */
		public ArrayList<Point> getSurroundingPoints() {
			ArrayList<Point> result = new ArrayList<Point>();
			result.add(new Point(this.x + 1, this.y));
			result.add(new Point(this.x - 1, this.y));
			result.add(new Point(this.x, this.y + 1));
			result.add(new Point(this.x, this.y - 1));
			
			return result;
		}
		

		/**
		 * get distance between this point and the input point
		 * @param p	an input point
		 * @return	distance between this point and the input point
		 */
		public double getDistance(Point p) {
			return Math.sqrt(Math.pow(this.x - p.x, 2) + Math.pow(this.y - p.y, 2));
		}
	}
}
