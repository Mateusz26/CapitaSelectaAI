package tracks.ruleGeneration.geneticRuleGenerator;
import java.util.*;

import core.game.SLDescription;
import core.game.StateObservation;
import core.game.Event;
import core.game.GameDescription.SpriteData;
import core.game.Observation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import java.util.Arrays;


// improved version 


public class Chromosome implements Comparable<Chromosome>{

	private ArrayList<Double> fitness;
	private double constrainFitness;
	private int errorCount;
	private String[][] ruleset;
	private SLDescription sl;
	private int FEASIBILITY_STEP_LIMIT = 40;
	private int doNothingLength;
	StateObservation doNothingState;
	StateObservation bestState;
	ArrayList<Types.ACTIONS> bestSol;

	public Chromosome(String[][] ruleset, SLDescription sl) {
		this.ruleset = ruleset;
		this.sl = sl;
		this.fitness = new ArrayList<Double>();
		fitness.add(0.0);
		fitness.add(0.0);
		this.badFrames = 0;
	}

	public void mutate() {
		int mutationCount = SharedData.random.nextInt(SharedData.MUTATION_AMOUNT) + 1;
		for(int i = 0; i < mutationCount; i++) {
			int mutateR = SharedData.random.nextInt(2);
			if(mutateR == 0){
				mutateInteraction();
			} else {
				mutateTermination();
			}
		}
	}
	
