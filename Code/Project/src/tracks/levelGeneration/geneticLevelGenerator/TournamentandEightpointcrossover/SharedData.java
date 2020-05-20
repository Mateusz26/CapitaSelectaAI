package tracks.levelGeneration.geneticLevelGenerator.TournamentandEightpointcrossover;

import java.util.Random;
import core.game.GameDescription;
import tools.GameAnalyzer;

//Added new genetic operators: crossover & tournament selection 

public class SharedData {

    public static final int POPULATION_SIZE = 50;
    public static final int REPETITION_AMOUNT = 50;
    public static final long EVALUATION_TIME = 30000;  // TODO: revert this to 30k
    public static final long EVALUATION_STEP_TIME = 40;
    public static final double CROSSOVER_PROB = 0.9;
    public static final double MUTATION_PROB = 0.1;
    public static final int ELITISM_NUMBER = 1;
    public static final double EIPSLON = 1e-6;
    public static final int RANDOM_INIT_AMOUNT = 50;
    public static final double INSERTION_PROB = 0.3;
    public static final double DELETION_PROB = 0.3;
    public static final int MUTATION_AMOUNT = 1;
    public static final double MAX_SCORE_PERCENTAGE = 0.1;
    public static final double DRAW_FITNESS  = 0;
    public static final double MIN_SIZE = 4;
    public static final double MAX_SIZE = 18;
    public static final double MIN_SOLUTION_LENGTH = 200;
    public static final double MIN_DOTHING_STEPS = 40;
    public static final double MIN_COVER_PERCENTAGE = 0.05;
    public static final double MAX_COVER_PERCENTAGE = 0.3;
    public static final double MIN_UNIQUE_RULE_NUMBER = 3;
    public static final boolean CONSTRUCTIVE_INITIALIZATION = false;
    public static final String AGENT_NAME = "tracks.singlePlayer.tools.repeatOLETS.Agent";
    public static final String NAIVE_AGENT_NAME = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
    public static final String DO_NOTHING_AGENT_NAME = "tracks.singlePlayer.simple.doNothing.Agent";
    public static GameDescription gameDescription;
    public static GameAnalyzer gameAnalyzer;
    public static Random random;
    public static tracks.levelGeneration.constructiveLevelGenerator.LevelGenerator constructiveGen;
}
