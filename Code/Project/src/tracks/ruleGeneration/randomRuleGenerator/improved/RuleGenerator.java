package tracks.ruleGeneration.randomRuleGenerator.improved;

import java.util.ArrayList;
import java.util.Random;
import core.game.GameDescription.SpriteData;
import core.game.SLDescription;
import core.generator.AbstractRuleGenerator;
import tools.ElapsedCpuTimer;

// improved

public class RuleGenerator extends AbstractRuleGenerator {

	private String[] interactions = new String[] { "killSprite", "killAll", "killIfHasMore", "killIfHasLess",
			"killIfFromAbove", "killIfOtherHasMore", "spawnBehind", "stepBack", "spawnIfHasMore", "spawnIfHasLess",
			"cloneSprite", "transformTo", "undoAll", "flipDirection", "transformToRandomChild", "updateSpawnType",
			"removeScore", "addHealthPoints", "addHealthPointsToMax", "reverseDirection", "subtractHealthPoints",
			"increaseSpeedToAll", "decreaseSpeedToAll", "attractGaze", "align", "turnAround", "wrapAround",
			"pullWithIt", "bounceForward", "teleportToExit", "collectResource", "setSpeedForAll", "undoAll",
			"reverseDirection", "changeResource" };

	private ArrayList<String> usefulSprites;
	private String avatar;
	private Random random;
	private int FIXED = 5;

	public RuleGenerator(SLDescription sl, ElapsedCpuTimer time) {
		this.usefulSprites = new ArrayList<String>();
		this.random = new Random();
		String[][] currentLevel = sl.getCurrentLevel();

		for (int y = 0; y < currentLevel.length; y++) {
			for (int x = 0; x < currentLevel[y].length; x++) {
				String[] parts = currentLevel[y][x].split(",");
				for (int i = 0; i < parts.length; i++) {
					if (parts[i].trim().length() > 0) {
						// Add the sprite if it doesn't exisit
						if (!usefulSprites.contains(parts[i].trim())) {
							usefulSprites.add(parts[i].trim());
						}
					}
				}
			}
		}
		this.usefulSprites.add("EOS");
		this.avatar = this.getAvatar(sl);
	}


	private String[] getArray(ArrayList<String> list) {
		String[] array = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;}


	private String getAvatar(SLDescription sl) {
		SpriteData[] sprites = sl.getGameSprites();
		for (int i = 0; i < this.usefulSprites.size(); i++) {
			SpriteData s = this.getSpriteData(sprites, this.usefulSprites.get(i));
			if (s != null && s.isAvatar) {
				return this.usefulSprites.get(i);
			}
		}
		return "";
	}

	private SpriteData getSpriteData(SpriteData[] sprites, String name) {
		for (int i = 0; i < sprites.length; i++) {
			if (sprites[i].name.equalsIgnoreCase(name)) {
				return sprites[i];
			}
		}

		return null;
	}

	@Override
	public String[][] generateRules(SLDescription sl, ElapsedCpuTimer time) {
		ArrayList<String> interaction = new ArrayList<String>();
		ArrayList<String> termination = new ArrayList<String>();

		// number of interactions in the game based on the number of sprites
		int numberOfInteractions = (int) (this.usefulSprites.size() * (0.5 + 0.5 * this.random.nextDouble()));
		if (this.FIXED > 0) {
			numberOfInteractions = this.FIXED;
		}
		for (int i = 0; i < numberOfInteractions; i++) {
			// get two random indeces for the two sprites in the interaction
			int i1 = this.random.nextInt(this.usefulSprites.size());
			int i2 = (i1 + 1 + this.random.nextInt(this.usefulSprites.size() - 1)) % this.usefulSprites.size();
			// add score change parameter for interactions
			String scoreChange = "";
			if(this.random.nextBoolean()){
				scoreChange += "scoreChange=" + (this.random.nextInt(5) - 2);
			}
			// add the new random interaction that doesn't produce errors
			interaction.add(this.usefulSprites.get(i1) + " " + this.usefulSprites.get(i2) + " > " +
					this.interactions[this.random.nextInt(this.interactions.length)] + " " + scoreChange);
			sl.testRules(getArray(interaction), getArray(termination));
			while(sl.getErrors().size() > 0){
				interaction.remove(i);
				interaction.add(this.usefulSprites.get(i1) + " " + this.usefulSprites.get(i2) + " > " +
						this.interactions[this.random.nextInt(this.interactions.length)] + " " + scoreChange);
				sl.testRules(getArray(interaction), getArray(termination));
			}
		}
		
		// Add a winning termination condition
		if (this.random.nextBoolean()) {
		    termination.add("Timeout limit=" + (800 + this.random.nextInt(500)) + " win=True");
		} else {
		    String chosen = this.usefulSprites.get(this.random.nextInt(this.usefulSprites.size()));
		    sl.testRules(getArray(interaction), getArray(termination));
		    while(sl.getErrors().size() > 0){
			termination.remove(termination.size() - 1);
			termination.add("SpriteCounter stype=" + chosen + " limit=0 win=True");
			sl.testRules(getArray(interaction), getArray(termination));
		    }
		}
		// Add a losing termination condition
		termination.add("SpriteCounter stype=" + this.avatar + " limit=0 win=False");

		return new String[][] { getArray(interaction), getArray(termination) };
	}


}