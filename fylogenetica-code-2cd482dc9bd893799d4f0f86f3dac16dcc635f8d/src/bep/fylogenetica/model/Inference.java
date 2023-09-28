package bep.fylogenetica.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A rule for reconstructing trees that can be used to create more quartets.
 */
public class Inference {
	
	public int taxaSize;
	public ArrayList<Quartet> input;
	public ArrayList<Quartet> output;
	public int totalOverlap;
	
	/**
	 * Constructs a new Inference rule.
	 * 
	 * @param input The list of quartets needed for the rule to hold.
	 * @param output The list of quartets that hold.
	 */
	public Inference(ArrayList<Quartet> input, ArrayList<Quartet> output) {
		this.input = input;
		this.output = output;
		taxaSize = 0;
		for (Quartet q : input) {
			taxaSize = Math.max(taxaSize, q.left1+1);
			taxaSize = Math.max(taxaSize, q.left2+1);
			taxaSize = Math.max(taxaSize, q.right1+1);
			taxaSize = Math.max(taxaSize, q.right2+1);
		}
		for (Quartet q1 : input) {
			q1.totalOverlap = 0;
			for(Quartet q2 : input) {
				q1.totalOverlap += q1.overlap(q2);
			}
			q1.totalOverlap += 5*input.size()*q1.overlap(output.get(0));
		}
		input.sort((q1, q2) -> q2.totalOverlap-q1.totalOverlap);
		totalOverlap = 0;
		for (Quartet q : input) {
			totalOverlap += q.totalOverlap;
		}
	}

	static public ArrayList<Inference> read() {
		try {
			File f = new File("src/inference/inferenceRules.txt");
			Scanner reader = new Scanner(f);
			String temp = reader.nextLine();
			ArrayList<Inference> rules = new ArrayList<>();
			int n = Integer.parseInt(reader.nextLine());
			for(int i = 0; i < n; i++) {
				String s = reader.nextLine();
				String inputString = s.substring(0, s.indexOf("->"));
				String outputString = s.substring(s.indexOf("->")+3);
				int start = inputString.indexOf("[")+1;
				ArrayList<Quartet> inputQ = new ArrayList<>();
				while(true) {
					int next = inputString.indexOf(",", start);
					if(next == -1) {
						next = inputString.indexOf("]", start);
						String t = inputString.substring(start, next);
						String l = t.substring(t.indexOf("(")+1, t.indexOf("|"));
						String r = t.substring(t.indexOf("|")+1, t.indexOf(")"));
						int l1 = Integer.parseInt(l.substring(0, l.indexOf(" ")));
						int l2 = Integer.parseInt(l.substring(l.indexOf(" ")+1));
						int r1 = Integer.parseInt(r.substring(0, l.indexOf(" ")));
						int r2 = Integer.parseInt(r.substring(l.indexOf(" ")+1));
						Quartet q = new Quartet(l1, l2, r1, r2);
						inputQ.add(q);
						break;
					}
					String t = inputString.substring(start, next);
					String l = t.substring(t.indexOf("(")+1, t.indexOf("|"));
					String r = t.substring(t.indexOf("|")+1, t.indexOf(")"));
					int l1 = Integer.parseInt(l.substring(0, l.indexOf(" ")));
					int l2 = Integer.parseInt(l.substring(l.indexOf(" ")+1));
					int r1 = Integer.parseInt(r.substring(0, l.indexOf(" ")));
					int r2 = Integer.parseInt(r.substring(l.indexOf(" ")+1));
					Quartet q = new Quartet(l1, l2, r1, r2);
					inputQ.add(q);
					start = next+2;
				}
				ArrayList<Quartet> outputQ = new ArrayList<>();
				start = outputString.indexOf("[")+1;
				while(true) {
					int next = outputString.indexOf(",", start);
					if(next == -1) {
						next = outputString.indexOf("]", start);
						String t = outputString.substring(start, next);
						String l = t.substring(t.indexOf("(")+1, t.indexOf("|"));
						String r = t.substring(t.indexOf("|")+1, t.indexOf(")"));
						int l1 = Integer.parseInt(l.substring(0, l.indexOf(" ")));
						int l2 = Integer.parseInt(l.substring(l.indexOf(" ")+1));
						int r1 = Integer.parseInt(r.substring(0, l.indexOf(" ")));
						int r2 = Integer.parseInt(r.substring(l.indexOf(" ")+1));
						Quartet q = new Quartet(l1, l2, r1, r2);
						outputQ.add(q);
						break;
					}
					String t = outputString.substring(start, next);
					String l = t.substring(t.indexOf("(")+1, t.indexOf("|"));
					String r = t.substring(t.indexOf("|")+1, t.indexOf(")"));
					int l1 = Integer.parseInt(l.substring(0, l.indexOf(" ")));
					int l2 = Integer.parseInt(l.substring(l.indexOf(" ")+1));
					int r1 = Integer.parseInt(r.substring(0, l.indexOf(" ")));
					int r2 = Integer.parseInt(r.substring(l.indexOf(" ")+1));
					Quartet q = new Quartet(l1, l2, r1, r2);
					outputQ.add(q);
					start = next+2;
				}
				Inference inf = new Inference(inputQ, outputQ);
				rules.add(inf);
			}
			rules.sort((r1, r2) -> r1.input.size() != r2.input.size() ? r1.input.size()-r2.input.size() : r2.totalOverlap-r1.totalOverlap);
			return rules;
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

	public ArrayList<Quartet> use(ArrayList<Quartet> has, ArrayList<Witness> witnesses) {
		ArrayList<Quartet> added = new ArrayList<>();
		int tried = 0;
		Witness w = witnesses.get(0);
		while (tried < witnesses.size()) {
			for (Witness next : witnesses) {
				if (!next.tried) {
					w = next;
				}
			}
			boolean removed = false;
			//for (Quartet q : output) {
			Quartet q = output.get(0);
				int[] perm = new int[taxaSize];
				for (int i = 0; i < taxaSize; i++) {
					perm[i] = -1;
				}
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 4; j++) {
						for (int k = 0; k < 4; k++) {
							for (int l = 0; l < 4; l++) {
								if (i != j && i != k && i != l && j != k && j != l && k != l) {
									perm[q.left1] = w.taxa[i];
									perm[q.left2] = w.taxa[j];
									perm[q.right1] = w.taxa[k];
									perm[q.right2] = w.taxa[l];
									if (lookPerm(0, has, perm)) {
										added.add(new Quartet(w.taxa[i], w.taxa[j], w.taxa[k], w.taxa[l]));
										//tried -= w.removeConnected(witnesses);
										witnesses.remove(w);
										removed = true;
										break;
									}
								}
							}
							if (removed) {
								break;
							}
						}
						if (removed) {
							break;
						}
					}
					if (removed) {
						break;
					}
				}
				//if (removed) {
				//	break;
				//}
			//}
			if (witnesses.contains(w)) {
				w.tried = true;
				tried++;
			}
		}
		for (Witness triedW : witnesses) {
			if (!triedW.tried) {
				System.out.println("Witness checking went wrong");
			}
			triedW.tried = false;
		}
		return added;
	}
	
