package tracks.levelGeneration.geneticLevelGenerator.Improved;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import core.game.GameDescription;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tracks.levelGeneration.geneticLevelGenerator.Improved.LevelData.Point;

// added new genetic operators 

public class ConstructiveLevelGenerator extends AbstractLevelGenerator{
	/**
	 * Output information for debugging
	 */
	private boolean verbose = false;

	/**
	 * object for game analyzer
	 */
	private Analyzer gameAnalyzer;
	/**
	 * random object 
	 */
	private Random random;
	/**
	 * level cover percentage (outer walls not included)
	 */
	private double coverPercentage;
	/**
	 * percentage of changing direction during creating walls
	 */
	private double shuffleDirectionPercentage;
	/**
	 * randomness of level sizes
	 */
	private double levelSizeRandomPercentage;
	/**
	 * randomness of cover percentages / block types
	 */
	private double randomness;
	/**
	 * chance for having inner walls
	 */
	private double innerWallsPercentage;
	/**
	 * object of LevelData to hold the current constructed level
	 */
	private LevelData generatedLevel;

	/**
	 * Initialize all the parameters for the level generator
	 * @param game			game description object that define the current game
	 * @param elpasedTimer	the amount of time that the constructor have
	 */

	public ConstructiveLevelGenerator(GameDescription game, ElapsedCpuTimer elpasedTimer){
		gameAnalyzer = new Analyzer(game);
		random = new Random();

		levelSizeRandomPercentage = 0.5;
		coverPercentage = 0.1;
		//		coverPercentage = 0.5;
		randomness = 0.2;

		innerWallsPercentage = 0.8;
		shuffleDirectionPercentage = 0.2;
	}

	/**
	 * Calculate the percentage covered from the level and 
	 * percentages of each block type
	 * @param game	game description object provided by the system
	 * @return		level cover data object contain all the calculated information
	 */
	private LevelCoverData getPercentagesCovered(GameDescription game){
		LevelCoverData data = new LevelCoverData();

		double solidValue = 0;
		double harmfulValue = 0;
		double otherValue = 0;
		double totalValue = 0;
		for(char key : gameAnalyzer.getAllBlocks()){
			double value = 2*(gameAnalyzer.getPriorityValue(key) + 1);

			//number of solid blocks (priority value)
			if(gameAnalyzer.getSolidBlocks().contains(key) || gameAnalyzer.getSolidRemovableBlocks().contains(key)){
				solidValue += value;
			}

			//number of harmful blocks (priority value)
			if(gameAnalyzer.getHarmfulBlocks().contains(key)){
				harmfulValue += value;
			}
			//number of other blocks (priority value)
			if(gameAnalyzer.getOtherBlocks().contains(key)){
				otherValue += value;
			}
			//overall total of all kind of blocks
			totalValue += value;
		}

		// set level cover percentage
		data.levelPercentage = SharedData.noise(coverPercentage, randomness, random.nextDouble());

		//calculate different percentage based on the previous data
		if(solidValue > 0 && random.nextDouble() < innerWallsPercentage) {
			data.solidPercentage = Math.max(SharedData.noise(solidValue / totalValue, randomness, random.nextDouble()), 0);
			//			data.solidPercentage = noise(0.4, randomness, random.nextDouble());
		}
		if(harmfulValue > 0){
			data.harmfulPercentage = Math.max(SharedData.noise(harmfulValue / totalValue, randomness, random.nextDouble()), 0);
			//			data.harmfulPercentage = noise(0.235, randomness, random.nextDouble());
		}
		if(otherValue > 0){
			data.otherPercentage = Math.max(SharedData.noise(otherValue / totalValue, randomness, random.nextDouble()), 0);
			//			data.otherPercentage = noise(0.22, randomness, random.nextDouble());
		}

		// normalize relative values
		totalValue = data.solidPercentage + data.harmfulPercentage + data.otherPercentage;
		data.solidPercentage /= totalValue;
		data.harmfulPercentage /= totalValue;
		data.otherPercentage /= totalValue;

		if (verbose) System.out.println("levelPercentage: " + data.levelPercentage);
		if (verbose) System.out.println("solidPercentage: " + data.solidPercentage);
		if (verbose) System.out.println("harmfulPercentage: " + data.harmfulPercentage);
		if (verbose) System.out.println("otherPercentage: " + data.otherPercentage);

		return data;
	}

