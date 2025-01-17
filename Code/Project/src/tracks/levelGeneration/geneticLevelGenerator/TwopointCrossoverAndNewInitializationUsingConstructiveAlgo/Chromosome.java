package tracks.levelGeneration.geneticLevelGenerator.TwopointCrossoverAndNewInitializationUsingConstructiveAlgo;

import core.game.Event;
import core.game.GameDescription.SpriteData;
import core.game.GameDescription.TerminationData;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import tracks.levelGeneration.constraints.CombinedConstraints;
import ontology.Types;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import tools.LevelMapping;
import tools.StepController;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// New version 
// In this version we used 2-point crossover and combined it with the constructive generator for initialization 

public class Chromosome implements Comparable<Chromosome>{

	private ArrayList<String>[][] level;
	private ArrayList<Double> fitness;
	private double constrainFitness;
	private boolean calculated;
	private AbstractPlayer automatedAgent;
	private AbstractPlayer naiveAgent;
	private AbstractPlayer doNothingAgent;
	private StateObservation stateObs;
	
	public Chromosome(int width, int height){
		this.level = new ArrayList[height][width];
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				this.level[y][x] = new ArrayList<String>();}
		}
		this.fitness = new ArrayList<Double>();
		this.calculated = false;
		this.stateObs = null;
	}
	
	public Chromosome clone(){
		Chromosome c = new Chromosome(level[0].length, level.length);
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				c.level[y][x].addAll(level[y][x]);
			}
		}
		c.constructAgent();
		return c;}
	
	private void constructAgent(){
		try{
			Class agentClass = Class.forName(SharedData.AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			automatedAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			Class agentClass = Class.forName(SharedData.NAIVE_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			naiveAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			Class agentClass = Class.forName(SharedData.DO_NOTHING_AGENT_NAME);
			Constructor agentConst = agentClass.getConstructor(new Class[]{StateObservation.class, ElapsedCpuTimer.class});
			doNothingAgent = (AbstractPlayer)agentConst.newInstance(getStateObservation().copy(), null);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	

	public void InitializeRandom(){
		for(int i = 0; i < SharedData.RANDOM_INIT_AMOUNT; i++){
			this.mutate();
		}
		
		constructAgent();
	}

	public void InitializeConstructive()
	{
		String[] levelString = null;
		HashMap<Character, ArrayList<String>> charMap = null;

		if(SharedData.levelGen instanceof Constructive)
		{
			Constructive gen = (Constructive)SharedData.levelGen;
			levelString = gen.generateLevel(SharedData.gameDescription, null, level[0].length, level.length).split("\n");
			charMap = SharedData.levelGen.getLevelMapping();
		}
		else if(SharedData.levelGen instanceof ModifiedRandomGen)
		{
			levelString = SharedData.levelGen.generateLevel(SharedData.gameDescription,null).split("\n");
			charMap = SharedData.levelGen.getLevelMapping();
		}
		
		for(int y=0; y<levelString.length; y++){
			for(int x=0; x<levelString[y].length(); x++){
				if(levelString[y].charAt(x) != ' '){
					this.level[y][x].addAll(charMap.get(levelString[y].charAt(x)));
				}
			}
		}
		
		constructAgent();
	}
	
	public ArrayList<Chromosome> crossOver(Chromosome c){
		ArrayList<Chromosome> children = new ArrayList<Chromosome>();
		children.add(new Chromosome(level[0].length, level.length));
		children.add(new Chromosome(level[0].length, level.length));

		int pointY = SharedData.random.nextInt(level.length);
		int pointX = SharedData.random.nextInt(level[0].length);

		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				if(y < pointY){
					children.get(0).level[y][x].addAll(this.level[y][x]);
					children.get(1).level[y][x].addAll(c.level[y][x]);
				}
				else if(y == pointY){
					if(x <= pointX){
						children.get(0).level[y][x].addAll(this.level[y][x]);
						children.get(1).level[y][x].addAll(c.level[y][x]);
					}
					else{
						children.get(0).level[y][x].addAll(c.level[y][x]);
						children.get(1).level[y][x].addAll(this.level[y][x]);
					}
				}
				else{
					children.get(0).level[y][x].addAll(c.level[y][x]);
					children.get(1).level[y][x].addAll(this.level[y][x]);
				}
			}
		}
		
		children.get(0).FixLevel();
		children.get(1).FixLevel();
		
		children.get(0).constructAgent();
		children.get(1).constructAgent();
		
		return children;
	}
	

	public void mutate(){
		
		for(int i = 0; i < SharedData.MUTATION_AMOUNT; i++)
		{
			int solidFrame = 0;
			if(SharedData.analyzer.getSolidBlocks().size() > 0){
				solidFrame = 2;
			}
			int pointX = SharedData.random.nextInt(level[0].length - solidFrame) + solidFrame / 2;
			int pointY = SharedData.random.nextInt(level.length - solidFrame) + solidFrame / 2;
			if(SharedData.random.nextDouble() < SharedData.INSERTION_PROB){
				String spriteName = SharedData.allSprites.get(SharedData.random.nextInt(SharedData.allSprites.size()));
				ArrayList<SpritePointData> freePositions = getFreePositions(new ArrayList<String>(Arrays.asList(new String[]{spriteName})));
				int index = SharedData.random.nextInt(freePositions.size());
				level[freePositions.get(index).y][freePositions.get(index).x].add(spriteName);
			}

			else if(SharedData.random.nextDouble() < SharedData.INSERTION_PROB + SharedData.DELETION_PROB){
				level[pointY][pointX].clear();
				if (SharedData.floor != null) {
					level[pointY][pointX].add(SharedData.floor);
				}
			}
			else{
				int point2X = SharedData.random.nextInt(level[0].length - solidFrame) + solidFrame / 2;
				int point2Y = SharedData.random.nextInt(level.length - solidFrame) + solidFrame / 2;
				
				ArrayList<String> temp = level[pointY][pointX];
				level[pointY][pointX] = level[point2Y][point2X];
				level[point2Y][point2X] = temp;
			}
		}
		
		FixLevel();
	}
	

	private ArrayList<SpritePointData> getFreePositions(ArrayList<String> sprites){
		ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> tileSprites = level[y][x];
				boolean found = false;
				for(String stype:tileSprites){
					found = found || sprites.contains(stype);
					found = found || SharedData.gameAnalyzer.getSolidSprites().contains(stype);
				}
				
				if(!found){
					positions.add(new SpritePointData("", x, y));
				}
			}
		}
		
		return positions;
	}
	
	private ArrayList<SpritePointData> getPositions(ArrayList<String> sprites){
		ArrayList<SpritePointData> positions = new ArrayList<SpritePointData>();
		
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> tileSprites = level[y][x];
				for(String stype:tileSprites){
					if(sprites.contains(stype)){
						positions.add(new SpritePointData(stype, x, y));
					}
				}
			}
		}
		
		return positions;
	}
	

	private void FixPlayer(){
		ArrayList<SpriteData> avatar = SharedData.gameDescription.getAvatar();
		ArrayList<String> avatarNames = new ArrayList<String>();
		for(SpriteData a:avatar){
			avatarNames.add(a.name);
		}

		ArrayList<SpritePointData> avatarPositions = getPositions(avatarNames); 
		if(avatarPositions.size() == 0){
			ArrayList<SpritePointData> freePositions = getFreePositions(avatarNames);
			
			int index = SharedData.random.nextInt(freePositions.size());
			level[freePositions.get(index).y][freePositions.get(index).x].add(avatarNames.get(SharedData.random.nextInt(avatarNames.size())));
		}
		else if(avatarPositions.size() > 1){
			int notDelete = SharedData.random.nextInt(avatarPositions.size());
			int index = 0;
			for(SpritePointData point:avatarPositions){
				if(index != notDelete){
					level[point.y][point.x].remove(point.name);
				}
				index += 1;
			}
		}
	}
	
	private void FixLevel(){
		FixPlayer();
	}
	
	public LevelMapping getLevelMapping(){
		LevelMapping levelMapping = new LevelMapping(SharedData.gameDescription);
		levelMapping.clearLevelMapping();
		char c = 'a';
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				if(levelMapping.getCharacter(level[y][x]) == null){
					levelMapping.addCharacterMapping(c, level[y][x]);
					c += 1;
				}
			}
		}
		
		return levelMapping;
	}

	public String getLevelString(LevelMapping levelMapping){
		String levelString = "";
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				levelString += levelMapping.getCharacter(level[y][x]);
			}
			levelString += "\n";
		}
		
		levelString = levelString.substring(0, levelString.length() - 1);
		
		return levelString;
	}
	private double getCoverPercentage(){
		int objects = 0;
		int borders = 0;
		if(SharedData.analyzer.getSolidBlocks().size() > 0 ||
				SharedData.analyzer.getSolidRemovableBlocks().size() > 0){
			borders = 1;
		}
		
		for (int y = borders; y < level.length - borders; y++) {
			for (int x = borders; x < level[y].length - borders; x++) {
				if (level[y][x].size() > 1) {
					objects++;
				}
				else if (!level[y][x].contains(SharedData.floor) && level[y][x].size() == 1){
					objects++;
				}	 
			}
		}
		
		return 1.0 * objects / (level.length * level[0].length);
	}
	
	private StateObservation getStateObservation(){
		if(stateObs != null){
			return stateObs;
		}
		
		LevelMapping levelMapping = getLevelMapping();
		String levelString = getLevelString(levelMapping);
		stateObs = SharedData.gameDescription.testLevel(levelString, levelMapping.getCharMapping());
		return stateObs;
	}
	
	private HashMap<String, Integer> calculateNumberOfObjects(){
		HashMap<String, Integer> objects = new HashMap<String, Integer>();
		ArrayList<SpriteData> allSprites = SharedData.gameDescription.getAllSpriteData();
		for(SpriteData sprite:allSprites){
			objects.put(sprite.name, 0);
		}
		for(int y = 0; y < level.length; y++){
			for(int x = 0; x < level[y].length; x++){
				ArrayList<String> sprites = level[y][x];
				for(String stype:sprites){
					if(objects.containsKey(stype)){
						objects.put(stype, objects.get(stype) + 1);
					}
					else{
						objects.put(stype, 1);
					}
				}
			}
		}
		
		return objects;
	}
	
	private double getGameScore(double scoreDiff, double maxScore){
		if(maxScore == 0){
			return 1;
		}
		if(scoreDiff <= 0){
			return 0;
		}
		double result = (3 * scoreDiff / maxScore);
		return 2 / (1 + Math.exp(-result)) - 1;
	}

	private boolean isPlayerCauseDeath(){
		for(TerminationData t:SharedData.gameDescription.getTerminationConditions()){
			String[] winners = t.win.split(",");
			Boolean win = Boolean.parseBoolean(winners[0]);

			for(String s:t.sprites){
				if(!win & SharedData.gameDescription.getAvatar().contains(s)){
					return true;
				}
			}
			
		}
		
		return false;
	}
	
	private double getUniqueRuleScore(StateObservation gameState, double minUniqueRule){
		double unique = 0;
		HashMap<Integer, Boolean> uniqueEvents = new HashMap<Integer, Boolean>();
		for(Event e:gameState.getEventsHistory()){
			int code = e.activeTypeId + 10000 * e.passiveTypeId;
			if(!uniqueEvents.containsKey(code)){
				unique += 1;
				uniqueEvents.put(code, true);
			}
		}

		if(isPlayerCauseDeath() && gameState.getGameWinner() == WINNER.PLAYER_LOSES){
			unique -= 1;
		}
		
		return 2 / (1 + Math.exp(-3 * unique / minUniqueRule)) - 1;
	}
	
	private int getNaivePlayerResult(StateObservation stateObs, int steps, AbstractPlayer agent){
		int i =0;
		for(i=0;i<steps;i++){
			if(stateObs.isGameOver()){
				break;
			}
			Types.ACTIONS bestAction = agent.act(stateObs, null);
			stateObs.advance(bestAction);
		}
		
		return i;
	}
	
	public ArrayList<Double> calculateFitness(long time, int number){
		if(!calculated){
			calculated = true;
			StateObservation stateObs = getStateObservation();

			int doNothingLength = 0;
			WINNER doNothingState = WINNER.PLAYER_LOSES;
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				StateObservation tempState = stateObs.copy();
				doNothingLength += getNaivePlayerResult(tempState, 2001, doNothingAgent);
				if(tempState.getGameWinner().equals(WINNER.PLAYER_WINS)){
					doNothingState = WINNER.PLAYER_WINS;
				}
			}
			doNothingLength = doNothingLength/SharedData.REPETITION_AMOUNT;
			double coverPercentage = getCoverPercentage();
			double maxScore = 0;
			if(SharedData.gameAnalyzer.getMinScoreUnit() > 0){
				double numberOfUnits = SharedData.gameAnalyzer.getMaxScoreUnit() / (SharedData.MAX_SCORE_PERCENTAGE * SharedData.gameAnalyzer.getMinScoreUnit());
				maxScore = numberOfUnits * SharedData.gameAnalyzer.getMinScoreUnit();
			}
		
			double correction=0;
			for(TerminationData t : SharedData.gameDescription.getTerminationConditions()){
				if(t.type.equals("Timeout")){
					correction=(1.0 / SharedData.gameDescription.getTerminationConditions().size()) / 7;
					break;
				}
			}

			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("solutionLength", 100);
			parameters.put("bestPlayer", Types.WINNER.PLAYER_LOSES);
			parameters.put("minSolutionLength", SharedData.MIN_SOLUTION_LENGTH);
			parameters.put("doNothingSteps", doNothingLength);
			parameters.put("doNothingState", doNothingState);
			parameters.put("minDoNothingSteps", SharedData.MIN_DOTHING_STEPS);
			parameters.put("coverPercentage", coverPercentage);
			parameters.put("minCoverPercentage", SharedData.MIN_COVER_PERCENTAGE);
			parameters.put("maxCoverPercentage", SharedData.MAX_COVER_PERCENTAGE);
			parameters.put("numOfObjects", calculateNumberOfObjects());
			parameters.put("gameAnalyzer", SharedData.gameAnalyzer);
			parameters.put("gameDescription", SharedData.gameDescription);

			CombinedConstraints constraint = new CombinedConstraints();
			constraint.addConstraints(new String[]{"SolutionLengthConstraint", "DeathConstraint", 
					"CoverPercentageConstraint", "SpriteNumberConstraint", "GoalConstraint", "AvatarNumberConstraint", "WinConstraint"});
			constraint.setParameters(parameters);
			constrainFitness = constraint.checkConstraint() + correction;
			
			StateObservation bestState = null;
			int bestSolSize = -1;
			if (constrainFitness > 0.72) {
				StepController stepAgent = new StepController(automatedAgent, SharedData.EVALUATION_STEP_TIME);
				ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
				elapsedTimer.setMaxTimeMillis(time);
				stepAgent.playGame(stateObs.copy(), elapsedTimer);
				bestState = stepAgent.getFinalState();
				bestSolSize = stepAgent.getSolution().size();
				parameters.put("solutionLength", bestSolSize);
				parameters.put("bestPlayer", bestState.getGameWinner());
				constraint.setParameters(parameters);
				constrainFitness = constraint.checkConstraint() + correction;
			}
			if(constrainFitness > 0.99999){
				StateObservation naiveState = null;
				for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
					StateObservation tempState = stateObs.copy();
					getNaivePlayerResult(tempState, bestSolSize, naiveAgent);
					if(naiveState == null || tempState.getGameScore() > naiveState.getGameScore()){
						naiveState = tempState;
					}
				}
				double scoreDiffScore = getGameScore(bestState.getGameScore() - naiveState.getGameScore(), maxScore);
				double ruleScore = getUniqueRuleScore(bestState, SharedData.MIN_UNIQUE_RULE_NUMBER);

				fitness.add(scoreDiffScore);
				fitness.add(ruleScore);
				System.out.println("Chromosome #" + number + " SolutionLength:" + bestSolSize + " doNothingSteps:" + doNothingLength 
						+ " coverPercentage:" + coverPercentage + " bestPlayer:" + 
						(bestState == null ? "N/A" : bestState.getGameWinner()) + "\n\tFitness: " + fitness);
			}
			else {
				System.out.println("Chromosome #" + number + " SolutionLength:" + bestSolSize + " doNothingSteps:" + doNothingLength 
						+ " coverPercentage:" + coverPercentage + " bestPlayer:" + 
						(bestState == null ? "N/A" : bestState.getGameWinner()) + "\n\tConstrain Fitness: " + constrainFitness);
			}

			this.automatedAgent = null;
			this.naiveAgent = null;
			this.stateObs = null;
		}

		return fitness;
	}

	public ArrayList<Double> getFitness(){
		return fitness;
	}

	public double getCombinedFitness(){
		double result = 0;
		for(double v: this.fitness){
			result += v;
		}
		return result / this.fitness.size();
	}
	
	public double getConstrainFitness(){
		return constrainFitness;
	}

	public class SpritePointData{
		public String name;
		public int x;
		public int y;
		
		public SpritePointData(String name, int x, int y){
			this.name = name;
			this.x = x;
			this.y = y;
		}
	}

	@Override
	public int compareTo(Chromosome o) {
		if(this.constrainFitness < 1 || o.constrainFitness < 1){
			if(this.constrainFitness < o.constrainFitness){
				return 1;
			}
			if(this.constrainFitness > o.constrainFitness){
				return -1;
			}
			return 0;
		}
		
		double firstFitness = 0;
		double secondFitness = 0;
		for(int i=0; i<this.fitness.size(); i++){
			firstFitness += this.fitness.get(i);
			secondFitness += o.fitness.get(i);
		}
		
		if(firstFitness > secondFitness){
			return -1;
		}
		
		if(firstFitness < secondFitness){
			return 1;
		}
		
		return 0;
	}
}
