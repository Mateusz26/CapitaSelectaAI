package tracks.levelGeneration.geneticLevelGenerator.Improved;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import core.game.GameDescription;
import core.game.GameDescription.InteractionData;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;

// improved 

public class Analyzer {
	/**
	 * Output information for debugging
	 */
	private boolean verbose = false;
	/**
	 * The game description we're working on
	 */
	private GameDescription game;
	/**
	 * List of all different types of spawners
	 */
	private final ArrayList<String> spawnerTypes = new ArrayList<String>(Arrays.asList(new String[]{"SpawnPoint", "Bomber", "RandomBomber", "Spreader", "ShootAvatar", "FlakAvatar"}));
	/**
	 * List for certain types of moving enemies
	 */
	private final ArrayList<String> movingTypes = new ArrayList<String>(Arrays.asList(new String[]{"Chaser", "RandomNPC"}));
	/**
	 * List of all different interactions cause spawning
	 */
	private final ArrayList<String> spawnInteractions = new ArrayList<String>(Arrays.asList(new String[]{"TransformTo", "SpawnIfHasMore", "SpawnIfHasLess"}));
	/**
	 * List of all different interactions cause an object to be solid
	 */
	private final ArrayList<String> solidInteractions = new ArrayList<String>(Arrays.asList(new String[]{"StepBack", "UndoAll"}));
	/**
	 * List of all different interactions cause an object to die
	 */
	private final ArrayList<String> deathInteractions = new ArrayList<String>(Arrays.asList(new String[]{"KillSprite", "KillIfHasMore", "KillIfHasLess", "KillIfFromAbove", "KillIfOtherHasMore", "KillIfFrontal", "KillIfNotFrontal", "SubtractHealthPoints"}));
	/**
	 * List of all horizontal moving avatar
	 */
	private final ArrayList<String> horizontalAvatar = new ArrayList<String>(Arrays.asList(new String[]{"FlakAvatar", "HorizontalAvatar"}));
	/**
	 * mapping of all LevelBlocks
	 */
	private HashMap<Character, ArrayList<SpriteData>> levelBlocks;
	/**
	 * mapping of all sprite names
	 */
	protected HashMap<Character, ArrayList<String>> levelMapping;
	/**
	 * list of priority values for all blocks
	 */
	private HashMap<Character, Integer> priorityValue;
	/**
	 * Max scoreChange found in all interactions
	 */
	private double minScoreUnit;
	/**
	 * Min scoreChange found in all interactions
	 */
	private double maxScoreUnit;
	/**
	 * List of all blocks that block the avatar's movement
	 */
	private ArrayList<Character> solidBlocks;
	/**
	 * List of all sprites that block the avatar's movement,
	 * but are removable
	 */
	private ArrayList<Character> solidRemovableBlocks;
	/**
	 * List of all blocks that contain an avatar sprite
	 */
	private ArrayList<Character> avatarBlocks;
	/**
	 * List of all sprites that kill the avatar
	 */
	private ArrayList<Character> harmfulBlocks;
	/**
	 * List of all block groups that have limits defined in
	 * the Termination Set
	 */
	private ArrayList<ArrayList<Character>> goalBlocks;
	/**
	 * limits corresponding to the goalBlocks
	 * Negative int means _upper_ limit!
	 */
	private ArrayList<Integer> goalLimits;
	/**
	 * The floor block
	 */
	private char floorBlock;
	/**
	 * All other blocks not defined
	 */
	private ArrayList<Character> otherBlocks;
	
