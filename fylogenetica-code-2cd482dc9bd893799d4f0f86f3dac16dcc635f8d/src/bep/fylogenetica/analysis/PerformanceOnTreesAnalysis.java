package bep.fylogenetica.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import bep.fylogenetica.algorithm.*;
import bep.fylogenetica.io.ipe.IpeDocument;
import bep.fylogenetica.io.ipe.IpeLineGraph;
import bep.fylogenetica.io.ipe.Point2D;
import bep.fylogenetica.model.Level1Network;
import bep.fylogenetica.model.Quartet;
import bep.fylogenetica.model.Tree;

/**
 * This class contains a method to analyze the algorithm's performance on trees.
 */
public class PerformanceOnTreesAnalysis {
	
	public static final int NUM_REPLICATIONS = 5;
	
	/**
	 * Performs the analysis.
	 */
	public static void analyze() {
		
		// first, make sure that JIT does not bias the first few measurements
		// therefore, reconstruct some random trees without measuring
		
		System.out.println("Preparation");
		
		for (int rep = 0; rep < 10; rep++) {
			System.out.println("  rep " + rep);
			
			Tree t = Tree.generateRandomTree(20);
			ArrayList<Quartet> q = t.getQuartets(20);
			
			// create matrix
			GF2Matrix m = new GF2MatrixDense(20);
			for (int k = 0; k < q.size(); k++) {
				m.addRowForQuartet(q.get(k));
			}
			
			// reduce matrix
			m.rowReduce(false);
			
			// reduce matrix using M4RI
			M4RIMatrix.createMatrix(q.size(), 20);
			M4RIMatrix.addQuartets(q);
			M4RIMatrix.rowReduce();
			M4RIMatrix.freeMatrix();
			
			// reconstruct network
			try {
				DenseVector v = m.determineConformingVector();
				CyclicOrder c = v.determineOrder();
				Level1Network n = Level1NetworkSplitFinder.reconstructNetwork(c, v, m);
			} catch (MatrixInconsistentException | NotCyclicException e) {
				System.out.println("Analysis failed since the algorithm crashed:");
				e.printStackTrace();
				return;
			}
		}
		
		ArrayList<Point2D> createMatrixTimes = new ArrayList<>();
		ArrayList<Point2D> reduceMatrixTimes = new ArrayList<>();
		ArrayList<Point2D> reduceM4RIMatrixTimes = new ArrayList<>();
		ArrayList<Point2D> reconstructNetworkTimes = new ArrayList<>();
		
		long startTime, endTime;
		
		for (int N = 20; N < 51; N += 2) {
			System.out.println("N = " + N);
			
			double createMatrixTime = 0;
			double reduceMatrixTime = 0;
			double reduceM4RIMatrixTime = 0;
			double reconstructNetworkTime = 0;
			
			for (int rep = 0; rep < NUM_REPLICATIONS; rep++) {
				System.out.println("  rep " + rep);
				
				Tree t = Tree.generateRandomTree(N);
				ArrayList<Quartet> q = t.getQuartets(N);
				
				// create matrix
				startTime = System.currentTimeMillis();
				GF2Matrix m = new GF2MatrixDense(N);
				for (int k = 0; k < q.size(); k++) {
					m.addRowForQuartet(q.get(k));
				}
				endTime = System.currentTimeMillis();
				createMatrixTime += endTime - startTime;
				
				// reduce matrix
				startTime = System.currentTimeMillis();
				m.rowReduce(false);
				endTime = System.currentTimeMillis();
				reduceMatrixTime += endTime - startTime;
				
				// reduce matrix using M4RI
				M4RIMatrix.createMatrix(q.size(), N);
				M4RIMatrix.addQuartets(q);
				startTime = System.currentTimeMillis();
				M4RIMatrix.rowReduce();
				endTime = System.currentTimeMillis();
				M4RIMatrix.freeMatrix();
				reduceM4RIMatrixTime += endTime - startTime;
				
				// reconstruct network
				try {
					startTime = System.currentTimeMillis();
					DenseVector v = m.determineConformingVector();
					CyclicOrder c = v.determineOrder();
					Level1Network n = Level1NetworkSplitFinder.reconstructNetwork(c, v, m);
					endTime = System.currentTimeMillis();
					reconstructNetworkTime += endTime - startTime;
				} catch (MatrixInconsistentException | NotCyclicException e) {
					System.out.println("Analysis failed since the algorithm crashed:");
					e.printStackTrace();
					return;
				}
			}
			
			createMatrixTimes.add(new Point2D(N, Math.log10(createMatrixTime / NUM_REPLICATIONS)));
			reduceMatrixTimes.add(new Point2D(N, Math.log10(reduceMatrixTime / NUM_REPLICATIONS)));
			reduceM4RIMatrixTimes.add(new Point2D(N, Math.log10(reduceM4RIMatrixTime / NUM_REPLICATIONS)));
			reconstructNetworkTimes.add(new Point2D(N, Math.log10(reconstructNetworkTime / NUM_REPLICATIONS)));
		}
		
		IpeDocument ipeCreate = new IpeDocument();
		ipeCreate.addToPreamble("\\usepackage{mathpazo}");
		ipeCreate.addToPreamble("\\usepackage[euler-digits]{eulervm}");
		ipeCreate.drawObject(new Point2D(128, 128), new Point2D(192, 128), new IpeLineGraph(createMatrixTimes));
		try {
			ipeCreate.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/performance-on-trees-create.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		IpeDocument ipeReduce = new IpeDocument();
		ipeReduce.drawObject(new Point2D(128, 128), new Point2D(192, 128), new IpeLineGraph(reduceMatrixTimes));
		try {
			ipeReduce.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/performance-on-trees-reduce.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		IpeDocument ipeReduceM4RI = new IpeDocument();
		ipeReduceM4RI.drawObject(new Point2D(128, 128), new Point2D(192, 128), new IpeLineGraph(reduceM4RIMatrixTimes));
		try {
			ipeReduceM4RI.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/performance-on-trees-reduce-m4ri.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		IpeDocument ipeReconstruct = new IpeDocument();
		ipeReconstruct.addToPreamble("\\usepackage{mathpazo}");
		ipeReconstruct.addToPreamble("\\usepackage[euler-digits]{eulervm}");
		ipeReconstruct.drawObject(new Point2D(128, 128), new Point2D(192, 128), new IpeLineGraph(reconstructNetworkTimes));
		try {
			ipeReconstruct.writeToFile(new File("/home/willem/Documenten/Studie/Bachelorproject/analysis-output/performance-on-trees-reconstruct.ipe"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
