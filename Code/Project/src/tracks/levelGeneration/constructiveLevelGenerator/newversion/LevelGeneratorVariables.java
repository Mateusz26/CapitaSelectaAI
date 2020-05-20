package tracks.levelGeneration.constructiveLevelGenerator.newversion;

import java.util.ArrayList;
import core.game.GameDescription.SpriteData;
import core.game.StateObservation;
import ontology.Types;

// improved 

public class LevelGeneratorVariables {
	public static class TestString {
		ArrayList<TestResult> tstRslt = new ArrayList<TestResult>();
		String result;
	}
	public static class TestResult {
		StateObservation bestState;
		ArrayList<Types.ACTIONS> bestSol;}

	public static class Position {
		int row;
		int col;

		public Position(int row, int col) {
			this.row = row;
			this.col = col;}

		public Position() {
			this.row = -1;
			this.col = -1;}

	}

	public static class preferredArea {
		int[] row = { -1, -1 };
		int[] col = { -1, -1 };	}

	public static class preferredSettings {
		preferredArea prefArea = new preferredArea();
		boolean fillRow = false;
		boolean fillCol = false;
		boolean fillRandom = false;
		boolean overwrite = false;
		int surroundWalls = 0; 
		char fillGap = '?';
		int tabooRow = -1;
		int tabooCol = -1;
		int mustRow = -1;
		int mustCol = -1;
		int evenUnevenRow = -1;
		int evenUnevenCol = -1;

		boolean noWallsInRow = false;
		int placeCount = 0;
		char neededNeighbourKey = '!';
		char onlyPlaceOnKey = '?';}


	public static class SpriteAssignment {
		ArrayList<SpriteData> sprites = new ArrayList<SpriteData>();
		ArrayList<String> SpriteNames = new ArrayList<String>();
		String type = "";
		int limit = -1;
		char key;
		boolean isAvatar = false;
		int priority = 0;
		preferredSettings prefSet = new preferredSettings();

		public void setPrefSet(preferredSettings prefSet) {
			this.prefSet = prefSet;}

		public void addSprite(SpriteData sprite) {
			this.sprites.add(sprite);}

		public void setKey(char key) {
			this.key = key;}

		public ArrayList<SpriteData> getSprites() {
			return this.sprites;}

		public char getKey() {
			return this.key;}

		public ArrayList<String> getNames() {
			return this.SpriteNames;}

		public void addName(String SpriteName) {
			this.SpriteNames.add(SpriteName);}

		public void printAssignment() {
			System.out.println("Sprites: " + SpriteNames + " Key: " + key + " type: " + type);}
		public void setType(String type) {
			this.type = type;}
		public String getType() {
			return this.type;}}
}
