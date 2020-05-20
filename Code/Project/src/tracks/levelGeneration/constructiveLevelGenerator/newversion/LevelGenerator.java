package tracks.levelGeneration.constructiveLevelGenerator.newversion;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import core.game.GameDescription;
import core.game.StateObservation;
import core.game.GameDescription.SpriteData;
import core.generator.AbstractLevelGenerator;
import core.player.AbstractPlayer;

import ontology.Types;

import tools.ElapsedCpuTimer;
import tools.StepController;
import tracks.levelGeneration.constructiveLevelGenerator.newversion.LevelGeneratorVariables.*;

//improved 

// identify type of the game and based on type of game define level size and
// for each sprite give pos 

public class LevelGenerator extends AbstractLevelGenerator {
	
	private char[][] gameField;
	private int length = -1;
	private int width = -1;
	private int fieldSize;
	private double coverPercentage;
	private char wallKey;
	private boolean biggerField = false;
	private boolean gigantField = false;
	boolean hasWalls = false;
	private boolean racingGame = false;
	private boolean satelliteGame = false;
	private GameDescription game;
	private Random random;
	public int bomberCount = 0;
	public int SpawnPointCount = 0;
	public int randomBomberCount = 0;
	public int immovableCount = 0;
	private int minSize;
	public final int FEW_WALLS = 5;
	public final int MEDIUM_WALLS = 15;
	public final int MANY_WALLS = 25;
	private int wallCount = MEDIUM_WALLS;
	private int minWalls = wallCount - 10;
	public final int NO_WALLS = 0;
	public final int ALL_WALLS = 1;
	public final int SIDE_WALLS = 2;
	private int walls = NO_WALLS;
	public int avatarGoalPosition = 0;
	public final static int LOW_PRIO = 0;
	public final static int MIDDLE_PRIO = 1;
	public final static int HIGH_PRIO = 2;
	boolean negativeLevel = false;
	boolean gridLevel = false;
	private ArrayList<String> neutralElem = new ArrayList<String>();
	private char neutralKey;
	private SpriteAssignment newSpriteA = new SpriteAssignment();
	private ArrayList<SpriteData> sprites;
	private ArrayList<SpriteAssignment> spriteAssgnmt = new ArrayList<SpriteAssignment>();
	private ArrayList<TestString> testStrings = new ArrayList<TestString>();
	private String avatarName;
	private char avatarKey;

	String sampleMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
	String sampleRSController = "tracks.singlePlayer.advanced.sampleRS.Agent";
	String sampleRHEAController = "tracks.singlePlayer.advanced.sampleRHEA.Agent";
	String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";
	String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
	String doNothingController = "tracks.singlePlayer.simple.doNothing.Agent";
	String sampleOneStepController = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
	String sampleFlatMCTSController = "tracks.singlePlayer.simple.greedyTreeSearch.Agent";
	
	private int numberOfGeneratedLevel = 10;
	private int numberOfTests = 10;
	StateObservation stateObs = null;

	AbstractPlayer automatedAgent = null;