	/**
	 * Add a solid to the level space without disconnecting the level
	 * @param level	the current level to test
	 * @param x		the x position
	 * @param y		the y position
	 * @param solid	the name of the solid
	 * @return		true if it placed it and false otherwise
	 */
	private boolean placeSolid(LevelData level, int x, int y, char solid){
		if(!level.checkConnectivity(x, y)){
			return false;
		}
		level.set(x, y, solid);
		return true;
	}

	/**
	 * build level layout using the solid objects
	 * @param level				the current level
	 * @param coverPercentage	the cover percentages
	 */
	private void buildLevelLayout(LevelData level, LevelCoverData coverPercentage){
		ArrayList<Character> solidBlocks = gameAnalyzer.getSolidBlocks();
		ArrayList<Character> solidRemovableBlocks = gameAnalyzer.getSolidRemovableBlocks();
		if (verbose) System.out.println("Solids available: " + solidBlocks);
		if (verbose) System.out.println("Removable Solids available: " + solidRemovableBlocks + "\n");

		if(solidBlocks.size() > 0 || solidRemovableBlocks.size() > 0){
			char randomSolid;
			
			//pick a random solid object to use for borders, prefer non-removable
			if(solidBlocks.size() > 0)
				randomSolid = solidBlocks.get(random.nextInt(solidBlocks.size()));
			else 
				randomSolid = solidRemovableBlocks.get(random.nextInt(solidRemovableBlocks.size()));
	
			//adding a borders around the level
			for(int x=0; x<level.getWidth(); x++){
				level.set(x, 0, randomSolid);
				level.set(x, level.getHeight() - 1, randomSolid);
			}

			for(int y=0; y<level.getHeight(); y++){
				level.set(0, y, randomSolid);
				level.set(level.getWidth() - 1, y, randomSolid);
			}
			
			//number of solid to insert in the level
			double solidNumber = coverPercentage.levelPercentage * coverPercentage.solidPercentage * 
					getArea(level);
			
			// If available, switch to removable solid for level interior
			if (solidRemovableBlocks.size() > 0) {
				randomSolid = solidRemovableBlocks.get(random.nextInt(solidRemovableBlocks.size()));
				solidNumber = Math.min(10*solidNumber, level.getAllFreeSpots().size()/2);
			}		

			while(solidNumber > 0){
				//list of all the free positions
				ArrayList<Point> freePositions = level.getAllFreeSpots();
				//pick random position
				Point randomPoint = freePositions.get(random.nextInt(freePositions.size()));
				// place block
				if (solidRemovableBlocks.size() > 0) {
					level.set(randomPoint.x, randomPoint.y, randomSolid);
				}
				//non-removable, check for connectivity
				else { 
					//if can't place it choose another one and try again
					if(!placeSolid(level, randomPoint.x, randomPoint.y, randomSolid)){
						continue;
					}
				}
				solidNumber -= 1;
				//start construct a corridor of random length
				int length = 2 + random.nextInt(1+(int)(getArea(level)/100));
				ArrayList<Point> directions = new ArrayList<Point>(Arrays.asList(new Point[]{new Point(1,0), new Point(-1,0), new Point(0,-1), new Point(0,1)}));
				while(length > 0){
					if(random.nextDouble() < shuffleDirectionPercentage){
						Collections.shuffle(directions);
					}
					int i=0;

					//check each direction and move using them
					for(i=0; i<directions.size(); i++){
						Point newPoint = new Point(randomPoint.x + directions.get(i).x, randomPoint.y + directions.get(i).y);
						if(level.get(newPoint.x, newPoint.y) == 0) {
							if(solidRemovableBlocks.size() > 0) {
								level.set(newPoint.x, newPoint.y, randomSolid);
							}
							else if (placeSolid(level, newPoint.x, newPoint.y, randomSolid)){}
							else {continue;}
							randomPoint.x = newPoint.x;
							randomPoint.y = newPoint.y;
							length -= 1;
							solidNumber -= 1;
							break;
						}
						else{
							continue;
						}
					}

					//if no direction can done just stop this corridor
					if(i >= directions.size()){
						break;
					}
				}
			}
		}
	}

