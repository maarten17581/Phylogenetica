package bep.fylogenetica.model;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import bep.fylogenetica.Fylogenetica;

/**
 * This class is a collection of all data used in the application.
 */
public class MainModel {
	
	/**
	 * The amount of taxa.
	 */
	public int taxonCount = 40;
	
	/**
	 * The set of quartets.
	 */
	public ArrayList<Quartet> quartets;

	/**
	 * The set of inference rules.
	 */
	public ArrayList<Inference> inferences;
	
	/**
	 * The network shown in the application.
	 */
	private Network network;
	
	private Fylogenetica f;
	
	/**
	 * Creates and initializes a {@link MainModel}.
	 * @param f The {@link Fylogenetica} object.
	 */
	public MainModel(Fylogenetica f) {
		this.f = f;
		quartets = new ArrayList<>();
		inferences = Inference.read();
		setNetwork(null);
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}
}
