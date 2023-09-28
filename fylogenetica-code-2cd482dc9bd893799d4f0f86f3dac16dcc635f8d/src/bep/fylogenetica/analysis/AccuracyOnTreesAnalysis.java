package bep.fylogenetica.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

import bep.fylogenetica.io.ipe.IpeDocument;
import bep.fylogenetica.io.ipe.IpeLineGraph;
import bep.fylogenetica.io.ipe.Point2D;
import bep.fylogenetica.model.Quartet;
import bep.fylogenetica.model.Tree;

/**
 * This class contains a method to analyze the algorithm's performance on trees.
 */
public class AccuracyOnTreesAnalysis {
	
	/**
	 * Performs an analysis for trees with 25 taxa (sec. 6.2, beginning).
	 */
	public static void analyze25Taxa() {
		
		// the amount of taxa to use
		int taxonCount = 25;
		
		// if the matrix has the following rank, the algorithm is succesful
		int goalRank = (taxonCount - 1) * (taxonCount - 2) / 2 - (taxonCount - 2);
		
		System.out.println("Goal rank: " + goalRank);
		
		// amount of replications per quartet fraction
		int numReplications = 1000;
		
		ArrayList<Point2D> successFractions = new ArrayList<>();
		ArrayList<Point2D> meanRanks = new ArrayList<>();
		
		for (double quartetFraction = 0.01; quartetFraction < 0.25; quartetFraction += 0.01) {
			
			System.out.print("quartet fraction = " + quartetFraction);

			int rankSum = 0;
			int success = 0;
			
			for (int i = 0; i < numReplications; i++) {
				Tree t = Tree.generateRandomTree(taxonCount);
				ArrayList<Quartet> q = t.getQuartets(taxonCount);
				Collections.shuffle(q);
				
				M4RIMatrix.createMatrix((int) (q.size() * quartetFraction), taxonCount);
				short[] quartets = new short[4 * (int) (q.size() * quartetFraction)];
				for (int k = 0; k < (int) (q.size() * quartetFraction); k++) {
					Quartet quartet = q.get(k);
					quartet.toCanonicalForm();
					quartets[4 * k] = (short) quartet.left1;
					quartets[4 * k + 1] = (short) quartet.left2;
					quartets[4 * k + 2] = (short) quartet.right1;
					quartets[4 * k + 3] = (short) quartet.right2;
				}
				M4RIMatrix.addQuartets(quartets);
				int rank = M4RIMatrix.rowReduce();
				rankSum += rank;
				if (rank == goalRank) {
					success++;
				}
				M4RIMatrix.freeMatrix();
			}
			
			System.out.println(": mean rank = " + ((double) rankSum) / numReplications + ", success fraction = " + ((double) success) / numReplications);
			
			successFractions.add(new Point2D(quartetFraction, ((double) success) / numReplications));
		}
		
		IpeDocument ipeMean = new IpeDocument();
		ipeMean.addToPreamble("\\usepackage{mathpazo}");
		ipeMean.addToPreamble("\\usepackage[euler-digits]{eulervm}");
		ipeMean.drawObject(new Point2D(128, 128), new Point2D(192, 128), new IpeLineGraph(meanRanks));
		try {
			ipeMean.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/accuracy-on-trees-meanrank.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		IpeDocument ipeFraction = new IpeDocument();
		ipeFraction.drawObject(new Point2D(128, 128), new Point2D(192, 128), new IpeLineGraph(successFractions));
		try {
			ipeFraction.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/accuracy-on-trees-successfraction.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Performs an analysis for trees with varying amounts of taxa. For every taxon count,
	 * the quartet fraction is determined that is needed to let the algorithm work in half
	 * of all cases.
	 */
	public static void analyzeVaryingTaxa() {

		ArrayList<Point2D> fractions = new ArrayList<>();
		ArrayList<Point2D> n4 = new ArrayList<>();
		ArrayList<Point2D> n3logn = new ArrayList<>();
		ArrayList<Point2D> n3 = new ArrayList<>();
		ArrayList<Point2D> n2logn = new ArrayList<>();
		ArrayList<Point2D> n2 = new ArrayList<>();
		
		// amount of replications per quartet fraction
		int numReplications = 200;
		
		for (int taxonCount = 5; taxonCount <= 35; taxonCount += 2) {
			
			// if the matrix has the following rank, the algorithm is succesful
			int goalRank = (taxonCount - 1) * (taxonCount - 2) / 2 - (taxonCount - 2);
			
			System.out.println(taxonCount + " taxa (goal rank: " + goalRank + ")");
			
			// we are going to do a binary search for the requested number
			double lower = 0;
			double upper = 1;
			
			int numberOfQuartets = 0; // TODO ugly!
			
			while (upper - lower > 0.0001) {
				double pivot = (upper + lower) / 2;
				
				// compute the success fraction with pivot as the quartet fraction
				int success = 0;
				
				for (int i = 0; i < numReplications; i++) {
					Tree t = Tree.generateRandomTree(taxonCount);
					ArrayList<Quartet> q = t.getQuartets(taxonCount);
					Collections.shuffle(q);
					numberOfQuartets = q.size(); // TODO ugly!
					
					M4RIMatrix.createMatrix((int) (q.size() * pivot), taxonCount);
					M4RIMatrix.addQuartets(q.subList(0, (int) (q.size() * pivot)));
					int rank = M4RIMatrix.rowReduce();
					if (rank == goalRank) {
						success++;
					}
					M4RIMatrix.freeMatrix();
				}
				
				// this is the success fraction
				double successFraction = ((double) success) / numReplications;
				
				System.out.println("  with quartet fraction " + pivot + ": success fraction " + successFraction);
				
				if (successFraction < 0.5) {
					lower = pivot;
				} else {
					upper = pivot;
				}
			}
			
			double quartetsNeeded = numberOfQuartets * ((upper + lower) / 2);
			fractions.add(new Point2D(taxonCount, (upper + lower) / 2));
			n4.add(new Point2D(taxonCount, quartetsNeeded / (Math.pow(taxonCount, 4))));
			n3logn.add(new Point2D(taxonCount, quartetsNeeded / (Math.pow(taxonCount, 3) * (Math.log(taxonCount) / Math.log(2)))));
			n3.add(new Point2D(taxonCount, quartetsNeeded / (Math.pow(taxonCount, 3))));
			n2logn.add(new Point2D(taxonCount, quartetsNeeded / (Math.pow(taxonCount, 2) * (Math.log(taxonCount) / Math.log(2)))));
			n2.add(new Point2D(taxonCount, quartetsNeeded / (Math.pow(taxonCount, 2))));
			System.out.println("  needed quartet fraction: " + (upper + lower) / 2 + " = " + quartetsNeeded + " quartets");
		}
		
		IpeDocument ipe = new IpeDocument();
		IpeLineGraph graph = new IpeLineGraph(fractions);
		graph.yMax = 0.5;
		graph.yStep = 0.1;
		graph.xMax = 40;
		ipe.drawObject(new Point2D(128, 128), new Point2D(192, 128), graph);
		try {
			ipe.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/accuracy-on-trees/fractions.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ipe = new IpeDocument();
		graph = new IpeLineGraph(n4);
		graph.yMax = 0.02;
		graph.yStep = 0.004;
		graph.xMax = 40;
		ipe.drawObject(new Point2D(128, 128), new Point2D(108, 72), graph);
		try {
			ipe.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/accuracy-on-trees/n4.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ipe = new IpeDocument();
		graph = new IpeLineGraph(n3logn);
		graph.yMax = 0.02;
		graph.yStep = 0.004;
		graph.xMax = 40;
		ipe.drawObject(new Point2D(128, 128), new Point2D(108, 72), graph);
		try {
			ipe.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/accuracy-on-trees/n3logn.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ipe = new IpeDocument();
		graph = new IpeLineGraph(n3);
		graph.yMax = 0.1;
		graph.yStep = 0.02;
		graph.xMax = 40;
		ipe.drawObject(new Point2D(128, 128), new Point2D(108, 72), graph);
		try {
			ipe.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/accuracy-on-trees/n3.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ipe = new IpeDocument();
		graph = new IpeLineGraph(n2logn);
		graph.yMax = 0.75;
		graph.yStep = 0.15;
		graph.xMax = 40;
		ipe.drawObject(new Point2D(128, 128), new Point2D(108, 72), graph);
		try {
			ipe.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/accuracy-on-trees/n2logn.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ipe = new IpeDocument();
		graph = new IpeLineGraph(n2);
		graph.yMax = 4;
		graph.yStep = 0.8;
		graph.xMax = 40;
		ipe.drawObject(new Point2D(128, 128), new Point2D(108, 72), graph);
		try {
			ipe.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/accuracy-on-trees/n2.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