	/**
	 * Get the area of the level
	 * @param level	the current level
	 * @return		the size of the internal level (without the borders if exists)
	 */
	private double getArea(LevelData level){
		if(gameAnalyzer.getSolidBlocks().size() > 0){
			return (level.getWidth() - 2) * (level.getHeight() - 2);
		}
		return level.getWidth() * level.getHeight();
	}

	/**
	 * get all free positions that have the lowest or highest y value 
	 * @param freePositions	list of all free positions
	 * @return				return list of all these points
	 */
	private ArrayList<Point> getUpperLowerPoints(ArrayList<Point> freePositions){
		ArrayList<Point> result = new ArrayList<Point>();
		int minY = 100000;
		int maxY = 0;
		for(Point p:freePositions){
			if(p.y > maxY){
				maxY = p.y;
			}
			if(p.y < minY){
				minY = p.y;
			}
		}

		for(Point p:freePositions){
			if(p.y == maxY || p.y == minY){
				result.add(p);
			}
		}

		return result;
	}

	/**
	 * get all free positions that have the lowest y value
	 * @param freePositions	list of all free positions
	 * @return				return list of all these points
	 */
	private ArrayList<Point> getUpperPoints(ArrayList<Point> freePositions){
		ArrayList<Point> result = new ArrayList<Point>();
		int minY = 100000;
		for(Point p:freePositions){
			if(p.y < minY){
				minY = p.y;
			}
		}

		for(Point p:freePositions){
			if(p.y == minY){
				result.add(p);
			}
		}

		return result;
	}

	/**
	 * get all free positions that have the highest y value 
	 * @param freePositions	list of all free positions
	 * @return				return list of all these points
	 */
	private ArrayList<Point> getLowerPoints(ArrayList<Point> freePositions){
		ArrayList<Point> result = new ArrayList<Point>();
		int maxY = 0;
		for(Point p:freePositions){
			if(p.y > maxY){
				maxY = p.y;
			}
		}

		for(Point p:freePositions){
			if(p.y == maxY){
				result.add(p);
			}
		}

		return result;
	}

	/**
	 * add the avatar to the current level
	 * @param level	the current level
	 * @param game	the game description object
	 * @return		the added position
	 */
	private Point addAvatar(LevelData level, GameDescription game){
		//get all the free position in the level
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		//pick random position from them
		Point randomPoint;
		//pick random avatar block
		ArrayList<Character> avatars = gameAnalyzer.getAvatarBlocks();
		char avatar = avatars.get(random.nextInt(avatars.size()));

		String type = gameAnalyzer.getAvatarType(avatar);
		//if the avatar is horizontal mover: place at the top or the bottom
		if(type.equals("horizontal")) {
			freePositions = getUpperLowerPoints(freePositions);
		}
		// if avatar shoots: place at top if shoots down (bottom otherwise)
		else if (type.equals("flakUp")) {
			freePositions = getLowerPoints(freePositions);
		}
		else if (type.equals("flakDown")) {
			freePositions = getUpperPoints(freePositions);
		}
		
		randomPoint = freePositions.get(random.nextInt(freePositions.size()));
		level.set(randomPoint.x, randomPoint.y, avatar);

		return randomPoint;
	}


	/**
	 * calculate the number of blocks in the level
	 * @param 	game	game description object
	 * @param 	level	the current level
	 * @return	hashmap for all block keys with the associated numbers
	 */
	private HashMap<Character, Integer> calculateNumberOfObjects(GameDescription game, LevelData level){
		HashMap<Character, Integer> objects = new HashMap<Character, Integer>();
		Set<Character> allBlocks = gameAnalyzer.getAllBlocks();

		//add all sprite names as keys in the hashmap
		for(char key : allBlocks){
			objects.put(key, 0);
		}

		//calculate the numbers
		for(int y = 0; y < level.getHeight(); y++){
			for(int x = 0; x < level.getWidth(); x++){
				if(objects.containsKey(level.get(x, y))){
					objects.put(level.get(x, y), objects.get(level.get(x, y)) + 1);
				}
				else{
					objects.put(level.get(x, y), 1);
				}
			}
		}
		return objects;
	}


