package bep.fylogenetica.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Witness {
	
	public int[] taxa;

	public ArrayList<Witness> connected;

	public boolean tried = false;

	public static void makeGraph(ArrayList<Witness> witnesses) {
		for (Witness w : witnesses) {
			w.connected = new ArrayList<>();
		}
		for (Witness w1 : witnesses) {
			for (Witness w2 : witnesses) {
				if (w1 != w2 && w1.isPair(w2)) {
					w1.connected.add(w2);
				}
			}
		}
	}

	public Witness(int firstTaxon, int secondTaxon, int thirdTaxon, int forthTaxon) {
		connected = new ArrayList<>();
		taxa = new int[]{firstTaxon, secondTaxon, thirdTaxon, forthTaxon};
	}

	public Witness(int[] taxa) {
		connected = new ArrayList<>();
		this.taxa = taxa;
	}

	public Witness(Witness w, int remove, int add) {
		taxa = new int[4];
		for (int i = 0; i < 4; i++) {
			if (w.taxa[i] != remove) {
				taxa[i] = w.taxa[i];
			} else {
				taxa[i] = add;
			}
		}
		toCanonicalForm();
	}

	public int removeConnected(ArrayList<Witness> witnesses) {
		witnesses.remove(this);
		int sum = tried ? 1 : 0;
		for (Witness w : connected) {
			if (witnesses.contains(w)) {
				sum += w.removeConnected(witnesses);
			}
		}
		return sum;
	}

	public void toCanonicalForm() {
		Arrays.sort(taxa);
	}

	public ArrayList<Integer> getTaxa() {
		toCanonicalForm();
		ArrayList<Integer> taxaList = new ArrayList<>();
		taxaList.add(taxa[0]);
		taxaList.add(taxa[1]);
		taxaList.add(taxa[2]);
		taxaList.add(taxa[3]);
		return taxaList;
	}

	public ArrayList<Integer> connect(ArrayList<Integer> taxaList) {
		ArrayList<Integer> thisTaxaList = getTaxa();
		ArrayList<Integer> out = new ArrayList<>();
		while (!taxaList.isEmpty() || !thisTaxaList.isEmpty()) {
			if (taxaList.get(0) < thisTaxaList.get(0)) {
				out.add(taxaList.remove(0));
			} else if (thisTaxaList.get(0) < taxaList.get(0)) {
				out.add(thisTaxaList.remove(0));
			} else if (taxaList.get(0) == thisTaxaList.get(0)) {
				out.add(taxaList.remove(0));
				thisTaxaList.remove(0);
			}
		}
		out.addAll(taxaList);
		out.addAll(thisTaxaList);
		return out;
	}

	/**
	 * Requires this and w to be in canonical form
	 * @param w The witness to compare it to
	 * @return If there is an overlap of 3 taxa and thus they form a pair on the 5 taxa they consist on
	 */
	public boolean isPair(Witness w) {
		int overlap = 0;
		int x = 0;
		int y = 0;
		while (x < 4 && y < 4) {
			if (taxa[x] == w.taxa[y]) {
				overlap++;
				x++;
				y++;
			} else if (taxa[x] < w.taxa[y]) {
				x++;
			} else if (taxa[x] > w.taxa[y]) {
				y++;
			}
		}
		return overlap == 3;
	}

	public boolean contains(int taxon) {
		return taxa[0] == taxon || taxa[1] == taxon || taxa[2] == taxon || taxa[3] == taxon;
	}

	@Override
	public String toString() {
		return Arrays.toString(taxa);
	}

}