	private boolean lookPerm(int i, ArrayList<Quartet> has, int[] perm) {
		if(i == input.size()) {
			return true;
		}
		Quartet in = input.get(i);
		int[] l1 = new int[]{in.left1, in.left2, in.left1, in.left2, in.right1, in.right1, in.right2, in.right2};
		int[] l2 = new int[]{in.left2, in.left1, in.left2, in.left1, in.right2, in.right2, in.right1, in.right1};
		int[] r1 = new int[]{in.right1, in.right1, in.right2, in.right2, in.left1, in.left2, in.left1, in.left2};
		int[] r2 = new int[]{in.right2, in.right2, in.right1, in.right1, in.left2, in.left1, in.left2, in.left1};
		for(Quartet q : has) {
			for(int j = 0; j < 8; j++) {
				if((perm[l1[j]] == -1 || perm[l1[j]] == q.left1) && (perm[l2[j]] == -1 || perm[l2[j]] == q.left2) && 
				(perm[r1[j]] == -1 || perm[r1[j]] == q.right1) && (perm[r2[j]] == -1 || perm[r2[j]] == q.right2)) {
					boolean wasl1 = false;
					boolean wasl2 = false;
					boolean wasr1 = false;
					boolean wasr2 = false;
					if(perm[l1[j]] == -1) {
						perm[l1[j]] = q.left1;
						wasl1 = true;
					}
					if(perm[l2[j]] == -1) {
						perm[l2[j]] = q.left2;
						wasl2 = true;
					}
					if(perm[r1[j]] == -1) {
						perm[r1[j]] = q.right1;
						wasr1 = true;
					}
					if(perm[r2[j]] == -1) {
						perm[r2[j]] = q.right2;
						wasr2 = true;
					}
					boolean look = lookPerm(i+1, has, perm);
					if(wasl1) {
						perm[l1[j]] = -1;
					}
					if(wasl2) {
						perm[l2[j]] = -1;
					}
					if(wasr1) {
						perm[r1[j]] = -1;
					}
					if(wasr2) {
						perm[r2[j]] = -1;
					}
					if(look) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return input + " -> " + output;
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Inference)) {
			return false;
		}
		Inference i = (Inference) o;
		if(input.size() != i.input.size() || output.size() != i.output.size()) {
			return false;
		}
		for(Quartet q : input) {
			if(!i.input.contains(q)) {
				return false;
			}
		}
		for(Quartet q : output) {
			if(!i.output.contains(q)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for(Quartet q : input) {
			hash ^= q.hashCode();
		}
		for(Quartet q : output) {
			hash ^= q.hashCode();
		}
		return hash;
	}
}