	/**
	 * fix the termination conditions by making sure all of them are unsatisfied
	 * @param game				game description object
	 * @param level				current level
	 * @param coverPercentage	the cover percentages
	 */
	private void fixGoals(GameDescription game, LevelData level, LevelCoverData coverPercentage){
		//get the number of objects in the level
		HashMap<Character, Integer> numObjects = calculateNumberOfObjects(game, level);
		ArrayList<Point> positions = level.getAllFreeSpots();

		ArrayList<ArrayList<Character>> goalBlocks = gameAnalyzer.getGoalBlocks();
		ArrayList<Integer> goalLimits = gameAnalyzer.getGoalLimits();

		for(int i=0; i<goalLimits.size(); i++){
			ArrayList<Character> keys = goalBlocks.get(i);
			int limit = goalLimits.get(i);
			int currentNum = 0;
			int increase = 0;

			//calculate the number of sprites found on the board
			for(char key : goalBlocks.get(i)){
				currentNum += numObjects.get(key);
			}

			// upper limit
			if (limit < 0 && currentNum >= -limit) {
				increase = (-limit - 1) - currentNum;
			}
			// lower limit
			else {
				if (currentNum <= limit) {
					increase = (limit + 1) - currentNum;
				}
			}

			if (verbose) System.out.println("FixGoals: " + increase + " of " + keys);

			// do necessary increase/decrease in number of sprites
			if(increase > 0){
				for(int j = 0; j < increase; j++){
					int index = random.nextInt(positions.size());
					Point pos = positions.remove(index);
					level.set(pos.x, pos.y, keys.get(random.nextInt(keys.size())));
				}
			}
			else if (increase < 0){
				// !!! TODO !!!
			}
		}
	}

	/**
	 * get a free position far from the avatar position
	 * @param freePosition		list of the free positions
	 * @param avatarPosition	the avatar position
	 * @return					the index of the possible far position
	 */
	private int getFarLocation(ArrayList<Point> freePosition, Point avatarPosition){
		ArrayList<Double> distProb = new ArrayList<Double>();
		double totalValue = 0;

		if(gameAnalyzer.getAvatarType(generatedLevel.get(avatarPosition.x, avatarPosition.y)) == "other") {
			//give each position a probability to be picked based on 
			//distance from the avatar position
			for(int i=0; i<freePosition.size(); i++){
				double distance = avatarPosition.getDistance(freePosition.get(i));
				distProb.add(distance);
				totalValue = Math.max(totalValue, distance);
			}
		}
		else {
			//horizontal avatar
			//pick probability based only on vertical distance
			for(int i=0; i<freePosition.size(); i++){
				double distance = Math.abs(avatarPosition.y-(freePosition.get(i).y));
				distProb.add(distance);
				totalValue = Math.max(totalValue, distance);
			}
		}

		double randomValue = random.nextDouble();
		for(int i=0; i<freePosition.size(); i++){
			distProb.set(i, distProb.get(i) / totalValue);
			if(randomValue < distProb.get(i)){
				return i;
			}
		}

		return -1;
	}


	/**
	 * Add harmful blocks to the level
	 * @param game				the game description object
	 * @param level				the current level
	 * @param coverPercentage	the cover percentages
	 * @param avatarPosition	the current avatar position
	 */
	private void addHarmfulBlocks(GameDescription game, LevelData level, LevelCoverData coverPercentage, Point avatarPosition){
		double numberOfHarmful = coverPercentage.levelPercentage * coverPercentage.harmfulPercentage * 
				getArea(generatedLevel);

		ArrayList<Character> harmfulBlocks = gameAnalyzer.getHarmfulBlocks();
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		if (verbose) System.out.print("Harmfuls available: " + harmfulBlocks + "\nadding: ");

//		System.out.println(numberOfHarmful);
		
		while(numberOfHarmful > 0){
			//get a random harmful sprite that is not spawned by another one
			char randomHarm = harmfulBlocks.get(random.nextInt(harmfulBlocks.size()));

			if (verbose) System.out.print(randomHarm + " ");

			int index;

			//if the harmful object is moving, then get a far position from the player starting point
			if(gameAnalyzer.isMoving(randomHarm)){
//				System.out.println("moving harmful detected");
				Collections.shuffle(freePositions);
				index = getFarLocation(freePositions, avatarPosition);
				numberOfHarmful -= 2;
			}
			//spawners of harmful objects should also be far from player
			else if(gameAnalyzer.isSpawner(randomHarm)){
				Collections.shuffle(freePositions);
				index = getFarLocation(freePositions, avatarPosition);
				numberOfHarmful -= 2; // create less spawners
			}

			//for everything else, any random position will be fine
			else{
				index = random.nextInt(freePositions.size());
				numberOfHarmful -= 1;
			}

			Point randPoint = freePositions.get(index);
			level.set(randPoint.x, randPoint.y, randomHarm);
			freePositions.remove(index);
		}
		if (verbose) System.out.print("\n\n");
	}