	/**
	 * Initialize GameAnalyzer
	 * @param game	game description object for the current game
	 */
	public Analyzer(GameDescription game){
		this.game = game;
	    levelBlocks = new HashMap<Character, ArrayList<SpriteData>>();
	    levelMapping = new HashMap<Character, ArrayList<String>>();
		priorityValue = new HashMap<Character, Integer>();
		
		solidBlocks = new ArrayList<Character>();
		solidRemovableBlocks = new ArrayList<Character>();
		avatarBlocks = new ArrayList<Character>();
		harmfulBlocks = new ArrayList<Character>();
		goalBlocks = new ArrayList<ArrayList<Character>>();
		goalLimits = new ArrayList<Integer>();
		otherBlocks = new ArrayList<Character>();
		
		getLevelBlocks(game);
		calculatePriorityValues(game);
		
		findSolidBlocks();
		findAvatarBlocks(game);
		findGoals(game);
		findHarmfulBlocks(game);
		findFloorBlocks();
		findOtherBlocks(game);

		calculateMinMaxScoreUnit(game);
	}

	private void getLevelBlocks(GameDescription game) {
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		HashMap<Character, ArrayList<String>> levelMapping = game.getLevelMapping();
		
		for (char c : levelMapping.keySet()) {
			ArrayList<SpriteData> currentSprites = new ArrayList<SpriteData>();
			
			//get SpriteData for all sprites associated with current Character
			for (String spriteName : levelMapping.get(c)) {
				for (SpriteData sprite : allSprites) {
					if (spriteName.equals(sprite.name)) {
						currentSprites.add(sprite);
						break;
					}
				}
			}
			
			//ignore empty default wall mapping
			if (c == 'w' && currentSprites.size() == 1 && currentSprites.get(0).name == "wall"
					&& getAllInteractions(currentSprites.get(0).name, InteractionType.ALL, game).size() == 0) {
				continue;
			}
			
			levelBlocks.put(c, currentSprites);
			this.levelMapping.put(c, levelMapping.get(c));
		}
	}
	
	/**
	 * calculate the priority values for all game sprites and save it in hashmap
	 * @param game	game description object for the current game
	 */
	private void calculatePriorityValues(GameDescription game){		
		for(char key : levelBlocks.keySet()){
			int value = 0;
			for (SpriteData sprite : levelBlocks.get(key)) {
				value += getAllInteractions(sprite.name, InteractionType.ALL, game).size();
			}
			priorityValue.put(key, value);
		}
	}
	
	/**
	 * get a list of all interactions for a certain game sprite
	 * @param stype	sprite required to be checked
	 * @param type	type of checked interaction (if the sprite is always on left or right or don't care)
	 * @param game	game description object for the current game
	 * @return		list of all interactions for the listed sprite name
	 */
	private ArrayList<InteractionData> getAllInteractions(String stype, InteractionType type, GameDescription game){
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		ArrayList<InteractionData> data = new ArrayList<InteractionData>();
		
		for (SpriteData sd:allSprites){
			if(type == InteractionType.FIRST || type == InteractionType.ALL){
				data.addAll(game.getInteraction(stype, sd.name));
			}
			if(type == InteractionType.SECOND || type == InteractionType.ALL){
				data.addAll(game.getInteraction(sd.name, stype));
			}
		}
		
		return data;
	}
	
	/**
	 * search for all solid blocks that blocks the avatar from moving
	 * (non-removable and removable)
	 * @param game	game description object for the current game
	 */
	private void findSolidBlocks(){
		ArrayList<SpriteData> avatars = game.getAvatar();
		ArrayList<SpriteData> staticSprites = game.getStatic();

		for(SpriteData sprite:staticSprites){
			boolean isSolid = false;
			boolean isRemovable = false;
			ArrayList<InteractionData> secondaryInteraction = getAllInteractions(sprite.name, InteractionType.FIRST, game);
			
			for(SpriteData avatar:avatars){
				ArrayList<InteractionData> interactions = game.getInteraction(avatar.name, sprite.name);
								
				//include the sprite if it blocks avatar's movement...
				for(InteractionData i:interactions){
					if(solidInteractions.contains(i.type)){
						isSolid = true;
						break;
					}
				}

				//... but exclude if it also does anything else
				for(InteractionData sI:secondaryInteraction){
					if(!solidInteractions.contains(sI.type)){
						isSolid = false;
						break;
					}
				}

				//check if it can be removed with avatar's weapon
				if (avatar.sprites.size()>0) {
					ArrayList<InteractionData> weaponInteraction = game.getInteraction(sprite.name, avatar.sprites.get(0));
					for(InteractionData wI:weaponInteraction) {
						if(wI.type.equals("KillSprite")) {
							isRemovable = true;
							isSolid = true;
							
						}
					}
				}
				
				//if it's not solid for one avatar, it shouldn't be solid at all
				if (!isSolid) break;
			}
			
			//add to lists based on results
			if(isSolid && !isRemovable){
				for (char key : levelMapping.keySet()) {
					if (levelMapping.get(key).contains(sprite.name) && !solidBlocks.contains(key)) {
						solidBlocks.add(key);
					}
				}
			}
			else if(isSolid && isRemovable){
				for (char key : levelMapping.keySet()) {
					if (levelMapping.get(key).contains(sprite.name)) {
						solidRemovableBlocks.add(key);
					}
				}
			}
		}
	}

