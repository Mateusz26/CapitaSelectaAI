package tracks.levelGeneration.geneticLevelGenerator.Improved;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.LevelMapping;

public class LevelGenerator extends AbstractLevelGenerator{

	/**
	 * Level mapping of the best chromosome
	 */
	private LevelMapping bestChromosomeLevelMapping;
	/**
	 * The best chromosome fitness across generations
	 */
	private ArrayList<Double> bestFitness;
	/**
	 * number of feasible chromosomes across generations
	 */
	private ArrayList<Integer> numOfFeasible;
	/**
	 * number of infeasible chromosomes across generations
	 */
	private ArrayList<Integer> numOfInFeasible;
	
	/**
	 * Initializing the level generator
	 * @param game			game description object
	 * @param elapsedTimer	amount of time for intiailization
	 */
	public LevelGenerator(GameDescription game, ElapsedCpuTimer elapsedTimer){
		SharedData.random = new Random();
        SharedData.gameDescription = game;
        SharedData.gameAnalyzer = new GameAnalyzer(game);
        SharedData.constructiveGen = new tracks.levelGeneration.constructiveLevelGenerator.LevelGenerator(game, null);
        bestChromosomeLevelMapping = null;
        bestFitness = null;
        numOfFeasible = null;
        numOfInFeasible = null;
	}
	
	/**
	 * Get the next population based on the current feasible infeasible population
	 * @param fPopulation	array of the current feasible chromosomes
	 * @param iPopulation	array of the current infeasible chromosomes
	 * @return				array of the new chromosomes at the new population
	 */
	private ArrayList<Chromosome> getNextPopulation(ArrayList<Chromosome> fPopulation, ArrayList<Chromosome> iPopulation){
		ArrayList<Chromosome> newPopulation = new ArrayList<Chromosome>();
		
		//collect some statistics about the current generation
		ArrayList<Double> fitnessArray = new ArrayList<Double>();
		for(int i=0;i<fPopulation.size();i++){
			fitnessArray.add(fPopulation.get(i).getFitness().get(0));
		}

		Collections.sort(fitnessArray);
		if(fitnessArray.size() > 0){
			bestFitness.add(fitnessArray.get(fitnessArray.size() - 1));
		}
		else{
			bestFitness.add((double) 0);
		}
		numOfFeasible.add(fPopulation.size());
		numOfInFeasible.add(iPopulation.size());
		

		
		
			
//		while(newPopulation.size() < SharedData.POPULATION_SIZE){
//			//choosing which population to work on with 50/50 probability 
//			//of selecting either any of them
//			ArrayList<Chromosome> population = fPopulation;
//			if(fPopulation.size() <= 0){
//				population = iPopulation;
//			}
//			if(SharedData.random.nextDouble() < 0.5){
//				population = iPopulation;
//				if(iPopulation.size() <= 0){
//					population = fPopulation;
//				}
//			}
//
//			//select the parents using roulletewheel selection
//			Chromosome parent1 = rouletteWheelSelection(population);
//			Chromosome parent2 = rouletteWheelSelection(population);
//			Chromosome child1 = parent1.clone();
//			Chromosome child2 = parent2.clone();
		
		
		
		
		while(newPopulation.size() < SharedData.POPULATION_SIZE) { 
			//initialize the ArraList with all the feasible and infeasible Chromosomes 
			ArrayList <Chromosome> population = fPopulation; 
			population.addAll(iPopulation); 
			
			//choose 2 different pairs of parents who will compete in a tournament 
			//hence, we need to choose 4 different parents at random 
			//we do this by adding each parent to the array, 
			ArrayList<Integer> indexes = new ArrayList<>(); 
			for(int i = 1; i < population.size(); i++) { 
				indexes.add(new Integer(i)); 
			}
			//then shuffling the array 
			Collections.shuffle(indexes);
			//and finally, selecting only the first 4 elements from the array 
			int index1 = indexes.get(0);
			int index2 = indexes.get(1); 
			int index3 = indexes.get(2); 
			int index4 = indexes.get(3); 
			
			//select the parents by letting them compete in a tournament 
			Chromosome parent1 = tournamentSelection(population, index1, index2); 
			Chromosome parent2 = tournamentSelection(population, index3, index4); 
			Chromosome child1 = parent1.clone();
			Chromosome child2 = parent2.clone(); 
		
			
			//do cross over
			if(SharedData.random.nextDouble() < SharedData.CROSSOVER_PROB){
				ArrayList<Chromosome> children = parent1.twoPointCrossOver(parent2); 
				child1 = children.get(0);
				child2 = children.get(1);
				

				//do mutation to the children
				if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
					child1.mutate();
				}
				if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
					child2.mutate();
				}
			}

