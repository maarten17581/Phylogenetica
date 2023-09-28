package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.MatrixInconsistentException;
import bep.fylogenetica.algorithm.NotCyclicException;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.gui.ProgressUpdate;
import bep.fylogenetica.model.Level1Network;
import bep.fylogenetica.model.Network;
import bep.fylogenetica.model.Tree;

/**
 * Creates a new random tree over the taxa.
 */
public class RandomTreeAction extends AbstractAction {
	
	Fylogenetica f;
	
	public RandomTreeAction(Fylogenetica f) {
		super("Random tree");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Creates a random tree and generates its quartets.");
        putValue(SMALL_ICON, IconHandler.getIcon("roll-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("roll-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (!f.model.quartets.isEmpty()) {
			int response = JOptionPane.showConfirmDialog(f.gui,
					"There are already quartets in the list. They will be overwritten " +
					"by the quartets of the generated tree. Are you sure you want this?",
					"Overwrite quartets",
					JOptionPane.YES_NO_OPTION);
			
			if (response != JOptionPane.YES_OPTION) {
				return;
			}
		}
		
		f.gui.tp.startTask("Generate random tree");
		
		final SwingWorker<Network, ProgressUpdate> worker = new SwingWorker<Network, ProgressUpdate>() {

			@Override
			protected Network doInBackground() throws Exception {

				long startTime = System.currentTimeMillis();

				publish(new ProgressUpdate(0, "Generating tree"));

				Tree t = Tree.generateRandomTree(f.model.taxonCount);

				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Enumerating quartets"));
				
				f.model.quartets = t.getQuartets(f.model.taxonCount);

				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Converting tree to graph"));
				
				Network n = t.toNetwork();
				
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Ready"));
				
				return n;
			}
			
			@Override
			protected void process(List<ProgressUpdate> chunks) {
				for (ProgressUpdate update : chunks) {
					f.gui.tp.handleProgressUpdate(update);
				}
			}
			
			@Override
			protected void done() {
				
				f.gui.tp.taskReady();
				
				Network n;
				try {
					n = get();
				} catch (InterruptedException ex) {
					f.gui.tp.handleException(ex.getCause());
					return;
				} catch (ExecutionException ex) {
					f.gui.tp.handleException(ex.getCause());
					return;
				}
				
				n.randomizePositions(f.gui.np.getWidth(), f.gui.np.getHeight());
				f.model.setNetwork(n);
				f.gui.qp.updateList();
				f.gui.np.triggerLayout();
			}
		};
		
		worker.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				f.gui.tp.sb.setProgress(worker.getProgress());
				f.gui.np.repaint();
			}
		});
		
		worker.execute();
	}
}
