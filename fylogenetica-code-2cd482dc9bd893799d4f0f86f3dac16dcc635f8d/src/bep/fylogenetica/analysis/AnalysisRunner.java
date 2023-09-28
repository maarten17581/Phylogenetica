package bep.fylogenetica.analysis;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.DenseVector;
import bep.fylogenetica.algorithm.GF2Matrix;
import bep.fylogenetica.algorithm.MatrixInconsistentException;
import bep.fylogenetica.algorithm.NotCyclicException;
import bep.fylogenetica.model.Quartet;
import bep.fylogenetica.model.Tree;

/**
 * This class handles the analysis of the algorithm.
 */
public class AnalysisRunner {
	
	/**
	 * Runs the analysis.
	 * 
	 * @param f The {@link Fylogenetica} object.
	 */
	public static void doAnalysis(Fylogenetica f) {
		
		//PerformanceOnTreesAnalysis.analyze();
		//AccuracyOnTreesAnalysis.analyze25Taxa();
		//AccuracyOnTreesAnalysis.analyzeVaryingTaxa();

		// Inference analysis with multithreading
		/*
		try {
			long time = System.currentTimeMillis();
			ArrayList<String> outputs = new ArrayList<>();
			int minTaxa = 5;
			int maxTaxa = 16;
			int maxInference = 5;
			int iterations = 100;
			int threadCount = 10;
			int random = 1;
			int total = 0;
			for (int i = minTaxa; i <= maxTaxa; i++) {
				total += threadCount*i*(i-1)*(i-2)*(i-3)/24;
			}
			InferenceAnalysis[][] analyzers = new InferenceAnalysis[threadCount][maxTaxa-minTaxa+1];
			Thread[][] threads = new Thread[threadCount][maxTaxa-minTaxa+1];
			AtomicInteger doneCount = new AtomicInteger(0);
			for (int taxa = minTaxa; taxa <= maxTaxa; taxa++) {
				new DenseVector(taxa);
				for (int i = 0; i < threadCount; i++) {
					analyzers[i][taxa-minTaxa] = new InferenceAnalysis(taxa, maxInference, iterations/threadCount, f.model.inferences, random, doneCount, total, time);
					threads[i][taxa-minTaxa] = new Thread(analyzers[i][taxa-minTaxa]);
					threads[i][taxa-minTaxa].start();
				}
				System.out.println("All threads for "+taxa+" are started");
			}
			System.out.println("########## All threads are started ##########");
			for (int taxa = minTaxa; taxa <= maxTaxa; taxa++) {
				for (int i = 0; i < threadCount; i++) {
					threads[i][taxa-minTaxa].setPriority(Thread.MAX_PRIORITY);
					threads[i][taxa-minTaxa].join();
					outputs.addAll(analyzers[i][taxa-minTaxa].outputs);
				}
			}
			//Collections.sort(outputs);
			File file = new File("C:/Users/20202991/Dropbox/My PC (S20202991)/Desktop/Inference_Analysis.txt");
			FileWriter writer = new FileWriter(file);
			for (String out : outputs) {
				writer.write(out);
			}
			writer.close();
			long timeTook = System.currentTimeMillis()-time;
			System.out.println("Whole proces took "+(timeTook/(60*60*1000))+":"+((timeTook/(60*1000))%60)+":"+((timeTook/1000)%60)+","+(timeTook%1000));
		} catch (Exception e) {
			System.out.println("Inference Analysis gave an error");
			e.printStackTrace();
		}
		*/

		// Dimensionality analysis with multithreading
		try {
			long time = System.currentTimeMillis();
			ArrayList<String> outputs = new ArrayList<>();
			int taxa = 10;
			int maxInference = 5;
			int iterations = 490;
			int threadCount = 35;
			DimensionalityAnalysis[] analyzers = new DimensionalityAnalysis[threadCount];
			Thread[] threads = new Thread[threadCount];
			AtomicInteger doneCount = new AtomicInteger(0);
			for (int i = 0; i < threadCount; i++) {
				analyzers[i] = new DimensionalityAnalysis(taxa, maxInference, iterations/threadCount, f.model.inferences, doneCount, threadCount, time);
				threads[i] = new Thread(analyzers[i]);
				threads[i].start();
			}
			System.out.println("########## All threads are started ##########");
			for (int i = 0; i < threadCount; i++) {
				threads[i].setPriority(Thread.MAX_PRIORITY);
				threads[i].join();
				outputs.addAll(analyzers[i].outputs);
			}
			//Collections.sort(outputs);
			File file = new File("C:/Users/20202991/Dropbox/My PC (S20202991)/Desktop/Dimensionality_Analysis.txt");
			FileWriter writer = new FileWriter(file);
			for (String out : outputs) {
				writer.write(out);
			}
			writer.close();
			long timeTook = System.currentTimeMillis()-time;
			System.out.println("Whole proces took "+(timeTook/(60*60*1000))+":"+((timeTook/(60*1000))%60)+":"+((timeTook/1000)%60)+","+(timeTook%1000));
		} catch (Exception e) {
			System.out.println("Inference Analysis gave an error");
			e.printStackTrace();
		}
	}
}
