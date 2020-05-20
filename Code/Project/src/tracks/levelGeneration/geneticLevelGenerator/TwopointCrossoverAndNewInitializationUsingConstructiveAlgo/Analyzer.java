package tracks.levelGeneration.geneticLevelGenerator.TwopointCrossoverAndNewInitializationUsingConstructiveAlgo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import core.game.GameDescription;
import core.game.GameDescription.InteractionData;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;

// New version
// In this version we used 2-point crossover and combined it with the constructive generator for initialization 

public class Analyzer {
	
	private boolean verbose = false;
	private GameDescription game;
	private final ArrayList<String> spawnerTypes = new ArrayList<String>(Arrays.asList(new String[]{"SpawnPoint", "Bomber", "RandomBomber", "Spreader", "ShootAvatar", "FlakAvatar"}));
	private final ArrayList<String> movingTypes = new ArrayList<String>(Arrays.asList(new String[]{"Chaser", "RandomNPC"}));
	private final ArrayList<String> spawnInteractions = new ArrayList<String>(Arrays.asList(new String[]{"TransformTo", "SpawnIfHasMore", "SpawnIfHasLess"}));
	private final ArrayList<String> solidInteractions = new ArrayList<String>(Arrays.asList(new String[]{"StepBack", "UndoAll"}));
	private final ArrayList<String> deathInteractions = new ArrayList<String>(Arrays.asList(new String[]{"KillSprite", "KillIfHasMore", "KillIfHasLess", "KillIfFromAbove", "KillIfOtherHasMore", "KillIfFrontal", "KillIfNotFrontal", "SubtractHealthPoints"}));
	private final ArrayList<String> horizontalAvatar = new ArrayList<String>(Arrays.asList(new String[]{"FlakAvatar", "HorizontalAvatar"}));
	private HashMap<Character, ArrayList<SpriteData>> levelBlocks;
	protected HashMap<Character, ArrayList<String>> levelMapping;
	private HashMap<Character, Integer> priorityValue;
	private double minScoreUnit;
	private double maxScoreUnit;
	private ArrayList<Character> solidBlocks;
	private ArrayList<Character> solidRemovableBlocks;
	private ArrayList<Character> avatarBlocks;
	private ArrayList<Character> harmfulBlocks;
	private ArrayList<ArrayList<Character>> goalBlocks;
	private ArrayList<Integer> goalLimits;
	private char floorBlock;
	private ArrayList<Character> otherBlocks;
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

			for (String spriteName : levelMapping.get(c)) {
				for (SpriteData sprite : allSprites) {
					if (spriteName.equals(sprite.name)) {
						currentSprites.add(sprite);
						break;
					}
				}
			}

			if (c == 'w' && currentSprites.size() == 1 && currentSprites.get(0).name == "wall"
					&& getAllInteractions(currentSprites.get(0).name, InteractionType.ALL, game).size() == 0) {
				continue;
			}
			
			levelBlocks.put(c, currentSprites);
			this.levelMapping.put(c, levelMapping.get(c));
		}
	}

	private void calculatePriorityValues(GameDescription game){		
		for(char key : levelBlocks.keySet()){
			int value = 0;
			for (SpriteData sprite : levelBlocks.get(key)) {
				value += getAllInteractions(sprite.name, InteractionType.ALL, game).size();
			}
			priorityValue.put(key, value);
		}
	}

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

	private void findSolidBlocks(){
		ArrayList<SpriteData> avatars = game.getAvatar();
		ArrayList<SpriteData> staticSprites = game.getStatic();

		for(SpriteData sprite:staticSprites){
			boolean isSolid = false;
			boolean isRemovable = false;
			ArrayList<InteractionData> secondaryInteraction = getAllInteractions(sprite.name, InteractionType.FIRST, game);
			
			for(SpriteData avatar:avatars){
				ArrayList<InteractionData> interactions = game.getInteraction(avatar.name, sprite.name);
				for(InteractionData i:interactions){
					if(solidInteractions.contains(i.type)){
						isSolid = true;
						break;
					}
				}

				for(InteractionData sI:secondaryInteraction){
					if(!solidInteractions.contains(sI.type)){
						isSolid = false;
						break;
					}
				}

				if (avatar.sprites.size()>0) {
					ArrayList<InteractionData> weaponInteraction = game.getInteraction(sprite.name, avatar.sprites.get(0));
					for(InteractionData wI:weaponInteraction) {
						if(wI.type.equals("KillSprite")) {
							isRemovable = true;
							isSolid = true;
							
						}
					}
				}

				if (!isSolid) break;
			}

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
	private void findGoals(GameDescription game){
		ArrayList<TerminationData> terminations = game.getTerminationConditions();
		
		for(TerminationData ter:terminations){
			ArrayList<Character> currentBlocks = new ArrayList<Character>();

			for (char key : levelBlocks.keySet()) {
				for (String sprite : ter.sprites) {
					if (game.getLevelMapping().get(key).contains(sprite)) {
						currentBlocks.add(key);
						break;
					}
				}
			}
			if (currentBlocks.size() == 0) continue;

			int limit;	
			if (ter.type.equals("MultiSpriteCounter") && ter.sprites.size() == 1) {
				limit = -(ter.limit); 
			}
			else {	
				limit = ter.limit;
			}
			
			goalBlocks.add(currentBlocks);
			goalLimits.add(limit);
		}
		
		if (verbose) System.out.println("findGoalBlocks blocks: " + goalBlocks);
		if (verbose) System.out.println("findGoalBlocks limits: " + goalLimits);
	}
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

		solidBlocks.removeAll(harmfulBlocks);
		solidRemovableBlocks.removeAll(harmfulBlocks);
		
		if (verbose) System.out.println("SolidBlocks: " + solidBlocks);
		if (verbose) System.out.println("SolidRemovableBlocks: " + solidRemovableBlocks);
		if (verbose) System.out.println("HarmfulBlocks: " + harmfulBlocks);
	}

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
	
	public int getPriorityValue(char key){
		return priorityValue.get(key);
	}

	public Set<Character> getAllBlocks(){
		return levelBlocks.keySet();
	}

	public ArrayList<Character> getSolidBlocks(){
		return solidBlocks;
	}
	

	public ArrayList<Character> getSolidRemovableBlocks(){
		return solidRemovableBlocks;
	}

	public ArrayList<Character> getAvatarBlocks(){
		return avatarBlocks;
	}

	public ArrayList<Character> getHarmfulBlocks(){
		return harmfulBlocks;
	}

	public ArrayList<ArrayList<Character>> getGoalBlocks(){
		return goalBlocks;
	}
	

	public ArrayList<Integer> getGoalLimits(){
		return goalLimits;
	}

	public ArrayList<Character> getOtherBlocks(){
		return otherBlocks;
	}
	
	public double getMaxScoreUnit(){
		return maxScoreUnit;
	}
	

	public double getMinScoreUnit(){
		return minScoreUnit;
	}

	public boolean isMoving(char key){
		for (SpriteData sprite : levelBlocks.get(key)) {
			if (movingTypes.contains(sprite.type)){
				return true;
			}
		}
		return false;
	}

	public boolean isSpawner(char key){
		for (SpriteData sprite : levelBlocks.get(key)) {
			if (spawnerTypes.contains(sprite.type))
				return true;
		}
		return false;
	}
	
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

	public char getFloor() {		
		return floorBlock;
	}

	private enum InteractionType{
		ALL,
		FIRST,
		SECOND
	}
}
