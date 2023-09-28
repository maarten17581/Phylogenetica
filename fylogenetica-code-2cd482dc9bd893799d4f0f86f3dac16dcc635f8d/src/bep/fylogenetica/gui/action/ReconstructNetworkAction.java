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
import bep.fylogenetica.algorithm.*;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.gui.ProgressUpdate;
import bep.fylogenetica.model.Level1Network;
import bep.fylogenetica.model.Network;
import bep.fylogenetica.model.Quartet;

/**
 * This action executes the entire algorithm:
 * <ul>
 * <li>create the matrix;</li>
 * <li>reduce the matrix;</li>
 * <li>determine a vector from the space;</li>
 * <li>reconstruct the circular ordering;</li>
 * <li>finding the splits <i>(also for level-1-networks).</li>
 * </ul>
 */
public class ReconstructNetworkAction extends AbstractAction {
	
	Fylogenetica f;
	
	public ReconstructNetworkAction(Fylogenetica f) {
		super("Reconstruct network");
		this.f = f;

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Reconstructs the level-1-network from the quartets.");
        putValue(SMALL_ICON, IconHandler.getIcon("arrow-right-double-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("arrow-right-double-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		f.gui.tp.startTask("Reconstruct level-1-network");
		f.gui.tp.setDeterminate(false);
		
		final SwingWorker<Network, ProgressUpdate> worker = new SwingWorker<Network, ProgressUpdate>() {

			@Override
			protected Network doInBackground() throws Exception {
				
				long startTime = System.currentTimeMillis();
				
				// step 1
				publish(new ProgressUpdate(0, "Creating matrix"));
				GF2Matrix m = new GF2MatrixDense(f.model.taxonCount);
				
				for (Quartet q : f.model.quartets) {
					m.addRowForQuartet(q);
				}

				// step 2
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Reducing matrix"));
				m.rowReduce(false);

				// step 3
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Determine conforming vector"));
				DenseVector v = m.determineConformingVector();
				
				// step 4
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Reconstructing ordering"));
				CyclicOrder c = v.determineOrder();
				if(c == null) {
					throw new NotCyclicException();
				}
				
				// step 5
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Finding splits"));
				Level1Network n = Level1NetworkSplitFinder.reconstructNetwork(c, v, m);

				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Ready"));
				
				// return the network
				return n.toNetwork();
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
				
				setEnabled(true);
				
				Network n;
				
				try {
					n = get();
				} catch (InterruptedException ex) {
					f.gui.tp.handleException(ex.getCause());
					return;
				} catch (ExecutionException ex) {
					
					if (ex.getCause() instanceof MatrixInconsistentException) {
						f.gui.tp.handleAbort("Inconsistent matrix");
						JOptionPane.showMessageDialog(f.gui, "<html><body style='width: 350px;'>" +
								"<big>The matrix is inconsistent</big><br>" +
								"<p>This means that your quartet input contains conflicting quartets.</p>",
								"Algorithm failed", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (ex.getCause() instanceof NotCyclicException) {
						f.gui.tp.handleAbort("Non-cyclic vector");
						JOptionPane.showMessageDialog(f.gui, "<html><body style='width: 350px;'>" +
								"<big>Non-cyclic vectors detected</big><br>" +
								"<p>This means there is not enough data to determine an ordering. " +
								"Note: this error never occurs if there are 'enough' quartets, that " +
								"is, if on every five taxa, there are at least four quartets.</p>",
								"Algorithm failed", JOptionPane.ERROR_MESSAGE);
						return;
					}
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
			}
		});
		
		worker.execute();
	}
}