	/**
	 * get a list of all avatar sprites in the game
	 * @param game	game description object for the current game
	 */
	private void findAvatarBlocks(GameDescription game){
		ArrayList<SpriteData> avatars = game.getAvatar();
		ArrayList<String> avatarSprites = new ArrayList<String>();
		
		for(SpriteData sprite:avatars){
			if(!avatarSprites.contains(sprite.name)){
				avatarSprites.add(sprite.name);
			}
		}
		
		for (char key : levelBlocks.keySet()) {
			for (String sprite : avatarSprites) {
				if (game.getLevelMapping().get(key).contains(sprite)) {
					avatarBlocks.add(key);
					break;
				}
			}
		}
		if (verbose) System.out.println("findAvatarBlocks: " + avatarBlocks);
	}
	
	/**
	 * find all sprites listed in the termination set
	 * @param game	game description object for the current game
	 */
	private void findGoals(GameDescription game){
		ArrayList<TerminationData> terminations = game.getTerminationConditions();
		
		for(TerminationData ter:terminations){
			ArrayList<Character> currentBlocks = new ArrayList<Character>();
			//find corresponding Blocks
			for (char key : levelBlocks.keySet()) {
				for (String sprite : ter.sprites) {
					if (game.getLevelMapping().get(key).contains(sprite)) {
						currentBlocks.add(key);
						break;
					}
				}
			}
			
			//nothing to do if no blocks are associated with this TerminationData
			if (currentBlocks.size() == 0) continue;
			
			//find limits
			int limit;	
			
			//if MultiSpriteCounter is used with only one sprite, it is very 
			//probably used as substitute for (non-functioning) SpriteCounterMore 
			//and meant as UPPER LIMIT
			if (ter.type.equals("MultiSpriteCounter") && ter.sprites.size() == 1) {
				//negative value for upper limit
				limit = -(ter.limit); 
			}
			else {	//assume LOWER LIMIT
				limit = ter.limit;
			}
			
			goalBlocks.add(currentBlocks);
			goalLimits.add(limit);
		}
		
		if (verbose) System.out.println("findGoalBlocks blocks: " + goalBlocks);
		if (verbose) System.out.println("findGoalBlocks limits: " + goalLimits);
	}
	