			//mutate the copies of the parents
			else if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
				child1.mutate();
			}
			else if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
				child2.mutate();
			}
			

			//add the new children to the new population
			newPopulation.add(child1);
			newPopulation.add(child2);
		}
		

		//calculate fitness of the new population chromosomes 
//		for(int i=0;i<newPopulation.size();i++){
//			newPopulation.get(i).calculateFitness(SharedData.EVALUATION_TIME);
//			if(newPopulation.get(i).getConstrainFitness() < 1){
//				System.out.println("\tChromosome #" + (i+1) + " Constrain Fitness: " + newPopulation.get(i).getConstrainFitness());
//			}
//			else{
//				System.out.println("\tChromosome #" + (i+1) + " Fitness: " + newPopulation.get(i).getFitness());
//			}
//		}

		
		//calculate fitness of the new population chromosomes 
		double worstFitness = Double.POSITIVE_INFINITY; 
		
		for(int i = 0; i < newPopulation.size(); i++) { 
			newPopulation.get(i).calculateFitness(SharedData.EVALUATION_TIME); 
			
			//feasible new population 
			if(newPopulation.get(i).getConstrainFitness() >= 1 && newPopulation.get(i).getCombinedFitness() < worstFitness) { 
				worstFitness = newPopulation.get(i).getCombinedFitness(); 
			}
		}
		
		//the infeasible population gets penalized by using the worstFitness value 
		//first initialize the worstFitness if this has not been done 
		if(worstFitness == Double.POSITIVE_INFINITY) { 
			worstFitness = 0; 
		}
		
		for(int i = 0; i < newPopulation.size(); i++) { 
			Chromosome chromosome = newPopulation.get(i); 
			
			//now penalize the infeasible population 
			if(chromosome.getConstrainFitness() < 1) { 
				//penalize by subtracting the worstFitness of the contraintFitness 
				chromosome.setConstrainFitness(chromosome.getConstrainFitness() - worstFitness); 
				System.out.println("\tChromosome #" + (i+1) + " Constrain Fitness: " + chromosome.getConstrainFitness()); 
			} else { 
				System.out.println("\tChromosome #" + (i+1) + " Fitness: " + chromosome.getFitness()); 
			}
		}
		
		

		//add the best chromosome(s) from old population to the new population
		Collections.sort(newPopulation);
		for(int i=SharedData.POPULATION_SIZE - SharedData.ELITISM_NUMBER;i<newPopulation.size();i++){
			newPopulation.remove(i);
		}

		if(fPopulation.isEmpty()){
			Collections.sort(iPopulation);
			for(int i=0;i<SharedData.ELITISM_NUMBER;i++){
				newPopulation.add(iPopulation.get(i));
			}
		}
		else{
			Collections.sort(fPopulation);
			for(int i=0;i<SharedData.ELITISM_NUMBER;i++){
				newPopulation.add(fPopulation.get(i));
			}
		}
		
		return newPopulation;
	}

	/**
	 * Roullete wheel selection for the infeasible population
	 * @param population	array of chromosomes surviving in this population
	 * @return				the picked chromosome based on its constraint fitness
	 */
	private Chromosome constraintRouletteWheelSelection(ArrayList<Chromosome> population){
		//calculate the probabilities of the chromosomes based on their fitness
		double[] probabilities = new double[population.size()];
		probabilities[0] = population.get(0).getConstrainFitness();
		for(int i=1; i<population.size(); i++){
			probabilities[i] = probabilities[i-1] + population.get(i).getConstrainFitness() + SharedData.EIPSLON;
		}
		
		for(int i=0; i<probabilities.length; i++){
			probabilities[i] = probabilities[i] / probabilities[probabilities.length - 1];
		}
		

		//choose a chromosome based on its probability
		double prob = SharedData.random.nextDouble();
		for(int i=0; i<probabilities.length; i++){
			if(prob < probabilities[i]){
				return population.get(i);
			}
		}
		
		return population.get(0);
	}


	/**
	 * Get the fitness for any population
	 * @param population	array of chromosomes surviving in this population
	 * @return				the picked chromosome based on its fitness
	 */
	private Chromosome rouletteWheelSelection(ArrayList<Chromosome> population){
		//if the population is infeasible use the constraintRoulletWheel function
		if(population.get(0).getConstrainFitness() < 1){
			return constraintRouletteWheelSelection(population);
		}
		

		//calculate the probabilities for the current population
		double[] probabilities = new double[population.size()];
		probabilities[0] = population.get(0).getCombinedFitness();
		for(int i=1; i<population.size(); i++){
			probabilities[i] = probabilities[i-1] + population.get(i).getCombinedFitness() + SharedData.EIPSLON;
		}
		
		for(int i=0; i<probabilities.length; i++){
			probabilities[i] = probabilities[i] / probabilities[probabilities.length - 1];
		}

		//choose random chromosome based on its fitness
		double prob = SharedData.random.nextDouble();
		for(int i=0; i<probabilities.length; i++){
			if(prob < probabilities[i]){
				return population.get(i);
			}
		}
		
		return population.get(0);
	}
	
	
	
	
	/** 
	 * Before applying tournament selection we have to determine whether we have 2 feasible, 2 infeasible or 
	 * a mixed pair of chromosomes 
	 * @param population     array of chromosomes surviving in this population 
	 * @return               the chromosome selected by the tournament 
	 */
	private Chromosome tournamentSelection(ArrayList<Chromosome> population, int index1, int index2) { 
		//when both chromosomes are infeasible, run constraintTournamentSelection 
		if(population.get(index1).getConstrainFitness() < 1 && population.get(index2).getConstrainFitness() < 1) { 
			return constraintTournamentSelection(population, index1, index2); 
			
			//when both chromosomes are feasible, run feasibleTournamentSelection 
		} else if(population.get(index1).getConstrainFitness() >= 1 && population.get(index2).getConstrainFitness() >= 1) { 
			return feasibleTournamentSelection(population, index1, index2);
			
			//when we have a mix of a feasible and infeasible chromosome, pick the feasible one 
		} else { 
			if(population.get(index1).getConstrainFitness() >= 1) { 
				return population.get(index1); 
			} else { 
				return population.get(index2); 
			}
		}
	}
	
	/**
	 * Constraint tournament selection, for the infeasible population 
	 * @param population     array of the chromosomes surviving in this population 
	 * @return               the chromosome selected by the tournament  
	 */
	private Chromosome constraintTournamentSelection(ArrayList<Chromosome> population, int index1, int index2) { 
		//get the constrained fitnesses of both the chromosomes 
		double fitness1 = population.get(index1).getConstrainFitness();  
		double fitness2 = population.get(index2).getConstrainFitness(); 
		
		//select the individual with the best fitness with a probability p
		//probability of 90% to select the indiviual with the best fitness 
		int p = 9; 
		
		//create a list with 9 times the winning individual and 1 time the losing individual 
		// n = 100% probability, and thus also the list length 
		int n = 10; 
		int[] probabilities = new int[n]; 
		
		
		//determine individual with the highest fitness and fill the list 
		if(fitness1 < fitness2) { 
			
			for(int i = 0; i < n; i++) { 
				if(i < p) { 
					probabilities[i] = 2;
				} else { 
					probabilities[i] = 1;
				}
			}
			
		} else { 

			for(int i = 0; i < n; i++) { 
				if(i < p) { 
					probabilities[i] = 1;
				} else { 
					probabilities[i] = 2;
				}
			}
		}
		
		//generate a random number 
		Random rand = new Random(); 
		int randomIndex = rand.nextInt(n); 
		
		//return individual with highest fitness with a probability of 90% 
		//or the individual with the lowest fitness with a probability of 10% 
		int winningIndividual = (int)Array.get(probabilities, randomIndex);  
		if(winningIndividual == 1) { 
			return population.get(index1); 
		} else { 
			return population.get(index2); 
		}
	}
	
	/** 
	 * Tournament selection when dealing with a feasible population 
	 * @param population     array of the chromosomes surviving in this population 
	 * @return               the chromosome selected by the tournament 
	 */ 
	private Chromosome feasibleTournamentSelection(ArrayList<Chromosome> population, int index1, int index2) { 
		//get the combined fitnesses of both the chromosomes 
		double fitness1 = population.get(index1).getCombinedFitness();   
		double fitness2 = population.get(index2).getCombinedFitness(); 
		
		//select the individual with the best fitness with a probability p
		//probability of 90% to select the indiviual with the best fitness 
		int p = 9; 
		
		//create a list with 9 times the winning individual and 1 time the losing individual 
		// n = 100% probability, and thus also the list length 
		int n = 10; 
		int[] probabilities = new int[n]; 
		
		
		//determine individual with the highest fitness and fill the list 
		if(fitness1 < fitness2) { 
			
			for(int i = 0; i < n; i++) { 
				if(i < p) { 
					probabilities[i] = 2;
				} else { 
					probabilities[i] = 1;
				}
			}
			
		} else { 
			
			for(int i = 0; i < n; i++) { 
				if(i < p) { 
					probabilities[i] = 1;
				} else { 
					probabilities[i] = 2;
				}
			}
		}
		
		//generate a random number 
		Random rand = new Random(); 
		int randomIndex = rand.nextInt(n); 
		
		//return individual with highest fitness with a probability of 90% 
		//or the individual with the lowest fitness with a probability of 10% 
		int winningIndividual = (int)Array.get(probabilities, randomIndex);  
		if(winningIndividual == 1) { 
			return population.get(index1); 
		} else { 
			return population.get(index2); 
		}
	}
	
	

	
	/**
	 * generates the random level width and height from hardcoded average values
	 * @return width and height in an array; 0=width and 1=height
	 */
	private int[] generateLevelSize()
	{
		// average size from example levels
		double avgWidth = 21.74;
		double avgHeight = 12.44;
		double blocks = SharedData.analyzer.getAllBlocks().size();
		if (blocks == 0) {
			--avgWidth;
			--avgHeight;
		}
		// apply resizing factor based on # different blocks
		avgWidth = avgWidth * (1 + (blocks-9)/100);
		avgHeight = avgHeight * (1 + (blocks-9)/100);
		// add randomness
		int[] result = new int[2];
		result[0] = (int)SharedData.noise(avgWidth, 0.5, SharedData.random.nextDouble());
		result[1] = (int)SharedData.noise(avgHeight, 0.5, SharedData.random.nextDouble());

		return result;
	}
	
	
	/**
	 * Generate a level using GA in a fixed amount of time and 
	 * return the level in form of a string
	 * @param game			the current game description object
	 * @param elapsedTimer	the amount of time allowed for generation
	 * @return				string for the generated level
	 */
	@Override
    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
        //initialize the statistics objects
        bestFitness = new ArrayList<Double>();
        numOfFeasible = new ArrayList<Integer>();
        numOfInFeasible = new ArrayList<Integer>();

        SharedData.gameDescription = game;

        int size = 0;
        if(SharedData.gameAnalyzer.getSolidSprites().size() > 0){
            size = 2;
        }

        //get the level size
        int width = (int)Math.max(SharedData.MIN_SIZE + size, game.getAllSpriteData().size() * (1 + 0.25 * SharedData.random.nextDouble()) + size);
        int height = (int)Math.max(SharedData.MIN_SIZE + size, game.getAllSpriteData().size() * (1 + 0.25 * SharedData.random.nextDouble()) + size);
        width = (int)Math.min(width, SharedData.MAX_SIZE + size);
        height = (int)Math.min(height, SharedData.MAX_SIZE + size);

        System.out.println("Generation #1: ");

        double worst_fitness = 1000000;
        ArrayList<Chromosome> fChromosomes = new ArrayList<Chromosome>();
        ArrayList<Chromosome> iChromosomes = new ArrayList<Chromosome>();
        ArrayList<Chromosome> population = new ArrayList<Chromosome>();
        for(int i =0; i < SharedData.POPULATION_SIZE; i++){

            //initialize the population using either randomly or using contructive level generator
        	Chromosome chromosome = new Chromosome(width, height);

            if(SharedData.CONSTRUCTIVE_INITIALIZATION){
                chromosome.InitializeConstructive();
            }
            else{
                chromosome.InitializeRandom();
            }
		
		//calculate the fitness for all the chromosomes and add them to the correct population
		//either the feasible or the infeasible one 
		chromosome.calculateFitness(SharedData.EVALUATION_TIME);
        if(chromosome.getConstrainFitness() < 1){
            iChromosomes.add(chromosome);
            population.add(chromosome);
        }
        else{
            fChromosomes.add(chromosome);
            population.add(chromosome);
            if (chromosome.getCombinedFitness() < worst_fitness) {
                worst_fitness = chromosome.getCombinedFitness();
                System.out.println("New worst fitness: " + worst_fitness);
            }
        }
        System.out.println("\tChromosome #" + (i+1) + " done.");
    }
		
		//penalize the infeasible population 
		if (worst_fitness == 1000000) { worst_fitness = 0; } // Initialize if necessary
        for (int i = 0; i < population.size(); i++) {
            Chromosome chromosome = population.get(i);
            if (chromosome.getConstrainFitness() < 1) {
                chromosome.setConstrainFitness(chromosome.getConstrainFitness() - worst_fitness);
                System.out.println("\tChromosome #" + (i+1) + " Constrain Fitness: " + chromosome.getConstrainFitness());
            }
            else {
                System.out.println("\tChromosome #" + (i+1) + " Fitness: " + chromosome.getFitness());
            }
        }
		

		//some variables to make sure not getting out of time
		double worstTime = SharedData.EVALUATION_TIME * SharedData.POPULATION_SIZE;
        double avgTime = worstTime;
        double totalTime = 0;
        int numberOfIterations = 0;

        System.out.println(elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
        while(elapsedTimer.remainingTimeMillis() > 2 * avgTime &&
                elapsedTimer.remainingTimeMillis() > worstTime){
            ElapsedCpuTimer timer = new ElapsedCpuTimer();

            System.out.println("Generation #" + (numberOfIterations + 2) + ": ");
				
			//get the new population and split it to a the feasible and infeasible populations
			ArrayList<Chromosome> chromosomes = getNextPopulation(fChromosomes, iChromosomes);
            fChromosomes.clear();
            iChromosomes.clear();
            for(Chromosome c:chromosomes) {
                if (c.getConstrainFitness() < 1) {
                    iChromosomes.add(c);
                } else {
                    fChromosomes.add(c);
                }
            }

            numberOfIterations += 1;
            totalTime += timer.elapsedMillis();
            avgTime = totalTime / numberOfIterations;
        }
		
		
		//return the best infeasible chromosome
		if(fChromosomes.isEmpty()){
            for(int i=0;i<iChromosomes.size();i++){
                iChromosomes.get(i).calculateFitness(SharedData.EVALUATION_TIME);
            }

            Collections.sort(iChromosomes);
            bestChromosomeLevelMapping = iChromosomes.get(0).getLevelMapping();
            System.out.println("Best Fitness: " + iChromosomes.get(0).getConstrainFitness());
            return iChromosomes.get(0).getLevelString(bestChromosomeLevelMapping);
        }
				
		//return the best feasible chromosome otherwise and print some statistics
		for(int i=0;i<fChromosomes.size();i++){
            fChromosomes.get(i).calculateFitness(SharedData.EVALUATION_TIME);
        }
        Collections.sort(fChromosomes);
        bestChromosomeLevelMapping = fChromosomes.get(0).getLevelMapping();
        System.out.println("Best Chromosome Fitness: " + fChromosomes.get(0).getFitness());
        System.out.println(bestFitness);
        System.out.println(numOfFeasible);
        System.out.println(numOfInFeasible);
        return fChromosomes.get(0).getLevelString(bestChromosomeLevelMapping);
	}


	/**
	 * get the current used level mapping to create the level string
	 * @return	the level mapping used to create the level string
	 */
	@Override
	public HashMap<Character, ArrayList<String>> getLevelMapping(){
		return bestChromosomeLevelMapping.getCharMapping();
	}
}