	public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedTimer) {		
	}

	private Position getRndmPos(preferredSettings prefSet) {
		Position rndPosition = new Position();
		do {if (prefSet.prefArea.col[1] != prefSet.prefArea.col[0]) {
				rndPosition.col = random.nextInt(prefSet.prefArea.col[1] - prefSet.prefArea.col[0] + 1)
						+ prefSet.prefArea.col[0];
			} else {rndPosition.col = prefSet.prefArea.col[0];}
		} while (rndPosition.col == prefSet.tabooCol
				|| (rndPosition.col % 2 == prefSet.evenUnevenCol && prefSet.evenUnevenCol != -1));

		do {if (prefSet.prefArea.row[1] != prefSet.prefArea.row[0]) {
				rndPosition.row = random.nextInt(prefSet.prefArea.row[1] - prefSet.prefArea.row[0] + 1)
						+ prefSet.prefArea.row[0];
			} else {rndPosition.row = prefSet.prefArea.row[0];}
		} while (rndPosition.row == prefSet.tabooRow
				|| (rndPosition.row % 2 == prefSet.evenUnevenRow && prefSet.evenUnevenRow != -1));

		if (rndPosition.row > length - 1 || rndPosition.col > width - 1) {
			System.out.println("ERROR");
		}
		return rndPosition;
	}

	private void placeSprite(SpriteAssignment spriteAssign) {
		int start = 0;
		int endCol = width;
		int endRow = length;
		if (hasWalls) {
			start = 1;
			endCol = width - 1;
			endRow = length - 1;
		}

		Position rndPosition = new Position();
		char key = spriteAssign.key;
		preferredSettings prefSet = spriteAssign.prefSet;
		int roundcount = 0;
		do {
			rndPosition = getRndmPos(prefSet);
			roundcount++;
		} while (gameField[rndPosition.row][rndPosition.col] != prefSet.onlyPlaceOnKey && roundcount < 100);

		if (prefSet.fillRandom) {
			if (!(prefSet.fillRow = random.nextBoolean())) {
				prefSet.fillCol = true;
			}
		}

		if (prefSet.fillRow) {
			for (int col = start; col < endCol; col++) {
				if (gameField[rndPosition.row][col] != '?') {
					if (prefSet.overwrite) {
						preferredSettings tmpPref = getPrefSettings(gameField[rndPosition.row][col]);
						tmpPref.tabooCol = col;
						Position tmpPos = getRndmPos(tmpPref);
						gameField[tmpPos.row][tmpPos.col] = gameField[rndPosition.row][col];
						gameField[rndPosition.row][col] = key;
					}
				} else {
					gameField[rndPosition.row][col] = key;
				}

			}
		} else if (prefSet.fillCol) {
			for (int row = start; row < endRow; row++) {
				if (gameField[row][rndPosition.col] != '?') {
					if (prefSet.overwrite) {
						preferredSettings tmpPref = getPrefSettings(gameField[row][rndPosition.col]);
						tmpPref.tabooRow = row;
						Position tmpPos = getRndmPos(tmpPref);
						gameField[tmpPos.row][tmpPos.col] = gameField[row][rndPosition.col];
						gameField[row][rndPosition.col] = key;
					}
				} else {
					gameField[row][rndPosition.col] = key;
				}
			}

		} else {

			gameField[rndPosition.row][rndPosition.col] = key;
		}

		if (prefSet.mustRow != -1) {
			for (int col = prefSet.prefArea.col[0]; col <= prefSet.prefArea.col[1]; col++) {
				if (gameField[prefSet.mustRow][col] == prefSet.onlyPlaceOnKey) {
					if (prefSet.evenUnevenCol == -1) {
						gameField[prefSet.mustRow][col] = key;
					} else {
						if (prefSet.evenUnevenCol != col % 2) {
							gameField[prefSet.mustRow][col] = key;
						}
					}
				}
			}
		}
		if (prefSet.mustCol != -1) {
			for (int row = prefSet.prefArea.row[0]; row <= prefSet.prefArea.row[1]; row++) {
				if (gameField[row][prefSet.mustCol] == prefSet.onlyPlaceOnKey) {
					if (prefSet.evenUnevenRow == -1) {
						gameField[row][prefSet.mustCol] = key;
					} else {
						if (prefSet.evenUnevenRow != row % 2) {
							gameField[row][prefSet.mustCol] = key;
						}
					}
				}
			}
		}

		if (prefSet.surroundWalls > 0) {
			for (int x = -1; x < 2; x++) {
				for (int y = -1; y < 2; y++) {
					if (gameField[rndPosition.row + x][rndPosition.col + y] == '?') {
						gameField[rndPosition.row + x][rndPosition.col + y] = wallKey;
					}
				}
			}
			switch (prefSet.surroundWalls) {
			case 1:
				gameField[rndPosition.row - 1][rndPosition.col] = prefSet.fillGap;
				break;
			case 2:
				gameField[rndPosition.row][rndPosition.col + 1] = prefSet.fillGap;
				break;
			case 3:
				gameField[rndPosition.row + 1][rndPosition.col] = prefSet.fillGap;
				break;
			case 4:
				gameField[rndPosition.row][rndPosition.col - 1] = prefSet.fillGap;
				break;
			}
		}
		if (prefSet.noWallsInRow) {
			for (int col = prefSet.prefArea.col[0]; col < prefSet.prefArea.col[1]; col++) {
				if (gameField[rndPosition.row][col] == wallKey) {
					gameField[rndPosition.row][col] = '?';}
			}
		}
	}
	private void initAgent(String result) {
		String levelString = result;
		stateObs = game.testLevel(levelString);

		try {
			Class agentClass;
			agentClass = Class.forName(sampleMCTSController);
			Constructor agentConst = agentClass.getConstructor(StateObservation.class, ElapsedCpuTimer.class);
			automatedAgent = (AbstractPlayer) agentConst.newInstance(stateObs.copy(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private TestResult testGame(String result) {
		TestResult testResult = new TestResult();
		StepController stepAgent = new StepController(automatedAgent, 40);
		ElapsedCpuTimer elapsedTimer2 = new ElapsedCpuTimer();
		elapsedTimer2.setMaxTimeMillis(60000);
		stepAgent.playGame(stateObs.copy(), elapsedTimer2);

		StateObservation bestState = stepAgent.getFinalState();
		ArrayList<Types.ACTIONS> bestSol = stepAgent.getSolution();

		System.out.println("SolutionLength:" + bestSol.size() + " bestPlayer:" + bestState.getGameWinner());

		testResult.bestSol = bestSol;
		testResult.bestState = bestState;
		return testResult;	}

	private String getLevelString() {
		String result = "";
		for (int row = 0; row < length; row++) {
			for (int col = 0; col < width; col++) {
				result += gameField[row][col];
			}
			result += "\n";
		}
		result += "\n";

		return result;
	}

	private boolean lastCheck(boolean hasNeutral) {
		boolean hasAvatar = false;
		for (int row = 0; row < length; row++) {
			for (int col = 0; col < width; col++) {
				if (gameField[row][col] == avatarKey) {
					hasAvatar = true;
				}
				if (gameField[row][col] == '?') {
					gameField[row][col] = neutralKey;
				}

			}
		}
		return hasAvatar;
	}
	private void printLevel() {
		for (int row = 0; row < length; row++) {
			for (int col = 0; col < width; col++) {
				System.out.print(gameField[row][col]);
			}
			System.out.println();
		}
		System.out.println();
	}

	private boolean findNeutralElement() {
		boolean hasNeutral = false;
		neutralKey = ' ';
		for (Map.Entry<Character, ArrayList<String>> pair : game.getLevelMapping().entrySet()) {
			if (pair.getKey() == ',' && pair.getValue().toString().contains("ground")) {
				neutralElem.add("ground");
				hasNeutral = true;
				neutralKey = ',';
				break;
			} else if (pair.getKey() == '_' && pair.getValue().toString().contains("floor")
					&& pair.getValue().size() == 1) {
				neutralElem = new ArrayList<String>();
				neutralElem.add("floor");
				hasNeutral = true;
				neutralKey = '_';
				break;
			} else if (pair.getKey() == 's' && pair.getValue().toString().contains("stratosphere")) {
				neutralElem = new ArrayList<String>();
				neutralElem.add("stratosphere");
				hasNeutral = true;
				neutralKey = 's';
			} else if (pair.getKey() == '+' && pair.getValue().toString().contains("floor")
					&& pair.getValue().size() == 1) {
				neutralElem = new ArrayList<String>();
				neutralElem.add("floor");
				hasNeutral = true;
				neutralKey = '+';
			} else if (pair.getKey() == '.' && !hasNeutral) {

				hasNeutral = true;
				for (int index = 0; index < pair.getValue().size(); index++) {
					neutralElem.add(pair.getValue().get(index).toString());
				}
				neutralKey = '.';
			}
		}
		return hasNeutral;
	}


	private void parseGame(boolean hasWalls) {
		boolean allNeut = true;

		for (Map.Entry<Character, ArrayList<String>> pair : game.getLevelMapping().entrySet()) {
			if (pair.getValue().contains("wall") && !hasWalls) {
				continue;
			}
			boolean foundSprite = false;
			newSpriteA = new SpriteAssignment();
			allNeut = true;

			newSpriteA.setKey(pair.getKey());

			if (pair.getValue().contains("wall")) {
				wallKey = pair.getKey();
				if (pair.getKey() == 'o')
					walls = SIDE_WALLS;
			}
			for (int index = 0; index < pair.getValue().size(); index++) {
				if (!neutralElem.contains(pair.getValue().get(index).toString())) {
					allNeut = false;
				}

			}

			for (int sp = 0; sp < sprites.size(); sp++) {

				String spriteString = sprites.get(sp).toString();

				String spriteName = sprites.get(sp).name;
				if (spriteName.equals("george")) {
					biggerField = true;
				}
				if (spriteName.equals("garbage")) {
					gigantField = true;
				}

				if (pair.getValue().contains(sprites.get(sp).name)) {
					newSpriteA.addSprite(sprites.get(sp));
					foundSprite = true;
					newSpriteA.addName(spriteName);
					for (int avatar = 0; avatar < game.getAvatar().size(); avatar++) {
						if (sprites.get(sp).name.equals(game.getAvatar().get(avatar).name)) {
							newSpriteA.isAvatar = true;
							avatarKey = pair.getKey();
						}
					}
					switch (sprites.get(sp).type) {
					case "Bomber":
						bomberCount++;
						break;
					case "randomBomber":
						randomBomberCount++;
						break;
					case "SpawnPoint":
						SpawnPointCount++;
						break;
					case "Immovable":
						immovableCount++;
						break;
					}
					if (!neutralElem.contains(spriteName) || allNeut) {
						if (newSpriteA.getType() == avatarName) {
							String spriteType = sprites.get(sp).type;
							newSpriteA.setType(spriteType);
						}
					}
					if (spriteString.contains("limit=")) {
						String tmpString = spriteString.substring(spriteString.indexOf("limit=") + 6);
						try {
							newSpriteA.limit = Integer.parseInt((tmpString.substring(0, tmpString.indexOf(" "))));
						} catch (Exception e) {
							newSpriteA.limit = Integer.parseInt((tmpString.substring(0)));
						}
					}
					continue;

				}
			}
			if (foundSprite)
				spriteAssgnmt.add(newSpriteA);
		}
	}

	private void initGameField(boolean hasWalls) {
		if (biggerField) {
			width = (int) Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble())) + 10;
			length = (int) Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble())) + 10;
		} else if (racingGame) {
			width = (int) Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble())) + 20;
			length = (int) Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble())) + 2;
		} else if (gigantField) {
			width = (int) Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble())) + 50;
			length = (int) Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble())) + 50;
		} else {
			width = (int) Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble())) + 2;
			length = (int) Math.max(minSize, sprites.size() * (1 + 0.25 * random.nextDouble())) + 2;
		}
		fieldSize = width * length;
		coverPercentage = 0.1;

		gameField = new char[length][width];

		for (int row = 0; row < length; row++) {
			for (int col = 0; col < width; col++) {

				if (negativeLevel) {
					gameField[row][col] = wallKey;
				} else {

					if (walls == NO_WALLS) {
						gameField[row][col] = '?';
					} else if (walls == SIDE_WALLS && (col == 0 || col == width - 1)) {
						gameField[row][col] = wallKey;
					} else if (walls == ALL_WALLS && (col == 0 || col == width - 1 || row == 0 || row == length - 1)) {
						gameField[row][col] = wallKey;
					} else {
						if (gridLevel) {
							if ((row % (length / 3) == 0 || col % (width / 3) == 0) && random.nextInt(10) > 1) {
								gameField[row][col] = wallKey;
							} else {
								gameField[row][col] = '?';
							}
						} else {
							gameField[row][col] = '?';
						}
					}
				}
			}
		}

		if (negativeLevel) {
			Position tmpPos = new Position(length / 2, width / 2);
			int deletedWalls = 1;
			while ((length - 1) * (width - 1) * 0.5 > deletedWalls) {
				switch (random.nextInt(4)) {
				case 0: 
					if (tmpPos.row - 1 > 0) {
						if (gameField[tmpPos.row - 1][tmpPos.col] == wallKey) {
							gameField[tmpPos.row - 1][tmpPos.col] = '?';
							deletedWalls++;
						}
						tmpPos.row--;
					}
					break;
				case 1:
					if (tmpPos.col + 1 < width - 1) {
						if (gameField[tmpPos.row][tmpPos.col + 1] == wallKey) {
							gameField[tmpPos.row][tmpPos.col + 1] = '?';
							deletedWalls++;
						}
						tmpPos.col++;
					}
					break;
				case 2:
					if (tmpPos.row + 1 < length - 1) {
						if (gameField[tmpPos.row + 1][tmpPos.col] == wallKey) {
							gameField[tmpPos.row + 1][tmpPos.col] = '?';
							deletedWalls++;
						}
						tmpPos.row++;
					}
					break;
				case 3:
					if (tmpPos.col - 1 > 0) {
						if (gameField[tmpPos.row][tmpPos.col - 1] == wallKey) {
							gameField[tmpPos.row][tmpPos.col - 1] = '?';
							deletedWalls++;
						}
						tmpPos.col--;
					}
					break;
				}
			}

		}
	}


	private preferredSettings getPrefSettings(char key) {
		for (int i = 0; i < spriteAssgnmt.size(); i++) {
			if (spriteAssgnmt.get(i).key == key)
				return spriteAssgnmt.get(i).prefSet;
		}
		return null;
	}

	private void addPrefSetToAssignment(boolean hasWalls) {

		preferredSettings prefSet = new preferredSettings();

		int maxEnemy = (int) (fieldSize * 0.085);
		int minEnemy = (int) (fieldSize * 0.085) / 2;
		int tempEnemy = 0;
		int tempAlly = 0;
		int maxSpritePlacement = (int) (fieldSize * coverPercentage / spriteAssgnmt.size());
		int boxcount = 0;
		for (int assignment = 0; assignment < spriteAssgnmt.size(); assignment++) {
			prefSet = new preferredSettings();

			prefSet.prefArea.row[0] = 0;
			prefSet.prefArea.row[1] = length - 1;
			prefSet.prefArea.col[0] = 0;
			prefSet.prefArea.col[1] = width - 1;

			if (walls == ALL_WALLS) {
				prefSet.prefArea.row[0] = 1;
				prefSet.prefArea.row[1] = length - 2;
				prefSet.prefArea.col[0] = 1;
				prefSet.prefArea.col[1] = width - 2;

			} else if (walls == SIDE_WALLS) {
				prefSet.prefArea.row[0] = 0;
				prefSet.prefArea.row[1] = length - 1;
				prefSet.prefArea.col[0] = 1;
				prefSet.prefArea.col[1] = width - 2;
			}

			// System.out.print(spriteAssgnmt.get(assignment).key + " ");
			prefSet.placeCount = random.nextInt(10) + 1;
			boolean getSpriteInfo = true;

			for (int sprite = 0; sprite < spriteAssgnmt.get(assignment).sprites.size(); sprite++) {
				if (minEnemy < 0)
					minEnemy = 0;
				if (maxEnemy <= 0)
					maxEnemy = 1;
				if (spriteAssgnmt.get(assignment).isAvatar) {
					getSpriteInfo = spriteAssgnmt.get(assignment).isAvatar;
				}

				if (getSpriteInfo) {
					SpriteData tmpSprite = spriteAssgnmt.get(assignment).sprites.get(sprite);
					switch (tmpSprite.type) {

					case "FlakAvatar":
						prefSet.placeCount = 1;
						prefSet.noWallsInRow = true;
						if (hasWalls) {
							prefSet.prefArea.row[0] = length - 2;
							prefSet.prefArea.row[1] = length - 2;
						} else {
							prefSet.prefArea.row[0] = length - 1;
							prefSet.prefArea.row[1] = length - 1;
						}
						break;

					case "MovingAvatar":
						prefSet.placeCount = 1;

						if (racingGame) {
							prefSet.prefArea.col[0] = 1;
							prefSet.prefArea.col[1] = 1;
							if (spriteAssgnmt.get(assignment).getKey() == 'B') {
								prefSet.placeCount = 0;
							}
						}
						avatarGoalPosition = random.nextInt(4) + 1;
						switch (avatarGoalPosition) {
						case 1:
							prefSet.prefArea.row[0] = length / 2 - 1;
							prefSet.prefArea.row[1] = length - 2;
							prefSet.prefArea.col[0] = width / 2 - 1;
							prefSet.prefArea.col[1] = width - 2;
							break;
						case 2:
							prefSet.prefArea.row[0] = 1;
							prefSet.prefArea.row[1] = length / 2 - 1;
							prefSet.prefArea.col[0] = width / 2 - 1;
							prefSet.prefArea.col[1] = width - 2;
							break;
						case 3:
							prefSet.prefArea.row[0] = length / 2 - 1;
							prefSet.prefArea.row[1] = length - 2;
							prefSet.prefArea.col[0] = 1;
							prefSet.prefArea.col[1] = width / 2 - 1;
							break;
						case 4:
							prefSet.prefArea.row[0] = 1;
							prefSet.prefArea.row[1] = length / 2 - 1;
							prefSet.prefArea.col[0] = 1;
							prefSet.prefArea.col[1] = width / 2 - 1;
							break;

						}

						break;
					case "ShootAvatar":
						if (satelliteGame) {
							prefSet.prefArea.row[0] = 5;
							prefSet.prefArea.row[1] = length - 3;
						}
						prefSet.placeCount = 1;
						break;
					case "OrientedAvatar":
						prefSet.placeCount = 1;
						break;
					case "OngoingTurningAvatar":

						prefSet.placeCount = 1;
						break;
					case "OngoingAvatar":

						prefSet.placeCount = 1;
						break;
					case "key":
						prefSet.placeCount = 1;
						break;
					case "Chaser":
						prefSet.placeCount = random.nextInt(3) + 1;
						break;
					case "RandomNPC":
						prefSet.placeCount = random.nextInt(3) + 1;
						break;
					case "Flicker":
						break;
					case "Immovable":
						if(immovableCount<1)immovableCount=1;
						prefSet.placeCount = random.nextInt((int) (fieldSize * 0.25 / immovableCount)) + 1;
						break;
					case "Missile":
						break;
					case "OrientedFlicker":
						break;
					case "Passive":
						break;
					case "PathAltChaser":
						prefSet.placeCount = random.nextInt(maxEnemy - minEnemy) + minEnemy;
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "Portal":
						prefSet.placeCount = 1;
						break;
					case "RandomBomber":
						if (randomBomberCount < 1) randomBomberCount = 1;
							prefSet.placeCount = random.nextInt((int) (fieldSize * 0.05 / randomBomberCount)) + 1;
						break;
					case "RandomMissile":
						prefSet.placeCount = random.nextInt(maxEnemy - minEnemy) + minEnemy;
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "RandomPathAltChaser":
						break;
					case "Resource":
						break;
					case "SpawnPoint":
						if(SpawnPointCount<1)SpawnPointCount=1;
						prefSet.placeCount = random.nextInt((int) (fieldSize * 0.05 / SpawnPointCount)) + 1;
						break;
					case "Spreader":
						prefSet.placeCount = random.nextInt(maxEnemy - minEnemy + 1) + minEnemy;
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "Bomber":
						if(bomberCount<1)bomberCount=1;
						prefSet.placeCount = random.nextInt((int) (fieldSize * 0.05 / bomberCount)) + 1;
						break;
					case "BomberRandomMissile":
						prefSet.placeCount = random.nextInt(maxEnemy - minEnemy + 1) + minEnemy;
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "Door":
						break;
					case "Fleeing":
						break;

					default:
						break;
					}

					switch (tmpSprite.name) {

					case "turnup":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = length - 2;
						prefSet.prefArea.col[0] = width - 2;
						prefSet.prefArea.col[1] = width - 2;
						prefSet.placeCount = 1;
						prefSet.fillCol = true;
						break;
					case "turnleft":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = 1;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width - 2;
						prefSet.fillRow = true;
						break;
					case "turndown":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = length - 2;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						prefSet.fillCol = true;
						break;
					case "turnright":
						prefSet.prefArea.row[0] = length - 2;
						prefSet.prefArea.row[1] = length - 2;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width - 2;
						prefSet.fillRow = true;
						break;
					case "smoke":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = 2;
						prefSet.placeCount = 1;
						break;
					case "lock":
						if (spriteAssgnmt.get(assignment).sprites.get(0).name.equals("cliff")) {
							prefSet.placeCount = 5;
						}
						break;
					case "key":
						if (spriteAssgnmt.get(assignment).sprites.get(1).name.equals("cliff")) {
							prefSet.placeCount = 5;
						} else {
							prefSet.placeCount = 1;
						}
						break;
					case "portalSlow":
					case "portalFast":
						if (hasWalls) {
							prefSet.prefArea.col[0] = width - 2;
							prefSet.prefArea.col[1] = width - 2;
							prefSet.prefArea.row[0] = 1;
						} else {
							prefSet.prefArea.row[0] = 0;
						}
						prefSet.prefArea.row[1] = 4;
						prefSet.placeCount = random.nextInt(maxSpritePlacement) + 1;
						break;
					case "base":
						if (tmpSprite.type.equals("Immovable")) {
							prefSet.placeCount = random.nextInt(5) + 10;
							prefSet.prefArea.row[0] = length / 3;
							prefSet.prefArea.row[1] = 2 * (length / 3);
						}
						break;
					case "angel":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = 2;
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "demon":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = 2;
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "trunk":
						prefSet.prefArea.row[0] = 3;
						prefSet.prefArea.row[1] = 3;
						prefSet.fillRow = true;
						prefSet.placeCount = 1;
						break;
					case "input":
						prefSet.prefArea.row[0] = length - 2;
						prefSet.prefArea.row[1] = length - 2;
						prefSet.prefArea.col[0] = width - 2;
						prefSet.prefArea.col[1] = width - 2;
						if (spriteAssgnmt.get(assignment).sprites.get(0).name.equals("crossing")) {
							prefSet.fillRow = true;
							prefSet.prefArea.row[0] = 1;
							prefSet.prefArea.row[1] = 1;
							prefSet.placeCount = 1;
							spriteAssgnmt.get(assignment).priority = HIGH_PRIO;
						}
						break;
					case "output":
						prefSet.prefArea.row[0] = length - 2;
						prefSet.prefArea.row[1] = length - 2;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						break;
					case "hole":
						if (boxcount > 0) {
							prefSet.placeCount = random.nextInt(3) + boxcount;
						} else {
							prefSet.placeCount = random.nextInt(10) + 5;
						}
						break;

					case "box":
						if (tmpSprite.type.equals("Passive")) {
							if (length > 7) {
								prefSet.prefArea.row[0] = 4;
								prefSet.prefArea.row[1] = length - 4;
							}
							prefSet.placeCount = random.nextInt((width) - 4) + 4;
							boxcount = prefSet.placeCount;
						}
						if (tmpSprite.type.equals("Immovable") && spriteAssgnmt.get(assignment).key == 'e') {
							prefSet.placeCount = random.nextInt(25) + 10;
						} else if (spriteAssgnmt.get(assignment).getType().equals("Immovable")) {
							prefSet.placeCount = random.nextInt(width * length / 6) + (width * length) / 6;
						}
						break;
					case "wall":
						if (wallCount > NO_WALLS) {
							minWalls = wallCount - 5;
							prefSet.placeCount = random.nextInt(wallCount - minWalls) + minWalls;
						} else {
							prefSet.placeCount = NO_WALLS;
						}
						if (racingGame) {
							prefSet.tabooCol = 1;
						}
						if (biggerField) {
							prefSet.placeCount = random.nextInt(15) + 20;
						}
						break;
					case "water":
						if (!neutralElem.contains("water")) {
							prefSet.placeCount = random.nextInt(5) + 4;

							prefSet.fillRandom = true;
						}
						break;
					case "cocoon":
						prefSet.placeCount = random.nextInt(6) + 8;
						break;
					case "slowChicken":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = 2;
						prefSet.placeCount = random.nextInt(2) + 1;
						wallCount = NO_WALLS;
						break;
					case "fastChicken":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = 2;
						prefSet.placeCount = random.nextInt(2) + 1;
						wallCount = NO_WALLS;
						break;
					case "george":
						prefSet.placeCount = 1;

						break;
					case "quiet":
						prefSet.placeCount = random.nextInt(5) + 5;
						break;

					case "lcup":
						prefSet.placeCount = random.nextInt(2);
						break;
					case "lcdown":
						prefSet.placeCount = random.nextInt(2);
						break;
					case "lcleft":
						prefSet.placeCount = random.nextInt(2);
						break;
					case "lcright":
						prefSet.placeCount = random.nextInt(2);
						break;
					case "bomb":
						if (neutralElem.get(0).equals("water"))
							prefSet.placeCount = random.nextInt(11);
						else
							prefSet.placeCount = 0;
						break;
					case "spider":
						prefSet.placeCount = random.nextInt(3) + 1;
						break;
					case "scorpion":
						prefSet.placeCount = random.nextInt(3) + 1;
						break;
					case "bat":
						prefSet.placeCount = random.nextInt(3) + 1;
						break;
					case "butterfly":
						prefSet.placeCount = random.nextInt(2) + 1;
						if (biggerField) {
							prefSet.placeCount = random.nextInt(5) + 5;
						}
						break;
					case "crab":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "exitdoor":
						prefSet.placeCount = 1;
						break;
					case "wallBreak":
						prefSet.placeCount = random.nextInt(length * width / 10) + length * width / 10;
						break;
					case "goal":
						prefSet.placeCount = 1;
						switch (avatarGoalPosition) {
						case 1:
							prefSet.prefArea.row[0] = 1;
							prefSet.prefArea.row[1] = length / 2 - 1;
							prefSet.prefArea.col[0] = 1;
							prefSet.prefArea.col[1] = width / 2 - 1;
							break;
						case 2:
							prefSet.prefArea.row[0] = length / 2 - 1;
							prefSet.prefArea.row[1] = length - 2;
							prefSet.prefArea.col[0] = 1;
							prefSet.prefArea.col[1] = width / 2 - 1;
							break;
						case 3:
							prefSet.prefArea.row[0] = 1;
							prefSet.prefArea.row[1] = length / 2 - 1;
							prefSet.prefArea.col[0] = width / 2 - 1;
							prefSet.prefArea.col[1] = width - 2;
							break;
						case 4:
							prefSet.prefArea.row[0] = length / 2 - 1;
							prefSet.prefArea.row[1] = length - 2;
							prefSet.prefArea.col[0] = width / 2 - 1;
							prefSet.prefArea.col[1] = width - 2;
							break;

						default:
							break;
						}
						if (racingGame) {
							prefSet.prefArea.col[0] = width - 2;
							prefSet.prefArea.col[1] = width - 2;
							prefSet.fillCol = true;
						}
						if (tempAlly != 0) {
							prefSet.placeCount = 4;
							prefSet.prefArea.row[0] = 1;
							prefSet.prefArea.row[1] = 4;
							prefSet.prefArea.col[0] = width - 2;
							prefSet.prefArea.col[1] = width - 2;
						}
						break;
					case "avatar":
						switch (avatarGoalPosition) {
						case 1:
							prefSet.prefArea.row[0] = length / 2 - 1;
							prefSet.prefArea.row[1] = length - 2;
							prefSet.prefArea.col[0] = width / 2 - 1;
							prefSet.prefArea.col[1] = width - 2;
							break;
						case 2:
							prefSet.prefArea.row[0] = 1;
							prefSet.prefArea.row[1] = length / 2 - 1;
							prefSet.prefArea.col[0] = width / 2 - 1;
							prefSet.prefArea.col[1] = width - 2;
							break;
						case 3:
							prefSet.prefArea.row[0] = length / 2 - 1;
							prefSet.prefArea.row[1] = length - 2;
							prefSet.prefArea.col[0] = 1;
							prefSet.prefArea.col[1] = width / 2 - 1;
							break;
						case 4:
							prefSet.prefArea.row[0] = 1;
							prefSet.prefArea.row[1] = length / 2 - 1;
							prefSet.prefArea.col[0] = 1;
							prefSet.prefArea.col[1] = width / 2 - 1;
							break;
						default:
							break;
						}
						break;

					case "randomCamel":
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						prefSet.placeCount = random.nextInt(maxEnemy);
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "fastR":
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						prefSet.placeCount = random.nextInt(maxEnemy);
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "mediumR":
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						prefSet.placeCount = random.nextInt(maxEnemy);
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "slowR":
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						prefSet.placeCount = random.nextInt(maxEnemy);
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "fastL":
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						prefSet.placeCount = random.nextInt(maxEnemy);
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "mediumL":
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						prefSet.placeCount = random.nextInt(maxEnemy);
						maxEnemy -= prefSet.placeCount;
						minEnemy -= prefSet.placeCount;
						break;
					case "slowL":
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = 1;
						prefSet.placeCount = maxEnemy;
						maxEnemy -= random.nextInt(maxEnemy);
						break;
					case "inc1":
						prefSet.placeCount = 1;
						break;
					case "inc2":
						prefSet.placeCount = 1;
						break;
					case "inc3":
						prefSet.placeCount = 1;
						break;
					case "inc4":
						prefSet.placeCount = 1;
						break;
					case "inc5":
						prefSet.placeCount = 1;
						break;
					case "inc6":
						prefSet.placeCount = 1;
						break;
					case "chef":
						prefSet.placeCount = random.nextInt(2) + 2;
						break;
					case "portalBase":
						prefSet.prefArea.row[0] = length - 1;
						prefSet.prefArea.row[1] = length - 1;
						prefSet.prefArea.col[0] = 0;
						prefSet.prefArea.col[1] = 0;
						break;
					case "troposphere":
						if (!spriteAssgnmt.get(assignment).SpriteNames.contains("portalBase")) {
							prefSet.prefArea.row[0] = length - 3;
							prefSet.prefArea.row[1] = length - 1;
							prefSet.fillRow = true;
							prefSet.placeCount = 3;
						}
						break;
					case "stratosphere":
						prefSet.placeCount = 1;
						// prefSet.prefArea.row[0] = 5;
						// prefSet.prefArea.row[1] = length - 3;
						// prefSet.fillRow = true;
						break;
					case "thermosphere":
						if (!spriteAssgnmt.get(assignment).SpriteNames.contains("satellite")) {
							prefSet.prefArea.row[0] = 0;
							prefSet.prefArea.row[1] = 4;
							prefSet.fillRow = true;
							prefSet.placeCount = 5;
							prefSet.overwrite = false;
						}
						break;
					case "satellite":
						prefSet.prefArea.row[0] = 0;
						prefSet.prefArea.row[1] = 4;
						prefSet.placeCount = random.nextInt(5) + 5;
						break;
					case "leftCloud":
						prefSet.prefArea.row[0] = 5;
						prefSet.prefArea.row[1] = length - 3;
						prefSet.placeCount = random.nextInt(10) + 10;
						break;
					case "fastLeftCloud":
						prefSet.prefArea.row[0] = 5;
						prefSet.prefArea.row[1] = length - 3;
						prefSet.placeCount = random.nextInt(10) + 10;
						break;
					case "rightCloud":
						prefSet.prefArea.row[0] = 5;
						prefSet.prefArea.row[1] = length - 3;
						prefSet.placeCount = random.nextInt(10) + 10;
						break;
					case "fastRightCloud":
						prefSet.prefArea.row[0] = 5;
						prefSet.prefArea.row[1] = length - 3;
						prefSet.placeCount = random.nextInt(10) + 10;
						break;
					case "portalAmmo":
						prefSet.prefArea.row[0] = 5;
						prefSet.prefArea.row[1] = length - 3;
						if (!hasWalls) {
							prefSet.prefArea.col[0] = width - 1;
							prefSet.prefArea.col[1] = width - 1;
						}
						prefSet.placeCount = 1;
						break;

					case "bluebox":
						prefSet.placeCount = random.nextInt(3) + 2;
						break;
					case "redbox":
						prefSet.placeCount = random.nextInt(3) + 2;
						break;
					case "greenbox":
						prefSet.placeCount = random.nextInt(3) + 2;
						break;

					case "normalSwitch":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "blueSwitch":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "redSwitch":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "greenSwitch":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "exit":
						prefSet.placeCount = 1;
						prefSet.surroundWalls = random.nextInt(4) + 1;
						if (spriteAssgnmt.get(assignment).sprites.get(0).name.equals("ground")) {
							prefSet.fillGap = 'l';
						} else if (spriteAssgnmt.get(assignment).sprites.get(0).name.contains("floor")) {
							prefSet.fillGap = 'm';
						} else {
							prefSet.fillGap = 'd';
						}
						break;
					case "blueblock":
						prefSet.placeCount = 1;
						break;
					case "redblock":
						prefSet.placeCount = 1;
						break;
					case "greenblock":
						prefSet.placeCount = 1;
						break;
					case "boilingwater":
						prefSet.placeCount = 1;
						break;
					case "tomato":
						prefSet.placeCount = 1;
						break;
					case "rawpasta":
						prefSet.placeCount = 1;
						break;
					case "tuna":
						prefSet.placeCount = 1;
						break;
					case "greenthug":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "yellowthug":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "redthug":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;
					case "boss":
						prefSet.placeCount = random.nextInt(2) + 1;
						break;

					case "gem":
						break;
					case "gold":
						break;
					case "monster":
						break;
					case "entrance":
						break;
					case "slowRcar":
						prefSet.prefArea.row[0] = 2 * length / 7;
						prefSet.prefArea.row[1] = 5 * length / 7 - 1;
						prefSet.placeCount = length / 5 + random.nextInt(length / 4);
						break;
					case "fastDcar":
						prefSet.prefArea.col[0] = 2 * width / 7;
						prefSet.prefArea.col[1] = 5 * width / 7 - 1;
						prefSet.placeCount = width / 5 + random.nextInt(width / 4);
						break;
					case "landSand":
						prefSet.placeCount = (width * length) / 10 + random.nextInt((width * length) / 7);
						break;
					case "treasure":
						prefSet.placeCount = 1 + random.nextInt(5);
						break;
					case "sharkhole":
						prefSet.placeCount = random.nextInt(4);
						break;
					case "whalehole":
						if (tempEnemy > 0) {
							prefSet.prefArea.col[0] = 0;
							prefSet.prefArea.col[1] = width / 3;
							prefSet.placeCount = length / 4 + random.nextInt(tempEnemy);
						}
						break;
					case "piranhahole":
						prefSet.prefArea.col[0] = 2 * width / 3;
						prefSet.prefArea.col[1] = width - 1;
						prefSet.placeCount = random.nextInt(length);
						tempEnemy = length - prefSet.placeCount;
						break;
					case "city":
						prefSet.prefArea.row[0] = length - 2;
						prefSet.prefArea.row[1] = length - 2;
						prefSet.placeCount = 1 + random.nextInt(5);
						break;
					case "marsh":
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width / 4;
						prefSet.placeCount = 3 * length;
						break;
					case "fastHell":
						if (tempEnemy > 0) {
							prefSet.prefArea.col[0] = 3 * width / 4;
							prefSet.prefArea.col[1] = width - 2;
							prefSet.placeCount = random.nextInt(length);
							tempEnemy = length - prefSet.placeCount / 2;
						}
						break;
					case "slowHell":
						if (tempEnemy > 0) {
							prefSet.prefArea.col[0] = 3 * width / 4;
							prefSet.prefArea.col[1] = width - 2;
							prefSet.placeCount = random.nextInt(tempEnemy);
						}
						break;
					case "camelA":
						prefSet.prefArea.row[0] = 1;
						prefSet.prefArea.row[1] = 1;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width / 3;
						prefSet.placeCount = 1;
						break;
					case "camelB":
						prefSet.prefArea.row[0] = 2;
						prefSet.prefArea.row[1] = 2;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width / 3;
						prefSet.placeCount = 1;
						break;
					case "camelC":
						prefSet.prefArea.row[0] = 3;
						prefSet.prefArea.row[1] = 3;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width / 3;
						prefSet.placeCount = 1;
						break;
					case "camelD":
						prefSet.prefArea.row[0] = 4;
						prefSet.prefArea.row[1] = 4;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width / 3;
						prefSet.placeCount = 1;
						tempAlly = 1;
						break;
					case "pitA":
						prefSet.prefArea.row[0] = length - 5;
						prefSet.prefArea.row[1] = length - 5;
						prefSet.prefArea.col[0] = width / 2;
						prefSet.prefArea.col[1] = width / 2;
						prefSet.placeCount = 1;
						break;
					case "pitB":
						prefSet.prefArea.row[0] = length - 4;
						prefSet.prefArea.row[1] = length - 4;
						prefSet.prefArea.col[0] = width / 2;
						prefSet.prefArea.col[1] = width / 2;
						prefSet.placeCount = 1;
						break;
					case "pitC":
						prefSet.prefArea.row[0] = length - 3;
						prefSet.prefArea.row[1] = length - 3;
						prefSet.prefArea.col[0] = width / 2;
						prefSet.prefArea.col[1] = width / 2;
						prefSet.placeCount = 1;
						break;
					case "pitD":
						prefSet.prefArea.row[0] = length - 2;
						prefSet.prefArea.row[1] = length - 2;
						prefSet.prefArea.col[0] = width / 2;
						prefSet.prefArea.col[1] = width / 2;
						prefSet.placeCount = 1;
						break;
					case "bossDragon":
						tempEnemy = 0;
						prefSet.prefArea.col[0] = width / 2 + 1;
						prefSet.prefArea.col[1] = 2 * width / 3;
						prefSet.placeCount = random.nextInt(2);
						tempEnemy += prefSet.placeCount;
						break;
					case "bossWater":
						prefSet.prefArea.col[0] = width / 2 + 1;
						prefSet.prefArea.col[1] = 2 * width / 3;
						prefSet.placeCount = random.nextInt(2);
						tempEnemy += prefSet.placeCount;
						break;
					case "bossThunder":
						prefSet.prefArea.col[0] = width / 2 + 1;
						prefSet.prefArea.col[1] = 2 * width / 3;
						if (tempEnemy == 0)
							prefSet.placeCount = 1;
						else
							prefSet.placeCount = random.nextInt(2);
						break;
					case "pokemon0":
						tempAlly = 9;
						prefSet.prefArea.col[0] = width / 3;
						prefSet.prefArea.col[1] = width / 2;
						prefSet.placeCount = random.nextInt(5);
						tempAlly -= prefSet.placeCount;
						break;
					case "pokemon1":
						prefSet.prefArea.col[0] = width / 3;
						prefSet.prefArea.col[1] = width / 2;
						if (tempAlly > 5)
							prefSet.placeCount = random.nextInt(5);
						else
							prefSet.placeCount = random.nextInt(4);
						tempAlly -= prefSet.placeCount;
						break;
					case "pokemon2":
						prefSet.prefArea.col[0] = width / 3;
						prefSet.prefArea.col[1] = width / 2;
						prefSet.placeCount = 1 + random.nextInt(tempAlly);
						break;
					case "crossing":
						if (spriteAssgnmt.get(assignment).sprites.size() == 1) {
							prefSet.fillRow = true;
							prefSet.placeCount = length / 2 - 1;
							wallCount = NO_WALLS;
							prefSet.mustRow = length - 2;
							spriteAssgnmt.get(assignment).priority = MIDDLE_PRIO;
						}
						break;
					case "tree":
						if (spriteAssgnmt.get(assignment).sprites.get(0).name.equals("street")
								|| spriteAssgnmt.get(assignment).sprites.get(1).name.equals("street")) {
							prefSet.placeCount = length / 2 - 1;
							prefSet.prefArea.col[0] = 0;
							prefSet.prefArea.col[1] = 0;
							prefSet.prefArea.row[0] = 1;
							prefSet.evenUnevenRow = 1;
							prefSet.mustCol = width - 1;
							if (length % 2 == 0) {
								prefSet.placeCount = length / 2 - 1;
							} else {
								prefSet.placeCount = length / 2;
							}
						} else {
							prefSet.fillRow = false;
							prefSet.placeCount = random.nextInt(10) + 10;
							prefSet.onlyPlaceOnKey = '+';
						}
						break;
					case "slowRtruck":
						prefSet.placeCount = random.nextInt(3) + 4;
						prefSet.evenUnevenRow = 0;
						break;
					case "fastRtruck":
						prefSet.placeCount = random.nextInt(3) + 4;
						prefSet.evenUnevenRow = 0;
						break;
					case "slowLtruck":
						prefSet.placeCount = random.nextInt(3) + 4;
						prefSet.evenUnevenRow = 1;
						break;
					case "fastLtruck":
						prefSet.placeCount = random.nextInt(3) + 5;
						prefSet.evenUnevenRow = 1;
						break;
					case "moving":
						if (spriteAssgnmt.get(assignment).sprites.get(2).name.equals("start")) {
							prefSet.prefArea.row[0] = length - 2;
							prefSet.prefArea.row[1] = length - 2;
							prefSet.placeCount = 1;
							wallCount = NO_WALLS;
							prefSet.onlyPlaceOnKey = '+';
						}
						break;
					case "treePortal":
						prefSet.prefArea.col[0] = 0;
						prefSet.prefArea.col[1] = 0;
						prefSet.mustCol = width - 1;
						prefSet.prefArea.row[0] = 0;
						prefSet.prefArea.row[1] = 0;
						prefSet.placeCount = 1;
						spriteAssgnmt.get(assignment).priority = HIGH_PRIO;
						break;
					case "fuelPortal":
						prefSet.placeCount = 1;
						prefSet.prefArea.row[0] = 0;
						prefSet.prefArea.row[1] = 0;
						break;
					case "slowPortal":
						prefSet.prefArea.row[0] = 0;
						prefSet.prefArea.row[1] = 1;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width - 1;
						prefSet.placeCount = random.nextInt(width / 2 - 3) + 3;
						break;
					case "fastPortal":
						prefSet.prefArea.row[0] = 0;
						prefSet.prefArea.row[1] = 1;
						prefSet.prefArea.col[0] = 1;
						prefSet.prefArea.col[1] = width - 1;
						prefSet.placeCount = random.nextInt(width / 2 - 3) + 3;
						break;
					case "street":
						if (spriteAssgnmt.get(assignment).sprites.size() == 1) {
							prefSet.prefArea.col[0] = 0;
							prefSet.prefArea.col[1] = 0;
							prefSet.prefArea.row[0] = 1;
							prefSet.mustCol = width - 1;
							prefSet.evenUnevenRow = 0;
							prefSet.placeCount = length / 2;
							// spriteAssgnmt.get(assignment).priority =MIDDLE_PRIO;
						}
						break;
					case "damaged":
						prefSet.placeCount = 1;
						break;
					case "cliff":
						prefSet.placeCount = random.nextInt(5) + 2;
						break;
					default:
						break;
					}
					if (hasWalls) {
						for (int avatar = 0; avatar < game.getAvatar().size(); avatar++) {
							String avatarString = game.getAvatar().get(avatar).name;

							for (int idx = 0; idx < game.getInteraction(tmpSprite.name, avatarString).size(); idx++) {
								if (game.getInteraction(tmpSprite.name, avatarString).get(idx).type
										.equals("BounceForward")
										|| game.getInteraction(tmpSprite.name, avatarString).get(idx).type
												.equals("TransformTo")) {
									if (prefSet.prefArea.row[0] == 1)
										prefSet.prefArea.row[0] = 2;

									if (prefSet.prefArea.row[1] == length - 2)
										prefSet.prefArea.row[1] = length - 3;

									if (prefSet.prefArea.col[0] == 1)
										prefSet.prefArea.col[0] = 2;

									if (prefSet.prefArea.col[1] == width - 2)
										prefSet.prefArea.col[1] = width - 3;
								}
							}
						}
					}
				}

			}
			if (spriteAssgnmt.get(assignment).isAvatar)
				prefSet.placeCount = 1;
			spriteAssgnmt.get(assignment).setPrefSet(prefSet);
		}
	}


	private String findBestString() {

		int[] scores = new int[testStrings.size()];
		int[] wins = new int[testStrings.size()];
		int[] rsltSIzes = new int[testStrings.size()];
		for (int index = 0; index < testStrings.size(); index++) {
			scores[index] = 0;
			wins[index] = 0;
			rsltSIzes[index] = 0;

			TestString tmpTst = testStrings.get(index);

			for (int rslt = 0; rslt < testStrings.get(index).tstRslt.size(); rslt++) {
				rsltSIzes[index] += tmpTst.tstRslt.get(rslt).bestSol.size();
				scores[index] += tmpTst.tstRslt.get(rslt).bestState.getGameScore();
				if (tmpTst.tstRslt.get(rslt).bestState.getGameWinner().name().equals("PLAYER_WINS")) {
					wins[index]++;
				}

			}
			System.out.println("level " + index + ": wins:" + wins[index] + " score:" + scores[index] + " resultSIze"
					+ rsltSIzes[index]);
		}
		int best = 0;

		for (int index = 1; index < testStrings.size(); index++) {
			if (wins[index] > wins[best]) {
				best = index;
			} else if (wins[index] == wins[best]) {
				if (scores[index] > scores[best]) {
					best = index;
				}
			}
		}
		System.out.println("level " + best + " is the best generated level");
		return testStrings.get(best).result;
	}

	private void fillLevel() {
		for (int prio = 2; prio >= 0; prio--) {
			for (int assgnmnt = 0; assgnmnt < spriteAssgnmt.size(); assgnmnt++) {
				if (spriteAssgnmt.get(assgnmnt).priority == prio) {
					System.out.println(
							spriteAssgnmt.get(assgnmnt).key + " " + spriteAssgnmt.get(assgnmnt).prefSet.placeCount);
					for (int i = 0; i < spriteAssgnmt.get(assgnmnt).prefSet.placeCount; i++) {

						placeSprite(spriteAssgnmt.get(assgnmnt));}
				}
			}
		}
	}

	@Override
	  public HashMap<Character, ArrayList<String>> getLevelMapping() {
	    return game.getLevelMapping();
	  }
	@Override
	public String generateLevel(GameDescription gameDescr, ElapsedCpuTimer elapsedTimer) {
		game = gameDescr;
		sprites = game.getAllSpriteData();
		random = new Random();
		biggerField = false;
		hasWalls = false;
		boolean hasNeutral = false;
		wallKey = 'w';
		minSize = 10;
		walls = NO_WALLS;
		String result = "";
		wallCount = MEDIUM_WALLS;
		if (game.getInteraction("camel", "goal").size() > 0) {
			racingGame = true;
		}
		if (game.getInteraction("butterfly", "cocoon").size() > 0) {
			biggerField = true;
			minSize = 15;
		}

		if (game.getInteraction("keym", "avatar").size() > 0) {
			if (game.getInteraction("keym", "avatar").get(0).type.equals("AttractGaze")) {
				negativeLevel = true;
				wallCount = NO_WALLS;
			}
		}
		avatarName = game.getAvatar().get(0).name;
		hasNeutral = findNeutralElement();
		if (hasWalls = game.getInteraction(avatarName, "wall").size() > 0) {
			walls = ALL_WALLS;
			if (game.getInteraction("satellite", "wall").size() > 0) {
				satelliteGame = true;
				walls = NO_WALLS;
				wallCount = NO_WALLS;
				hasWalls = false;

			}
		}
		if (game.getInteraction("goal", "avatar").size() > 1 || game.getInteraction("exit", "avatar").size() > 1) {
			avatarGoalPosition = random.nextInt(4) + 1;
		} else {
			avatarGoalPosition = 0;
		}
		parseGame(hasWalls);
		for (int assgn = 0; assgn < spriteAssgnmt.size(); assgn++) {
			spriteAssgnmt.get(assgn).printAssignment();
		}

		for (int tries = 0; tries < numberOfGeneratedLevel; tries++) {
			do {
				initGameField(hasWalls);
				// analyzeSpriteInfo(hasWalls);
				addPrefSetToAssignment(hasWalls);
				fillLevel();
			} while (!lastCheck(hasNeutral));

			printLevel();
			result = getLevelString();

			TestString tmpTstStr = new TestString();
			tmpTstStr.result = result;

			testStrings.add(tmpTstStr);
		}

		if (automatedAgent == null) {
			initAgent(result);
		}
		int testCount = 1;
		for (int test = 0; test < numberOfTests; test++) {
			for (int string = 0; string < numberOfGeneratedLevel; string++) {
				System.out.print("test " + testCount + "/" + numberOfTests * numberOfGeneratedLevel + " :");
				testCount++;
				if (test % 2 == 0) {
					TestResult tmpTstRslt = testGame(testStrings.get(string).result);
					testStrings.get(string).tstRslt.add(tmpTstRslt);
				} else {
					TestResult tmpTstRslt = testGame(testStrings.get(numberOfGeneratedLevel - string - 1).result);
					testStrings.get(numberOfGeneratedLevel - string - 1).tstRslt.add(tmpTstRslt);
				}

			}

		}
		result = findBestString();
		return result;}
}
