package tracks.ruleGeneration.geneticRuleGenerator;

import java.util.ArrayList;
import java.util.Random;

import core.player.AbstractPlayer;
import tools.LevelAnalyzer;

// Extended

public class SharedData {

	public static final int POPULATION_SIZE = 50;
	public static ArrayList<String> usefulSprites;
	public static LevelAnalyzer la;
	public static final long EVALUATION_TIME = 10000;
	public static final int ELITISM_NUMBER = 1;
	public static final double CROSSOVER_PROB = 0.9;
	public static final double MUTATION_PROB = 0.1;
	public static final double INIT_RANDOM_PERCENT = 0.4;
	public static final double INIT_CONSTRUCT_PERCENT = 0.2;
	public static final double INIT_MUT_PERCENT = 0.4;
	public static final int INIT_MUTATION_AMOUNT = 2;
	public static Random random;
	public static final String BEST_AGENT_NAME = "tracks.singlePlayer.advanced.olets.Agent";
	public static final String NAIVE_AGENT_NAME = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
	public static final String RANDOM_AGENT_NAME = "tracks.singlePlayer.simple.simpleRandom.Agent";
	public static final String DO_NOTHING_AGENT_NAME = "tracks.singlePlayer.simple.doNothing.Agent";
	public static final int EVALUATION_STEP_COUNT = 300;
	public static final int REPETITION_AMOUNT = 3;
	public static final long EVALUATION_STEP_TIME = 40;
	public static final int MUTATION_AMOUNT = 2;
	public static final double INSERTION_PROB = 0.33;
	public static final double DELETION_PROB = 0.33;
	public static final double MODIFY_RULE_PROB = .34;
	public static final double MODIFY_PARAM_PROB = 0.5;
	public static final double INSERT_PARAM_PROB = 0.5;
	public static final double DELETE_PARAM_PROB = 0.5;
	public static final double PARAM_NUM_OR_SPRITE_PROB = 0.5;
	public static final double WIN_PARAM_PROB = 0.5;
	public static final int NUMERICAL_VALUE_PARAM = 2000;
	public static final int TERMINATION_LIMIT_PARAM = 1000;
	public static final int PROTECTION_COUNTER = 3;
	public static AbstractPlayer automatedAgent;
	public static AbstractPlayer naiveAgent;
	public static AbstractPlayer doNothingAgent;
	public static AbstractPlayer randomAgent;
	public static tracks.ruleGeneration.constructiveRuleGenerator.RuleGenerator constGen;
	public static String[] interactions = new String[]{
			"killSprite", "killAll", "killIfHasMore", "killIfHasLess", "killIfFromAbove",
			"killIfOtherHasMore", "transformToSingleton", "spawnBehind",
			"spawnIfHasMore", "spawnIfHasLess", "cloneSprite", "transformTo", "transformIfCounts", 
			"transformToRandomChild", "updateSpawnType", "removeScore", 
			"addHealthPoints",  "addHealthPointsToMax", "subtractHealthPoints", "increaseSpeedToAll", 
			"decreaseSpeedToAll", "setSpeedForAll", "stepBack",  "undoAll", "flipDirection",  
			"reverseDirection", "attractGaze", "align", "turnAround", "wrapAround", "teleportToExit",
			"pullWithIt", "bounceForward", "collectResource", "changeResource"};

	public static String[] terminations = new String[] {
		"SpriteCounter", "SpriteCounterMore", "MultiSpriteCounter",
		"StopCounter", "Timeout"};
	public static String[] interactionParams = new String[] {
		"scoreChange", "stype", "limit", "resource", "stype_other", "forceOrientation", "spawnPoint",
		"value", "geq", "leq"};
	public static String[] terminationParams = new String[] {
		"stype", "stype1", "stype2", "stype3"
	};

}