	/**
	 * find all blocks that can kill the avatar
	 * @param game	game description object for the current game
	 */
	private void findHarmfulBlocks(GameDescription game){
		ArrayList<SpriteData> avatars = game.getAvatar();
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		List<String> harmfulSprites = new ArrayList<String>();
		List<String> shieldSprites = new ArrayList<String>();
		
		for(SpriteData a:avatars){
			for(SpriteData s:allSprites){
				ArrayList<InteractionData> interactions = game.getInteraction(a.name, s.name);
				for(InteractionData i:interactions){
					if(deathInteractions.contains(i.type) && !harmfulSprites.contains(s.name)){
						harmfulSprites.add(s.name);
					}
					else if(i.type.equals("ShieldFrom") && !shieldSprites.contains(s.name)) {
						shieldSprites.add(s.name);
					}
				}
			}
		}
		for(SpriteData s:allSprites){
			if(spawnerTypes.contains(s.type)){
				for(String stype:s.sprites){
					if(harmfulSprites.contains(stype) && 
							!harmfulSprites.contains(s.name)){
						harmfulSprites.add(s.name);
					}
				}
			}
		}
		for (char key : levelBlocks.keySet()) {
			for (String sprite : harmfulSprites) {
				if (game.getLevelMapping().get(key).contains(sprite)) {
					//look for a shielding sprite on the same block and hope
					//it actually shields from this harmful type 
					boolean shielded = false;
					for (String blockSprite : game.getLevelMapping().get(key)) {
						if (shieldSprites.contains(blockSprite)) {
							shielded = true;
							break;
						}
					}
					if (!shielded) {
						harmfulBlocks.add(key);
						break;
					}
				}
			}
		}
		
		//harmful blocks shouldn't be included in solidBlocks
		solidBlocks.removeAll(harmfulBlocks);
		solidRemovableBlocks.removeAll(harmfulBlocks);
		
		if (verbose) System.out.println("SolidBlocks: " + solidBlocks);
		if (verbose) System.out.println("SolidRemovableBlocks: " + solidRemovableBlocks);
		if (verbose) System.out.println("HarmfulBlocks: " + harmfulBlocks);
	}

	/**
	 * find the floor block
	 * Limitation: finds only one, even if different ones exist
	 */
	private void findFloorBlocks(){			
		for (char key : levelBlocks.keySet()) {
			if(levelBlocks.get(key).size() == 1 && !solidBlocks.contains(key)
					&& !avatarBlocks.contains(key)) {
				if (!harmfulBlocks.contains(key)) {
					floorBlock = key;
					break;
				}
			}
		}
		
		//default if detection fails (fixes)
		if (floorBlock == 0) floorBlock='.';
		
		int counter=0;
		for (char key : levelBlocks.keySet()) {
			if (levelBlocks.get(key).size() != 1) {
				counter++;
			}
		}
		if (counter == 0) floorBlock = ' ';
		
		if (verbose) System.out.println("findFloorBlocks: " + floorBlock);
	}
	