	public void mutateInteraction() {
		ArrayList<String> interactionSet = new ArrayList<>( Arrays.asList(ruleset[0]));
		double mutationType = SharedData.random.nextDouble();
		if(mutationType < SharedData.INSERTION_PROB) {
			double roll = SharedData.random.nextDouble();
			if(roll < SharedData.INSERT_PARAM_PROB) {
				int point = SharedData.random.nextInt(interactionSet.size());
				String addToMe = interactionSet.get(point);
				String nParam = SharedData.interactionParams[SharedData.random.nextInt(SharedData.interactionParams.length)];
				nParam += "=";
				if(nParam.equals("scoreChange=") || nParam.equals("limit=") || nParam.equals("value=") || nParam.equals("geq=")
						|| nParam.equals("leq=")) {
					int val = SharedData.random.nextInt(SharedData.NUMERICAL_VALUE_PARAM) - 1000;
					nParam += val;
				} else {
					String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
					nParam += nSprite;
				}
				addToMe += " " + nParam;
				ruleset[0][point] = addToMe;
			}
			else {
				String nInteraction = SharedData.interactions[SharedData.random.nextInt(SharedData.interactions.length)];
				int i1 = SharedData.random.nextInt(SharedData.usefulSprites.size());
			    int i2 = (i1 + 1 + SharedData.random.nextInt(SharedData.usefulSprites.size() - 1)) % SharedData.usefulSprites.size();
			    
			    String newInteraction = SharedData.usefulSprites.get(i1) + " " + SharedData.usefulSprites.get(i2) + " > " + nInteraction;
			    roll = SharedData.random.nextDouble();
			     if(roll < SharedData.INSERT_PARAM_PROB) {
			    	String nParam = SharedData.interactionParams[SharedData.random.nextInt(SharedData.interactionParams.length)];
					nParam += "=";
					if(nParam.equals("scoreChange=") || nParam.equals("limit=") || nParam.equals("value=") || nParam.equals("geq=")
							|| nParam.equals("leq=")) {
						int val = SharedData.random.nextInt(SharedData.NUMERICAL_VALUE_PARAM) - 1000;
						nParam += val;
					} else {
						String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
						nParam += nSprite;
					}
					newInteraction += " " + nParam;
			    }
			    interactionSet.add(newInteraction);
			    interactionSet.removeIf(s -> s == null);
				interactionSet = (ArrayList<String>) interactionSet.stream().distinct().collect(Collectors.toList());
				ruleset[0] = new String[interactionSet.size()];
				ruleset[0] = interactionSet.toArray(ruleset[0]);
			}
		} 
		else if(mutationType < SharedData.DELETION_PROB + SharedData.INSERTION_PROB) {
			double roll = SharedData.random.nextDouble();
			if(roll < SharedData.DELETE_PARAM_PROB) {
				int point = SharedData.random.nextInt(interactionSet.size());
				String deleteFromMe = interactionSet.get(point);
				String[] splitDeleteFromMe = deleteFromMe.split("\\s+");
				ArrayList<String> params = new ArrayList<String>();
				for(String param : splitDeleteFromMe) {
					if(param.contains("=")){
						params.add(param);
					}
				}
				if(params.size() == 0) {
					
				} 
				else if(params.size() == 1) {
					String fixedRule = "";
					for(String part : splitDeleteFromMe) {
						if(!part.contains("=")) {
							fixedRule += part + " ";
						}
					}
					interactionSet.set(point, fixedRule);
				}
				else {
					int rule = SharedData.random.nextInt(params.size());
					String fixedRule = "";
					for(String part : splitDeleteFromMe) {
						if(!part.equals(params.get(rule))) {
							fixedRule += part + " ";
						}
					}
					interactionSet.set(point, fixedRule);
				}
			    interactionSet.removeIf(s -> s == null);
				interactionSet = (ArrayList<String>) interactionSet.stream().distinct().collect(Collectors.toList());
				ruleset[0] = new String[interactionSet.size()];
				ruleset[0] = interactionSet.toArray(ruleset[0]);
			}
			else{
				int point = SharedData.random.nextInt(interactionSet.size());
				if (interactionSet.size() > 1) {
					interactionSet.remove(point);
				}
			    interactionSet.removeIf(s -> s == null);
				interactionSet = (ArrayList<String>) interactionSet.stream().distinct().collect(Collectors.toList());
				ruleset[0] = new String[interactionSet.size()];
				ruleset[0] = interactionSet.toArray(ruleset[0]);
			}
		} 
		else if (mutationType < SharedData.MODIFY_RULE_PROB + SharedData.DELETION_PROB + SharedData.INSERTION_PROB) {
			int point = SharedData.random.nextInt(interactionSet.size());
			double roll = SharedData.random.nextDouble();
			if(roll < SharedData.MODIFY_PARAM_PROB) {
				String modifyFromMe = interactionSet.get(point);
				String[] splitModifyFromMe = modifyFromMe.split("\\s+");
				ArrayList<String> ps = new ArrayList<String>();
				for(String param : splitModifyFromMe) {
					if(param.contains("=")){
						ps.add(param);}
				}
				if(ps.size() == 0) {
				} else {
					int rule = SharedData.random.nextInt(ps.size());
					String fixedRule = "";
					for(String part : splitModifyFromMe) {
						if(!part.equals(ps.get(rule))) {
							fixedRule += part + " ";
						} else {
							String nParam = SharedData.interactionParams[SharedData.random.nextInt(SharedData.interactionParams.length)];
							nParam += "=";
							if(nParam.equals("scoreChange=") || nParam.equals("limit=") || nParam.equals("value=") || nParam.equals("geq=")
									|| nParam.equals("leq=")) {
								int val = SharedData.random.nextInt(SharedData.NUMERICAL_VALUE_PARAM) - 1000;
								nParam += val;
							} else {
								String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
								nParam += nSprite;
							}
							
							fixedRule += nParam + " ";
						}
					}
					interactionSet.set(point, fixedRule);
				}
			    interactionSet.removeIf(s -> s == null);
				interactionSet = (ArrayList<String>) interactionSet.stream().distinct().collect(Collectors.toList());
				ruleset[0] = new String[interactionSet.size()];
				ruleset[0] = interactionSet.toArray(ruleset[0]);
			}
			else {
				String newRule = SharedData.interactions[SharedData.random.nextInt(SharedData.interactions.length)];
				String modRule = ruleset[0][point];
				
				String[] splitModRule = modRule.split("\\s+");
				splitModRule[3] = newRule;
				newRule = "";
				for(String part : splitModRule) {
					newRule += part + " ";
				}
				ruleset[0][point] = newRule;
			}
		} 
		else {
			System.err.println("What?! How did we even get here!?");
		}
	}

