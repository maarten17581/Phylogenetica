package bep.fylogenetica.gui.action;

import javax.swing.Action;

import bep.fylogenetica.Fylogenetica;

/**
 * This class is a repository for all actions.
 */
public class Actions {

	public Action open;
	public Action save;
	public Action quickSave;
	public Action exportGraphAsIpe;
	public Action quit;
	public Action addQuartet;
	public Action editQuartet;
	public Action removeQuartet;
	public Action addRandomQuartets;
	public Action removeRandomQuartets;
	public Action shuffleQuartets;
	public Action reconstructTree;
	public Action reconstructNetwork;
	public Action reconstructNetworkM4RI;
	public Action algoStep1;
	public Action algoStep2;
	public Action algoStep3;
	public Action algoStep4;
	public Action algoStep5;
	public Action randomTree;
	public Action layoutNetwork;
	public Action editTaxonCount;
	public Action testM4RI;
	public Action about;
	
	/**
	 * Creates a new {@link Actions} object.
	 * 
	 * @param f The {@link Fylogenetica} application the actions in this repository
	 * belong to.
	 */
	public Actions(Fylogenetica f) {
		open = new OpenAction(f);
		save = new SaveAction(f);
		quickSave = new QuickSaveAction(f);
		exportGraphAsIpe = new ExportGraphAsIpeAction(f);
		quit = new QuitAction(f);
		editTaxonCount = new EditTaxonCountAction(f);
		addQuartet = new AddQuartetAction(f);
		removeQuartet = new RemoveQuartetAction(f);
		addRandomQuartets = new AddRandomQuartetsAction(f);
		removeRandomQuartets = new RemoveRandomQuartetsAction(f);
		shuffleQuartets = new ShuffleQuartetsAction(f);
		reconstructTree = new ReconstructTreeAction(f);
		reconstructNetwork = new ReconstructNetworkAction(f);
		algoStep1 = new AlgoStep1Action(f);
		algoStep2 = new AlgoStep2Action(f);
		algoStep3 = new AlgoStep3Action(f);
		algoStep4 = new AlgoStep4Action(f);
		algoStep5 = new AlgoStep5Action(f);
		randomTree = new RandomTreeAction(f);
		layoutNetwork = new LayoutNetworkAction(f);
		editQuartet = new EditQuartetAction(f);
		about = new AboutAction(f);
	}
}