	/**
	 * Add other blocks objects to the level
	 * @param level				the current game level
	 * @param coverPercentage	the cover percentages
	 */
	private void addOtherBlocks(LevelData level, LevelCoverData coverPercentage){
		double numberOfOther = coverPercentage.levelPercentage * coverPercentage.otherPercentage * 
				getArea(level);
		ArrayList<Character> otherBlocks = gameAnalyzer.getOtherBlocks();
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		if (verbose) System.out.print("Others available: " + otherBlocks + "\nadding: ");

		while(numberOfOther > 0){

			//pick a random block
			char randomBlock = otherBlocks.get(random.nextInt(otherBlocks.size()));

			if (verbose) System.out.print(randomBlock + " ");

			//place it at any random free position
			int index = random.nextInt(freePositions.size());
			Point randPoint = freePositions.get(index);
			level.set(randPoint.x, randPoint.y, randomBlock);
			freePositions.remove(index);
			numberOfOther -= 1;
		}
		if (verbose) System.out.print("\n\n");
	}

	/**
	 * Add floor blocks to all empty positions of the level
	 * @param level		the current game level
	 */
	private void addFloor(LevelData level){
		ArrayList<Point> freePositions = level.getAllFreeSpots();
		char floor = gameAnalyzer.getFloor();
		if (verbose) System.out.print("Floor: " + floor);

		for (Point p : freePositions) {
			level.set(p.x, p.y, floor);
		}
	}


	/**
	 * Generate a level with a fixed width and length
	 * @param game			the current level description
	 * @param elapsedTimer	the amount of time allowed for generation
	 * @param width			the width of the level
	 * @param height		the height of the level
	 * @return				string for the generated level
	 */
	public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer, int width, int height){
		generatedLevel = new LevelData(width, height);
		LevelCoverData coverPercentages = getPercentagesCovered(game);

		buildLevelLayout(generatedLevel, coverPercentages);
		Point avatarPosition = addAvatar(generatedLevel, game);
		addHarmfulBlocks(game, generatedLevel, coverPercentages, avatarPosition);
		//		addCollectableObjects(game, generatedLevel, coverPercentages);
		addOtherBlocks(generatedLevel, coverPercentages);
		//
		fixGoals(game, generatedLevel, coverPercentages);

		addFloor(generatedLevel);

		return generatedLevel.getLevel();
	}


	/**
	 * generate a level without specifying the width and the height of the level
	 * @param game			the current game description object
	 * @param elpasedTimer	the amount of time allowed for generation
	 * @return				string for the generated level
	 */
	@Override
	public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
		// average size from example levels
		double avgWidth = 21.74;
		double avgHeight = 12.44;  

		if (gameAnalyzer.getSolidBlocks().size() == 0) {
			avgWidth -= 1;
			avgHeight -= 1;
		}

		// apply resizing factor based on # different blocks
		double blocks = gameAnalyzer.getAllBlocks().size();
		avgWidth = avgWidth * (1 + (blocks-9)/100);
		avgHeight = avgHeight * (1 + (blocks-9)/100);

		// add randomness
		int width = (int)Utils.noise(avgWidth, levelSizeRandomPercentage, random.nextDouble());
		int height = (int)Utils.noise(avgHeight, levelSizeRandomPercentage, random.nextDouble());

		return generateLevel(game, elapsedTimer, width, height);
	}

	public HashMap<Character, ArrayList<String>> getLevelMapping(){
		return gameAnalyzer.levelMapping;
	}
}
