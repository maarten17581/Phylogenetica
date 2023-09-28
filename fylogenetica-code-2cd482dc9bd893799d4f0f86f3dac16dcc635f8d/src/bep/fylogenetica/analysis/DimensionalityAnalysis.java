package bep.fylogenetica.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.CyclicOrder;
import bep.fylogenetica.algorithm.DenseVector;
import bep.fylogenetica.algorithm.GF2Matrix;
import bep.fylogenetica.algorithm.GF2MatrixDense;
import bep.fylogenetica.algorithm.MatrixInconsistentException;
import bep.fylogenetica.algorithm.NotCyclicException;
import bep.fylogenetica.algorithm.TreeSplitFinder;
import bep.fylogenetica.model.Inference;
import bep.fylogenetica.model.Quartet;
import bep.fylogenetica.model.Tree;
import bep.fylogenetica.model.Witness;

/**
 * This class contains a method to analyze the algorithm's performance on trees.
 */
public class DimensionalityAnalysis implements Runnable {

	int taxa;
	int maxInference;
	int iterations;
	ArrayList<Inference> inferences;
	ArrayList<ArrayList<Inference>> usable;
	public ArrayList<String> outputs;
	AtomicInteger doneCount;
	int total;
	long time;

	public DimensionalityAnalysis(int taxa, int maxInference, int iterations, ArrayList<Inference> inferences, AtomicInteger doneCount, int total, long time) {
		this.taxa = taxa;
		this.maxInference = maxInference;
		this.iterations = iterations;
		this.inferences = inferences;
		this.doneCount = doneCount;
		this.total = total;
		this.time = time;
		usable = new ArrayList<>();
		for (Inference inf : inferences) {
			int index = inf.input.size()-2;
			while (index >= usable.size()) {
				usable.add(new ArrayList<>());
			}
			usable.get(index).add(inf);
		}
	}

	@Override
	public void run() {
		try {
			outputs = analyzeInference();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> analyzeInference() throws MatrixInconsistentException, NotCyclicException {
		ArrayList<String> output = new ArrayList<>();
		
		long timeStamp = System.currentTimeMillis();
		long now = timeStamp;
		long timePerTaxa = timeStamp;
		for (int count = 0; count < iterations; count++) {
			Tree t = Tree.generateRandomTree(taxa);
			//System.out.println(t);
			//System.out.println("Tree made for "+i+" taxa "+(System.currentTimeMillis()-now));
			ArrayList<Quartet> quartets = t.getQuartets(taxa);
			//System.out.println(quartets);
			//System.out.println("Quartets made");
			Collections.shuffle(quartets);
			GF2MatrixDense[] m = new GF2MatrixDense[maxInference];
			for (int i = 0; i < maxInference; i++) {
				m[i] = new GF2MatrixDense(taxa);
			}
			ArrayList<ArrayList<Quartet>> inUse = new ArrayList<>();
			for (int i = 0; i < maxInference; i++) {
				inUse.add(new ArrayList<>());
			}
			for (int i = 0; i < quartets.size(); i++) {
				ArrayList<Integer> makeable = new ArrayList<>();
				for (int j = 0; j < maxInference; j++) {
					m[j].addRowForQuartet(quartets.get(i));
					m[j].rowReduce(false);
					inUse.get(j).add(quartets.get(i));
					if (j > 0) {
						ArrayList<Witness> witnesses = m[j].findWitnesses();
						if (witnesses.isEmpty()) {
							GF2MatrixDense w = m[j].getKernel();
							w.rowReduce(false);
							makeable.add(w.detemineRank());
							continue;
						}
						for (int k = j-1; k <= j-1; k++) {
							for (int l = 0; l < usable.get(k).size(); l++) {
								Inference inf = usable.get(k).get(l);
								ArrayList<Quartet> add = inf.use(inUse.get(j), witnesses);
								if (add.size() > 0) {
									k = 0;
									l = 0;
									for (int n = j; n < maxInference; n++) {
										inUse.get(n).addAll(add);
										for (Quartet q : add) {
											m[n].addRowForQuartet(q);
										}
										m[n].rowReduce(false);
									}
								}
								
								if (l == usable.get(k).size()-1 || witnesses.isEmpty()) {
									witnesses = m[j].findWitnesses();
									//Witness.makeGraph(witnesses);
									if (witnesses.isEmpty()) {
										break;
									}
								}
							}
							if (witnesses.isEmpty()) {
								break;
							}
						}
						GF2MatrixDense w = m[j].getKernel();
						w.rowReduce(false);
						makeable.add(w.detemineRank());
					} else {
						GF2MatrixDense w = m[j].getKernel();
						w.rowReduce(false);
						makeable.add(w.detemineRank());
					}
				}
				output.add(taxa+" "+(i+1)+" "+makeable+"\n");
				if (i%10 == 0) {
					System.out.println(i + "/" + quartets.size() + " of a thread for iteration "+count);
				}
			}
			long newNow = System.currentTimeMillis();
			//System.out.println("Step "+count+" at taxa size "+taxa+" took "+(newNow-now)+" milliseconds");
			//System.out.println("Results are as follows "+makeable+" for "+quartets.size()+" quartets");
			now = newNow;
		}
		long newNow = System.currentTimeMillis();
		//System.out.println("Step at taxa size "+taxa+" took a total of "+(newNow-timePerTaxa)+" milliseconds");
		//System.out.println("################################");
		timePerTaxa = newNow;
		//System.out.println("Whole analysis took "+(System.currentTimeMillis()-timeStamp)+" milliseconds");
		int count = doneCount.addAndGet(1);
		long totalTime = (System.currentTimeMillis()-time);
		long timeLeft = (long)Math.round(totalTime*(((total-count)*1.0/((double)count))));
		System.out.println("Thread is done, that is "+count+"/"+total+", at time "+(totalTime/(60*60*1000))+":"+((totalTime/(60*1000))%60)+":"+((totalTime/1000)%60)+","+(totalTime%1000)+", time estimation left "+(timeLeft/(60*60*1000))+":"+((timeLeft/(60*1000))%60)+":"+((timeLeft/1000)%60)+","+(timeLeft%1000));
		return output;
	}
}