	/**
	 * analyze the game description object and list all other blocks
	 * @param game	game description object for the current game
	 */
	private void findOtherBlocks(GameDescription game){
		ArrayList<Character> combinedBlocks = new ArrayList<Character>();
		combinedBlocks.addAll(avatarBlocks);
		combinedBlocks.addAll(harmfulBlocks);
		combinedBlocks.addAll(solidBlocks);
		combinedBlocks.addAll(solidRemovableBlocks);
		combinedBlocks.add(floorBlock);
		
		for(char key : levelBlocks.keySet()){
			if(!combinedBlocks.contains(key) && !otherBlocks.contains(key)){
					otherBlocks.add(key);
			}
		}
		
		if (verbose) System.out.println("findOtherBlocks: " + otherBlocks);
	}
	
	
	/**
	 * calculate the min and max score change in the instruction set
	 * @param game	game description object for the current game
	 */
	private void calculateMinMaxScoreUnit(GameDescription game){
		maxScoreUnit = 0;
		minScoreUnit = Integer.MAX_VALUE;
		ArrayList<SpriteData> allSprites = game.getAllSpriteData();
		
		for(SpriteData s1:allSprites){
			for(SpriteData s2:allSprites){
				ArrayList<InteractionData> interactions = game.getInteraction(s1.name, s2.name);
				for(InteractionData i:interactions){
					String[] scores = i.scoreChange.split(",");
					for (String j: scores) {
						int s = Integer.parseInt(j);
						if (s > 0) {
							if (s > maxScoreUnit) {
								maxScoreUnit = s;
							}
							if (s < minScoreUnit) {
								minScoreUnit = s;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Get the priority value for a specific block
	 * @param stype	sprite name to be checked
	 * @return		number of occurence of the sprite in the InteractionSet
	 */
	public int getPriorityValue(char key){
		return priorityValue.get(key);
	}
	
	/**
	 * Get array of all available blocks
	 * @return	array contains all solid blocks
	 */
	public Set<Character> getAllBlocks(){
		return levelBlocks.keySet();
	}
	
	/**
	 * Get array of solid blocks
	 * @return	array contains all solid blocks
	 */
	public ArrayList<Character> getSolidBlocks(){
		return solidBlocks;
	}
	
	/**
	 * Get array of solid, removable blocks
	 * @return	array contains all solid, removable blocks
	 */
	public ArrayList<Character> getSolidRemovableBlocks(){
		return solidRemovableBlocks;
	}
	
	/**
	 * get array of avatar blocks
	 * @return	array of all blocks marked as avatar
	 */
	public ArrayList<Character> getAvatarBlocks(){
		return avatarBlocks;
	}
	
	/**
	 * get array of all blocks that can kill the avatar
	 * @return	array of all harmful blocks
	 */
	public ArrayList<Character> getHarmfulBlocks(){
		return harmfulBlocks;
	}
	
	/**
	 * get array for all objects that can be collected using player
	 * @return	array list contains collectible sprites
	 */
//	public ArrayList<Character> getCollectableSprites(){
//		return collectableBlocks;
//	}
	
	/**
	 * get an array contains all blocks groups in the termination set
	 * @return	array list contains all goal block groups
	 */
	public ArrayList<ArrayList<Character>> getGoalBlocks(){
		return goalBlocks;
	}
	
	/**
	 * get an array contains all limits for the goalBlocks
	 * @return	array list of goal block limits
	 */
	public ArrayList<Integer> getGoalLimits(){
		return goalLimits;
	}
	
	/**
	 * get an array list of all other blocks that are not listed in the previous lists
	 * @return	array list of all other blocks
	 */
	public ArrayList<Character> getOtherBlocks(){
		return otherBlocks;
	}
	
	/**
	 * get maximum +ve score change listed in the instruction set
	 * @return	maximum +ve score change value
	 */
	public double getMaxScoreUnit(){
		return maxScoreUnit;
	}
	
	/**
	 * get minimum +ve score change listed in the instruction set
	 * @return minimum +ve score change value
	 */
	public double getMinScoreUnit(){
		return minScoreUnit;
	}
	
	/**
	 * Check if the block contains a moving object.
	 * Only returns true for certain, limited types of movers 
	 * which we want to place far from avatar if harmful
	 * @param key	block to be checked
	 * @return		true or false
	 */
	public boolean isMoving(char key){
		for (SpriteData sprite : levelBlocks.get(key)) {
			if (movingTypes.contains(sprite.type)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * check if the block contains a spawner object
	 * @param key	block to be checked
	 * @return		true or false
	 */
	public boolean isSpawner(char key){
		for (SpriteData sprite : levelBlocks.get(key)) {
			if (spawnerTypes.contains(sprite.type))
				return true;
		}
		return false;
	}
	
	/**
	 * get the type of avatar on this block
	 * @param key	block to be checked
	 * @return		"horizontal", "flakUp", "flakDown", "other"
	 */
	public String getAvatarType(char key) {		
		for (SpriteData sprite : levelBlocks.get(key)) {
			if (horizontalAvatar.contains(sprite.type)) {
				if (sprite.type.equals("FlakAvatar")) {
					String shot = sprite.sprites.get(0);
					for (SpriteData moving : game.getMoving()) {
						if (moving.name.equals(shot)) {
							if(moving.toString().contains("orientation=DOWN")) {
								return "flakDown";
							}
							else {
								return "flakUp";
							}	
						}
					}
				}
				return "horizontal";
			}		
		}
		return "other";
	}
	
	/**
	 * Get the floor block. Limitation: If more than one exists,
	 * only first one is returned.
	 * @return	key value of the floor block
	 */
	public char getFloor() {		
		return floorBlock;
	}
	
	/**
	 * Internal Enum for getAllInteractions function
	 */
	private enum InteractionType{
		ALL,
		FIRST,
		SECOND
	}
}
