package tracks.levelGeneration.geneticLevelGenerator.TournamentandEightpointcrossover;

import core.game.GameDescription;
import core.generator.AbstractLevelGenerator;
import tools.ElapsedCpuTimer;
import tools.GameAnalyzer;
import tools.LevelMapping;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

//Added new genetic operators: crossover & tournament selection 

public class LevelGenerator extends AbstractLevelGenerator {

    private LevelMapping bestChromosomeLevelMapping;
    private ArrayList<Double> bestFitness;
    private ArrayList<Integer> numOfFeasible;
    private ArrayList<Integer> numOfInFeasible;

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

    private ArrayList<Chromosome> getNextPopulation(ArrayList<Chromosome> fPopulation, ArrayList<Chromosome> iPopulation){
        ArrayList<Chromosome> newPopulation = new ArrayList<Chromosome>();

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



        while(newPopulation.size() < SharedData.POPULATION_SIZE){
            ArrayList <Chromosome> population = fPopulation;
            population.addAll(iPopulation);
            int n = 4;
            ArrayList<Integer> idxs = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < n; i++) {
                int index = random.nextInt(population.size());
                while (idxs.contains(index)) {
                    index = random.nextInt(population.size());
                }
                idxs.add(index);
            }
            Chromosome parent1 = tournamentSelection(population, new ArrayList<>(idxs.subList(0, n/2)), n/2);
            Chromosome parent2 = tournamentSelection(population, new ArrayList<>(idxs.subList(n/2, idxs.size())), n/2);
            Chromosome child1 = parent1.clone();
            Chromosome child2 = parent2.clone();
            if(SharedData.random.nextDouble() < SharedData.CROSSOVER_PROB){
                ArrayList<Chromosome> children = parent1.kPointCrossOver(parent2,8);
                child1 = children.get(0);
                child2 = children.get(1);
                if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
                    child1.mutate();}
                if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
                    child2.mutate();}
            }

            else if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
                child1.mutate();
            }
            else if(SharedData.random.nextDouble() < SharedData.MUTATION_PROB){
                child2.mutate();
            }

            newPopulation.add(child1);
            newPopulation.add(child2);
        }

        double worst_fitness = 1000000;
        for(int i=0;i<newPopulation.size();i++){
            newPopulation.get(i).calculateFitness(SharedData.EVALUATION_TIME);
            if (newPopulation.get(i).getConstrainFitness() >= 1 && newPopulation.get(i).getCombinedFitness() < worst_fitness) {
                worst_fitness = newPopulation.get(i).getCombinedFitness();
            }
        }

        if (worst_fitness == 1000000) { worst_fitness = 0; } 
        for (int i = 0; i < newPopulation.size(); i++) {
            Chromosome chromosome = newPopulation.get(i);
            if (chromosome.getConstrainFitness() < 1) {
                chromosome.setConstrainFitness(chromosome.getConstrainFitness() - worst_fitness);
                System.out.println("\tChromosome #" + (i+1) + " Constrain Fitness: " + chromosome.getConstrainFitness());
            }
            else {
                System.out.println("\tChromosome #" + (i+1) + " Fitness: " + chromosome.getFitness());
            }
        }

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
        return newPopulation;}

    private Chromosome constraintRouletteWheelSelection(ArrayList<Chromosome> population){
        double[] probabilities = new double[population.size()];
        probabilities[0] = population.get(0).getConstrainFitness();
        for(int i=1; i<population.size(); i++){
            probabilities[i] = probabilities[i-1] + population.get(i).getConstrainFitness() + SharedData.EIPSLON;
        }

        for(int i=0; i<probabilities.length; i++){
            probabilities[i] = probabilities[i] / probabilities[probabilities.length - 1];
        }
        double prob = SharedData.random.nextDouble();
        for(int i=0; i<probabilities.length; i++){
            if(prob < probabilities[i]){
                return population.get(i);
            }
        }
        return population.get(0);
    }


    private Chromosome rouletteWheelSelection(ArrayList<Chromosome> population){
        if(population.get(0).getConstrainFitness() < 1){
            return constraintRouletteWheelSelection(population);
        }
        double[] probabilities = new double[population.size()];
        probabilities[0] = population.get(0).getCombinedFitness();
        for(int i=1; i<population.size(); i++){
            probabilities[i] = probabilities[i-1] + population.get(i).getCombinedFitness() + SharedData.EIPSLON;
        }

        for(int i=0; i<probabilities.length; i++){
            probabilities[i] = probabilities[i] / probabilities[probabilities.length - 1];
        }
        double prob = SharedData.random.nextDouble();
        for(int i=0; i<probabilities.length; i++){
            if(prob < probabilities[i]){
                return population.get(i);
            }
        }

        return population.get(0);
    }

    private Chromosome tournamentSelection(ArrayList<Chromosome> population, ArrayList<Integer> idxs, int n) {
        if (population.get(idxs.get(0)).getConstrainFitness() >= 1
                && population.get(idxs.get(1)).getConstrainFitness() >= 1) {
            return standardTournamentSelection(population, idxs, n);
        }
        else if (population.get(idxs.get(0)).getConstrainFitness() < 1
                && population.get(idxs.get(1)).getConstrainFitness() < 1) {
            return constraintTournamentSelection(population, idxs, n);
        }
        else {
            System.out.println("Return feasible");
           if (population.get(idxs.get(0)).getConstrainFitness() >= 1) {
               return population.get(idxs.get(0));
           }
           else {
               return population.get(idxs.get(1));
           }
        }
    }
    
    private Chromosome constraintTournamentSelection(ArrayList<Chromosome> population, ArrayList<Integer> idxs, int n){
        double[] fitnesses = new double[n];
        for(int i=0; i<n; i++){
            fitnesses[i] = population.get(idxs.get(i)).getConstrainFitness();
        }

        int max_idx = 0;
        for(int i=1; i<n; i++){
            if(fitnesses[max_idx]<fitnesses[i]){
                max_idx = i;
            }
        }
        return population.get(max_idx);
    }

    private Chromosome standardTournamentSelection(ArrayList<Chromosome> population, ArrayList<Integer> idxs, int n){
        double[] fitnesses = new double[n];
        for(int i=0; i<n; i++){
            fitnesses[i] = population.get(idxs.get(i)).getCombinedFitness();
        }
        int max_idx = 0;
        for(int i=1; i<n; i++){
            if(fitnesses[max_idx]<fitnesses[i]){
                max_idx = i;
            }
        }

        System.out.println(population.get(idxs.get(0)).getCombinedFitness());
        System.out.println(population.get(idxs.get(1)).getCombinedFitness());
        System.out.println("standardTournamentSelection: chromosome " + max_idx);
        return population.get(max_idx);
    }

    @Override
    public String generateLevel(GameDescription game, ElapsedCpuTimer elapsedTimer) {
        bestFitness = new ArrayList<Double>();
        numOfFeasible = new ArrayList<Integer>();
        numOfInFeasible = new ArrayList<Integer>();

        SharedData.gameDescription = game;

        int size = 0;
        if(SharedData.gameAnalyzer.getSolidSprites().size() > 0){
            size = 2;
        }
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

            Chromosome chromosome = new Chromosome(width, height);
            if(SharedData.CONSTRUCTIVE_INITIALIZATION){
                chromosome.InitializeConstructive();
            }
            else{
                chromosome.InitializeRandom();
            }
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
        if (worst_fitness == 1000000) { worst_fitness = 0; } 
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
        double worstTime = SharedData.EVALUATION_TIME * SharedData.POPULATION_SIZE;
        double avgTime = worstTime;
        double totalTime = 0;
        int numberOfIterations = 0;

        System.out.println(elapsedTimer.remainingTimeMillis() + " " + avgTime + " " + worstTime);
        while(elapsedTimer.remainingTimeMillis() > 2 * avgTime &&
                elapsedTimer.remainingTimeMillis() > worstTime){
            ElapsedCpuTimer timer = new ElapsedCpuTimer();

            System.out.println("Generation #" + (numberOfIterations + 2) + ": ");
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

        if(fChromosomes.isEmpty()){
            for(int i=0;i<iChromosomes.size();i++){
                iChromosomes.get(i).calculateFitness(SharedData.EVALUATION_TIME);
            }

            Collections.sort(iChromosomes);
            bestChromosomeLevelMapping = iChromosomes.get(0).getLevelMapping();
            System.out.println("Best Fitness: " + iChromosomes.get(0).getConstrainFitness());
            return iChromosomes.get(0).getLevelString(bestChromosomeLevelMapping);
        }

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

    @Override
    public HashMap<Character, ArrayList<String>> getLevelMapping(){
        return bestChromosomeLevelMapping.getCharMapping();
    }
}