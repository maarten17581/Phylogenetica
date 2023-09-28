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
public class InferenceAnalysis implements Runnable {

	int taxa;
	int maxInference;
	int iterations;
	int random;
	ArrayList<Inference> inferences;
	ArrayList<ArrayList<Inference>> usable;
	public ArrayList<String> outputs;
	AtomicInteger doneCount;
	int total;
	long time;

	public InferenceAnalysis(int taxa, int maxInference, int iterations, ArrayList<Inference> inferences, int random, AtomicInteger doneCount, int total, long time) {
		this.taxa = taxa;
		this.maxInference = maxInference;
		this.iterations = iterations;
		this.inferences = inferences;
		this.random = random;
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
			ArrayList<Integer> makeable = new ArrayList<>();
			Tree t = Tree.generateRandomTree(taxa);
			//System.out.println(t);
			//System.out.println("Tree made for "+i+" taxa "+(System.currentTimeMillis()-now));
			ArrayList<Quartet> quartets = t.getQuartets(taxa);
			//System.out.println(quartets);
			//System.out.println("Quartets made");
			Collections.shuffle(quartets);
			int[] vx = new int[maxInference];
			int[] vy = new int[maxInference];
			for (int i = 0; i < maxInference; i++) {
				vy[i] = quartets.size();
			}
			int mid = quartets.size()/2;
			long extraNow = System.currentTimeMillis();
			for (int index = 0; index < maxInference; index++) {
				int x = vx[index];
				int y = vy[index];
				mid = (x+y)/2;
				while (y - x > 1) {
					//System.out.println("Start steps for index "+index+" "+(System.currentTimeMillis()-now));
					boolean[] out = new boolean[maxInference];
					int[] outCount = new int[maxInference];
					for (int i = 0; i < random; i++) {
						//Collections.shuffle(quartets);
						boolean[] outSteps = steps(taxa, new ArrayList<Quartet>(quartets.subList(0, mid)), usable, maxInference);
						for (int j = 0; j < maxInference; j++) {
							if (outSteps[j]) {
								outCount[j]++;
							}
						}
						for (int j = 0; j < maxInference; j++) {
							if (outCount[j] > random/2) {
								out[j] = true;
							}
						}
					}
					
					//System.out.println("End steps for index "+index+" "+Arrays.toString(out));
					if (out[index]) {
						y = mid;
					} else {
						x = mid;
					}
					for (int i = index+1; i < maxInference; i++) {
						if (out[i] && vy[i] > mid) {
							vy[i] = mid;
						} else if (!out[i] && vx[i] < mid) {
							vx[i] = mid;
						}
					}
					mid = (x+y)/2;
					//System.out.println("New mid is "+mid+" x is "+x+" y is "+y+" "+(System.currentTimeMillis()-now));
					//System.out.println("-----------------------------");
				}
				makeable.add(y);
				long timeIndex = System.currentTimeMillis();
				//System.out.println("index "+index+" for taxa "+i+" took "+(timeIndex-extraNow)+" milliseconds");
				extraNow = timeIndex;
			}
			long newNow = System.currentTimeMillis();
			//System.out.println("Step "+count+" at taxa size "+taxa+" took "+(newNow-now)+" milliseconds");
			//System.out.println("Results are as follows "+makeable+" for "+quartets.size()+" quartets");
			now = newNow;
			output.add(taxa+" "+makeable+"\n");
			if (makeable.get(makeable.size()-1)<0.1*makeable.get(0)) {
				System.out.println("###########################################");
				System.out.println("Intresting tree:");
				System.out.println(t);
				System.out.println("Its quartets:");
				System.out.println(quartets.subList(0, makeable.get(makeable.size()-1)));
				System.out.println("From:");
				System.out.println(quartets);
				System.out.println("###########################################");
			}
		}
		long newNow = System.currentTimeMillis();
		//System.out.println("Step at taxa size "+taxa+" took a total of "+(newNow-timePerTaxa)+" milliseconds");
		//System.out.println("################################");
		timePerTaxa = newNow;
		//System.out.println("Whole analysis took "+(System.currentTimeMillis()-timeStamp)+" milliseconds");
		int count = doneCount.addAndGet(taxa*(taxa-1)*(taxa-2)*(taxa-3)/24);
		long totalTime = (System.currentTimeMillis()-time);
		long timeLeft = (long)Math.round(totalTime*(((total-count)*1.0/((double)count))));
		System.out.println("Thread with "+taxa+" taxa is done, that is "+count+"/"+total+", at time "+(totalTime/(60*60*1000))+":"+((totalTime/(60*1000))%60)+":"+((totalTime/1000)%60)+","+(totalTime%1000)+", time estimation left "+(timeLeft/(60*60*1000))+":"+((timeLeft/(60*1000))%60)+":"+((timeLeft/1000)%60)+","+(timeLeft%1000));
		return output;
	}

	public boolean[] steps(int taxonCount, ArrayList<Quartet> quartets, ArrayList<ArrayList<Inference>> inferences, int maxInference) throws MatrixInconsistentException, NotCyclicException {
		boolean[] out = new boolean[maxInference];
		for (int i = 0; i < maxInference; i++) {
			out[i] = true;
		}
				
		// step 1
		GF2MatrixDense m = new GF2MatrixDense(taxonCount);
		
		for (Quartet q : quartets) {
			m.addRowForQuartet(q);
		}

		// step 2
		m.rowReduce(false);

		witnessHandling(m, null, null, quartets, inferences, maxInference, out, taxa);

		/*
		// step 3
		DenseVector v = m.determineConformingVector();
		
		// step 4
		CyclicOrder c = v.determineOrder();

		if (c == null) {
			c = new CyclicOrder(taxonCount);
			witnessHandling(m, v, c, quartets, inferences, maxInference, out);
			boolean isOut = false;
			for (int i = 0; i < maxInference && !isOut; i++) {
				isOut &= out[i];
			}
			if (!isOut) {
				return out;
			}
		}
		
		// step 5
		Tree t = TreeSplitFinder.findSplits(c, v, m);

		if(t == null) {
			witnessHandling(m, v, c, quartets, inferences, maxInference, out);
			// step 5
			t = TreeSplitFinder.findSplits(c, v, m);
		}*/
		return out;
	}

	public void witnessHandling(GF2MatrixDense m, DenseVector v, CyclicOrder c, ArrayList<Quartet> quartets, ArrayList<ArrayList<Inference>> inferences, int maxInference, boolean[] out, int taxa) throws MatrixInconsistentException, NotCyclicException{
		
		ArrayList<Witness> witnesses = m.findWitnesses();
		if (witnesses == null) {
			for (int i = 0; i < maxInference; i++) {
				out[i] = false;
			}
			return;
		}
		if (witnesses.isEmpty()) {
			return;
		}
		out[0] = false;
		//Witness.makeGraph(witnesses);
		ArrayList<Quartet> added = new ArrayList<>();
		for (int i = 0; i <= maxInference-2; i++) {
			for (int j = 0; j < inferences.get(i).size(); j++) {
				Inference inf = inferences.get(i).get(j);
				ArrayList<Quartet> add = inf.use(quartets, witnesses);
				if (add.size() > 0) {
					added.addAll(add);
					i = 0;
					j = 0;
					quartets.addAll(add);
					for (Quartet q : add) {
						m.addRowForQuartet(q);
					}
					m.rowReduce(false);
					//witnesses = m.findWitnesses();
				}
				
				if (j == inferences.get(i).size()-1 || witnesses.isEmpty()) {
					witnesses = m.findWitnesses();
					//Witness.makeGraph(witnesses);
					if (witnesses.isEmpty()) {
						break;
					}
				}
			}

			if (witnesses.isEmpty()) {
				break;
			} else {
				out[i+1] = false;
			}
		}
		//System.out.println(added + " added");
		if (witnesses.size() > 0) {
			return;
		}
		/*
		// step 1
		for (Quartet q : added) {
			m.addRowForQuartet(q);
		}

		// step 2
		m.rowReduce(false);

		// step 3
		v.changeAll(m.determineConformingVector());

		// step 4
		c.changeAll(v.determineOrder());
		*/
	}
}
