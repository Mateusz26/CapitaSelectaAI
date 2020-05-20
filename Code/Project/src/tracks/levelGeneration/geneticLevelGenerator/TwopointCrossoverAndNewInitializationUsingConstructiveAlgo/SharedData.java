package tracks.levelGeneration.geneticLevelGenerator.TwopointCrossoverAndNewInitializationUsingConstructiveAlgo;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import java.util.ArrayList;
import java.util.Random;

// Updated 

public class SharedData {
	
	public static final int POPULATION_SIZE = 50;
	public static final int REPETITION_AMOUNT = 25;
	public static final long EVALUATION_TIME = 40000;
	public static final long EVALUATION_STEP_TIME = 40;
	public static final double CROSSOVER_PROB = 0.7;
	public static final double MUTATION_PROB = 0.1;
	public static final int ELITISM_NUMBER = 1;
	public static final double EPSILON = 1e-6;
	public static final int RANDOM_INIT_AMOUNT = 50;
	public static final double INSERTION_PROB = 0.3;
	public static final double DELETION_PROB = 0.3;
	public static final int MUTATION_AMOUNT = 1;
	public static final double MAX_SCORE_PERCENTAGE = 0.1;
	public static final double DRAW_FITNESS = 0;
	public static final double MIN_SIZE = 4;
	public static final double MAX_SIZE = 18;
	public static final double MIN_SOLUTION_LENGTH = 200;
	public static final double MIN_DOTHING_STEPS = 40;
	public static final double MIN_COVER_PERCENTAGE = 0.05;
	public static final double MAX_COVER_PERCENTAGE = 0.3;
	public static final double MIN_UNIQUE_RULE_NUMBER = 3;
	public static final boolean CONSTRUCTIVE_INITIALIZATION = true;
	public static final String AGENT_NAME = "tracks.levelGeneration.architect.randomOLETS.Agent";
	public static final String NAIVE_AGENT_NAME = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
	public static final String DO_NOTHING_AGENT_NAME = "tracks.singlePlayer.simple.doNothing.Agent";
	public static final boolean THREADING = false;
	public static GameDescription gameDescription;
	public static tools.GameAnalyzer gameAnalyzer;
	public static Analyzer analyzer;
	public static Random random;
	public static AbstractLevelGenerator levelGen;
	public static int generatorType = 0;
	public static String floor;
	public static ArrayList<String> allSprites;
	public static boolean saveMyChromosomes = false;
	public static String tempLevel;
	public static double noise(double input, double epsilon, double random) {
		return input * (1.0 + epsilon * (random - 0.5));
	}
}