	public void mutateTermination() {
		ArrayList<String> terminationSet = new ArrayList<>( Arrays.asList(ruleset[1]));
		double mutationType = SharedData.random.nextDouble();
		if(mutationType < SharedData.INSERTION_PROB) {
			double roll = SharedData.random.nextDouble();
			if(roll < SharedData.INSERT_PARAM_PROB) {
				int point = SharedData.random.nextInt(terminationSet.size());
				String addToMe = terminationSet.get(point);
				String nParam = SharedData.terminationParams[SharedData.random.nextInt(SharedData.terminationParams.length)];
				nParam += "=";
				double roll1 = SharedData.random.nextDouble();
				if(roll1 < SharedData.PARAM_NUM_OR_SPRITE_PROB) {
					String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
					nParam += nSprite;
				}
				else {
					int val = SharedData.random.nextInt(SharedData.NUMERICAL_VALUE_PARAM);
					nParam += val;}
				addToMe += " " + nParam;
				ruleset[1][point] = addToMe;
					for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
			else {
				String nTermination = SharedData.terminations[SharedData.random.nextInt(SharedData.terminations.length)];    
				if(roll < SharedData.INSERT_PARAM_PROB) {
					String nParam = SharedData.terminationParams[SharedData.random.nextInt(SharedData.terminationParams.length)];
					nParam += "=";
					String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
					nParam += nSprite;
					nTermination+= " " + nParam;
				}
				nTermination += " win=";
				double roll2 = SharedData.random.nextDouble();
				if(roll2 < SharedData.WIN_PARAM_PROB){
					nTermination += "True";
				} else {
					nTermination += "False";
				}
				if(nTermination.contains("Timeout")) {
					int val = SharedData.random.nextInt(SharedData.TERMINATION_LIMIT_PARAM) + 500;
					nTermination += " limit="+val;
				} else{
					int val = SharedData.random.nextInt(SharedData.TERMINATION_LIMIT_PARAM);
					nTermination += " limit="+val;
				}
			    terminationSet.add(nTermination);
			    terminationSet.removeIf(s -> s == null);
				terminationSet = (ArrayList<String>) terminationSet.stream().distinct().collect(Collectors.toList());
				ruleset[1] = new String[terminationSet.size()];
				ruleset[1] = terminationSet.toArray(ruleset[1]);
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
		} 
		else if(mutationType < SharedData.DELETION_PROB + SharedData.INSERTION_PROB) {
			double roll = SharedData.random.nextDouble();
			if(roll < SharedData.DELETE_PARAM_PROB) {
				int point = SharedData.random.nextInt(terminationSet.size());
				String deleteFromMe = terminationSet.get(point);
				String[] splitDeleteFromMe = deleteFromMe.split("\\s+");
				ArrayList<String> params = new ArrayList<String>();
				for(String param : splitDeleteFromMe) {
					if(param.contains("=") && !param.contains("limit") && !param.contains("win")){
						params.add(param);
					}
				}
				if(params.size() == 0) {
									} 
				else {
					int rule = SharedData.random.nextInt(params.size());
					String fixedRule = "";
					for(String part : splitDeleteFromMe) {
						if(!part.equals(params.get(rule))) {
							fixedRule += part + " ";
						}
					}
					terminationSet.set(point, fixedRule);
				}
				terminationSet.removeIf(s -> s == null);
				terminationSet = (ArrayList<String>) terminationSet.stream().distinct().collect(Collectors.toList());
				ruleset[1] = new String[terminationSet.size()];
				ruleset[1] = terminationSet.toArray(ruleset[1]);
					for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
			else{
				int point = SharedData.random.nextInt(terminationSet.size());
				if (terminationSet.size() > 1) {
					terminationSet.remove(point);
				}
				terminationSet.removeIf(s -> s == null);
				terminationSet = (ArrayList<String>) terminationSet.stream().distinct().collect(Collectors.toList());
				ruleset[1] = new String[terminationSet.size()];
				ruleset[1] = terminationSet.toArray(ruleset[1]);
								for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
		} 
		else if (mutationType < SharedData.MODIFY_RULE_PROB + SharedData.DELETION_PROB + SharedData.INSERTION_PROB) {
			int point = SharedData.random.nextInt(terminationSet.size());
						double roll = SharedData.random.nextDouble();
			if(roll < SharedData.MODIFY_PARAM_PROB) {
				String modifyFromMe = terminationSet.get(point);
				String[] splitModifyFromMe = modifyFromMe.split("\\s+");
				ArrayList<String> ps = new ArrayList<String>();
				for(String param : splitModifyFromMe) {
					if(param.contains("=")){
						ps.add(param);
					}
				}
				if(ps.size() == 0) {
					} else {
					int rule = SharedData.random.nextInt(ps.size());
					String fixedRule = "";
					for(String part : splitModifyFromMe) {
						if(!part.equals(ps.get(rule))) {
							fixedRule += part + " ";
						} 
						else {
							String nParam = ""; 
							if(part.contains("win")) {
								nParam = "win=";
								double roll2 = SharedData.random.nextDouble();
								if(roll2 < SharedData.WIN_PARAM_PROB) {
									nParam += "True";
								} else {
									nParam += "False";
								}
							} else if(part.contains("limit")) {
								nParam = "limit=";
								if(fixedRule.contains("Timeout")) {
									int roll2 = SharedData.random.nextInt(SharedData.TERMINATION_LIMIT_PARAM) + 500;
									nParam += roll2;}
								else{
									int roll2 = SharedData.random.nextInt(SharedData.TERMINATION_LIMIT_PARAM);
									nParam += roll2;}
							} else {
								nParam = SharedData.terminationParams[SharedData.random.nextInt(SharedData.terminationParams.length)] + "=";
								String nSprite = SharedData.usefulSprites.get(SharedData.random.nextInt(SharedData.usefulSprites.size()));
								nParam += nSprite;
							}
							fixedRule += nParam + " ";
						}
					}
					terminationSet.set(point, fixedRule);
				}
				terminationSet.removeIf(s -> s == null);
				terminationSet = (ArrayList<String>) terminationSet.stream().distinct().collect(Collectors.toList());
				ruleset[1] = new String[terminationSet.size()];
				ruleset[1] = terminationSet.toArray(ruleset[1]);
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			} 
			else {
				String newRule = SharedData.terminations[SharedData.random.nextInt(SharedData.terminations.length)];
				String modRule = ruleset[1][point];
				String[] splitModRule = modRule.split("\\s+");
				splitModRule[0] = newRule;
				newRule = "";
				for(String part : splitModRule) {
					newRule += part + " ";
				}
				ruleset[1][point] = newRule;
				for(int i = 0; i < this.ruleset[1].length; i++) {
					if(ruleset[1][i].contains("limit= ")) {
						System.out.println("Broken");
					}
				
				}
			}
		} 
		else {
			System.err.println("What?! Howd we even get here!?");
		}
	}

	public Chromosome clone(){
		String[][] nRuleset = new String[ruleset.length][];
		nRuleset[0] = new String[ruleset[0].length];
		nRuleset[1] = new String[ruleset[1].length];
		for(int i = 0; i < ruleset[0].length; i++) {
			nRuleset[0][i] = ruleset[0][i];
		}
		for(int i = 0; i < ruleset[1].length; i++) {
			nRuleset[1][i] = ruleset[1][i];
		}
		Chromosome c = new Chromosome(nRuleset, sl);
		return c;
	}

	public void cleanseChromosome() {
		Set<String> cleanser = new HashSet<String>();
		for(int i = 0; i < ruleset[0].length; i++) {
			cleanser.add(ruleset[0][i]);
		}
		ruleset[0] = new String[0];

		ruleset[0] = cleanser.toArray(ruleset[0]);
		
		cleanser = new HashSet<String>();
		for(int i = 0; i < ruleset[1].length; i++) {
			cleanser.add(ruleset[1][i]);
		}
		ruleset[1] = new String[0];
		ruleset[1] = cleanser.toArray(ruleset[1]);
		boolean hasCondition = false;
		SpriteData[] avatarName = SharedData.la.getAvatars(false);
		for(int i = 0; i < ruleset[1].length; i++) {
			if(ruleset[1][i].contains("SpriteCounter") && ruleset[1][i].contains("stype="+avatarName[0].name) && ruleset[1][i].contains("limit=0")) {
				hasCondition = true;
				break;
			}
		}
		if(!hasCondition) {
			String[] tempTerm = new String[ruleset[1].length + 1];
			for(int j = 0; j < ruleset[1].length; j++) {
				tempTerm[j] = ruleset[1][j];
			}
			String termy = "SpriteCounter stype=" + avatarName[0].name + " limit=0 win=";
			int roll = SharedData.random.nextInt(2);
			if(roll == 1) {
				termy += "True";
			} else {
				termy += "False";
			}
			tempTerm[tempTerm.length - 1] = termy;
			ruleset[1] = tempTerm;
		}
	}

	private StateObservation feasibilityTest() {
		HashMap<String, ArrayList<String>> spriteSetStruct = SharedData.constGen.getSpriteSetStructure();
		StateObservation state = sl.testRules(ruleset[0], ruleset[1], spriteSetStruct);		
		errorCount = sl.getErrors().size();
		constrainFitness = 0;
		constrainFitness += (0.5) * 1.0 / (errorCount + 1.0);	
		if(constrainFitness >= 0.5) {
			doNothingLength = Integer.MAX_VALUE;
			for(int i = 0; i < SharedData.REPETITION_AMOUNT; i++) {
				int temp = this.getAgentResult(state.copy(), FEASIBILITY_STEP_LIMIT, SharedData.doNothingAgent);
				if(temp < doNothingLength){
					doNothingLength = temp;
				}
			}
			constrainFitness += 0.2 * (doNothingLength / (40.0));
			
			this.fitness.set(0, constrainFitness);

		}
		return state;
	}
	
	public void calculateFitness(long time) {
		this.badFrames = 0;
		Set<String> events = new HashSet<String>();
		StateObservation stateObs = feasibilityTest();
		if(constrainFitness < 0.7) {
			this.fitness.set(0, constrainFitness);
		}
		else {					
			double score = -200;
			ArrayList<Vector2d> SOs = new ArrayList<>();
			int frameCount = 0;
			double agentBestScore = Double.NEGATIVE_INFINITY;
			double automatedScoreSum = 0.0;
			double automatedWinSum = 0.0;
			int bestSolutionSize = 0;
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				StateObservation tempState = stateObs.copy();
				cleanOpenloopAgents();
				int temp = getAgentResult(tempState, SharedData.EVALUATION_STEP_COUNT, SharedData.automatedAgent);
				frameCount += temp;
				
				if(tempState.getGameScore() > agentBestScore) {
					agentBestScore = tempState.getGameScore();
					bestState = tempState;
					bestSolutionSize = temp;
				}
				
				score = tempState.getGameScore();
				automatedScoreSum += score;
				if(tempState.getGameWinner() == Types.WINNER.PLAYER_WINS){
					automatedWinSum += 1;
				} else if(tempState.getGameWinner() == Types.WINNER.NO_WINNER) {
					automatedWinSum += 0.5;
				}
				
				while(iter1.hasNext()) {
					Event e = iter1.next();
					events.add(e.activeTypeId + "" + e.passiveTypeId);
				}
				score = -200;
			}
			 score = -200;
			 
			double randomScoreSum = 0.0;
			double randomWinSum = 0.0;
			StateObservation randomState = null;
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				StateObservation tempState = stateObs.copy();
				int temp = getAgentResult(tempState, bestSolutionSize, SharedData.randomAgent);
				frameCount += temp;
				randomState = tempState;
				score = randomState.getGameScore();
				randomScoreSum += score;
				if(randomState.getGameWinner() == Types.WINNER.PLAYER_WINS){
					randomWinSum += 1;
				} else if(randomState.getGameWinner() == Types.WINNER.NO_WINNER) {
					randomWinSum += 0.5;
				}
				while(iter1.hasNext()) {
					Event e = iter1.next();
					events.add(e.activeTypeId + "" + e.passiveTypeId);
				}
				score = -200;
			}
			score = -200;
			StateObservation naiveState = null;
			double naiveScoreSum = 0.0;
			double naiveWinSum = 0.0;
			for(int i=0; i<SharedData.REPETITION_AMOUNT; i++){
				StateObservation tempState = stateObs.copy();
				int temp = getAgentResult(tempState, bestSolutionSize, SharedData.naiveAgent);
				frameCount += temp;
				naiveState = tempState;
				
				score = naiveState.getGameScore();
				if(score > -100) {
					naiveScoreSum += score;
					if(naiveState.getGameWinner() == Types.WINNER.PLAYER_WINS){
						naiveWinSum += 1;
					} else if(naiveState.getGameWinner() == Types.WINNER.NO_WINNER) {
						naiveWinSum += 0.5;
					}
				}
				while(iter1.hasNext()) {
					Event e = iter1.next();
					events.add(e.activeTypeId + "" + e.passiveTypeId);
					}
				score = -200;
			}
				double avgBestScore = automatedScoreSum / SharedData.REPETITION_AMOUNT;
				double avgNaiveScore = naiveScoreSum / SharedData.REPETITION_AMOUNT;
				double avgRandomScore = randomScoreSum / SharedData.REPETITION_AMOUNT;
				
				double avgBestWin = automatedWinSum / SharedData.REPETITION_AMOUNT;
				double avgNaiveWin = naiveWinSum / SharedData.REPETITION_AMOUNT;
				double avgRandomWin = randomWinSum / SharedData.REPETITION_AMOUNT;
		    	double sigBest = 1 / (1 + Math.pow(Math.E, (0.1) * -avgBestScore));
				double sigNaive = 1 / (1 + Math.pow(Math.E, (0.1) * -avgNaiveScore));
				double sigRandom = 1 / (1 + Math.pow(Math.E, (0.1) * -avgRandomScore));
				double summedBest = 0.9 * avgBestWin + 0.1 * sigBest;
				double summedNaive = 0.9 * avgNaiveWin + 0.1 * sigNaive;
				double summedRandom = 0.9 * avgRandomWin + 0.1 * sigRandom;

				double gameScore = (summedBest - summedNaive) * (summedNaive - summedRandom);

				if(gameScore > -0.0005) {
					
					gameScore = 0;
				}
				int uniqueCount = events.size();
				double rulesTriggered = uniqueCount / (ruleset[0].length * 1.0f + 1);
				double fitness = (gameScore + 1) * (rulesTriggered);
				constrainFitness = 1.0;
				this.fitness.set(0, constrainFitness);
				this.fitness.set(1, fitness);
		} 
	}

	private int getAgentResult(StateObservation stateObs, int steps, AbstractPlayer agent){
		int i =0;
		int k = 0;
		for(i=0;i<steps;i++){
			if(stateObs.isGameOver()){
				break;
			}
			ElapsedCpuTimer timer = new ElapsedCpuTimer();
			timer.setMaxTimeMillis(SharedData.EVALUATION_STEP_TIME);
			Types.ACTIONS bestAction = agent.act(stateObs, timer);
			stateObs.advance(bestAction);
			k += checkIfOffScreen(stateObs);

		}
		if(k > 0) {
			this.badFrames += k;
		}
		return i;
	}
	public ArrayList<Chromosome> crossover(Chromosome c){
		ArrayList<Chromosome> children = new ArrayList<Chromosome>();
		children.add(this.clone());
		children.add(c.clone());
		String[][] nRuleSetOne;
		String[][] nRuleSetTwo;
		int pointOne = SharedData.random.nextInt(ruleset[0].length);
		int pointTwo = SharedData.random.nextInt(c.getRuleset()[0].length);
		int nSizeOne = pointOne + (c.getRuleset()[0].length - pointTwo);
		int nSizeTwo = pointTwo + (ruleset[0].length - pointOne);
		nRuleSetOne = new String[2][];
		nRuleSetOne[0] = new String[nSizeOne];
		nRuleSetTwo = new String[2][];
		nRuleSetTwo[0] = new String[nSizeTwo];
		for(int i = 0; i < pointOne; i++) {
			nRuleSetOne[0][i] = ruleset[0][i];
		}
		int counter = pointTwo;
		for(int i = pointOne; i < nSizeOne; i++) {
			nRuleSetOne[0][i] = c.getRuleset()[0][counter];
			counter++;
		}
		for(int i = 0; i < pointTwo; i++) {
			nRuleSetTwo[0][i] = c.getRuleset()[0][i];
		}
		counter = pointOne;
		for(int i = pointTwo; i < nSizeTwo; i++) {
			nRuleSetTwo[0][i] = ruleset[0][counter];
		}

		pointOne = SharedData.random.nextInt(ruleset[1].length);
		pointTwo = SharedData.random.nextInt(c.getRuleset()[1].length);		
		nSizeOne = pointOne + (c.getRuleset()[1].length - pointTwo);
		nSizeTwo = pointTwo + (ruleset[1].length - pointOne);
		nRuleSetOne[1] = new String[nSizeOne];
		nRuleSetTwo[1] = new String[nSizeTwo];
		children.get(0).setRuleset(nRuleSetOne);
		children.get(1).setRuleset(nRuleSetTwo);
		for(int i = 0; i < pointOne; i++) {
			nRuleSetOne[1][i] = ruleset[1][i];
		}
		counter = pointTwo;
		for(int i = pointOne; i < nSizeOne; i++) {
			nRuleSetOne[1][i] = c.getRuleset()[1][counter];
			counter++;
		}
		for(int i = 0; i < pointTwo; i++) {
			nRuleSetTwo[1][i] = c.getRuleset()[1][i];
		}
		counter = pointOne;
		for(int i = pointTwo; i < nSizeTwo; i++) {
			nRuleSetTwo[1][i] = ruleset[1][counter];
		}
		for(int i = 0; i < nRuleSetOne.length; i++) {
			ArrayList<String> temp = new ArrayList<> (Arrays.asList(nRuleSetOne[i]));
			temp = (ArrayList<String>) temp.stream().distinct().collect(Collectors.toList());
			nRuleSetOne[i] = new String[temp.size()];
			nRuleSetOne[i] = temp.toArray(nRuleSetOne[i]);

			temp = new ArrayList<> (Arrays.asList(nRuleSetTwo[i]));
			temp = (ArrayList<String>) temp.stream().distinct().collect(Collectors.toList());
			nRuleSetTwo[i] = new String[temp.size()];
			nRuleSetTwo[i] = temp.toArray(nRuleSetTwo[i]);
			}

		return children;
	}

	
	private void cleanOpenloopAgents() {
		((tracks.singlePlayer.advanced.olets.Agent)SharedData.automatedAgent).mctsPlayer = 
			new tracks.singlePlayer.advanced.olets.SingleMCTSPlayer(new Random(), 
				(tracks.singlePlayer.advanced.olets.Agent) SharedData.automatedAgent);
	}
	
	private int checkIfOffScreen(StateObservation stateObs) {
		ArrayList<Observation> allSprites = new ArrayList<Observation>();
		ArrayList<Observation>[] temp = stateObs.getNPCPositions();
		if(temp != null) {
			for(ArrayList<Observation> list : temp) {
				allSprites.addAll(list);
			}	
		}
		temp = stateObs.getImmovablePositions();
		if(temp != null) {
			for(ArrayList<Observation> list : temp) {
				allSprites.addAll(list);
			}
		}
		
		temp = stateObs.getMovablePositions();
		if(temp != null) {
			for(ArrayList<Observation> list : temp) {
				allSprites.addAll(list);
			}
		}
		int xMin = -1 * stateObs.getBlockSize();
		int yMin = -1 * stateObs.getBlockSize();
		int xMax = (SharedData.la.getWidth()+1) * stateObs.getBlockSize();
		int yMax = (SharedData.la.getLength()+1) * stateObs.getBlockSize();
		int counter = 0;
		boolean frameBad = false;
		for(Observation s : allSprites) {
			if(s.position.x < xMin || s.position.x > xMax || s.position.y < yMin || s.position.y > yMax) {
				if(!frameBad) {
					counter++;
					frameBad = true;
				}
			}
		}
		return counter;
		
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
	public ArrayList<Double> getFitness() {
		return fitness;
	}
	public double getConstrainFitness(){
		return constrainFitness;
	}
	public String[][] getRuleset() {
		return ruleset;
	}
	public void setRuleset(String[][] nRuleset) {
		this.ruleset = nRuleset;
	}
}