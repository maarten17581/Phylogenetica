package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.*;
import bep.fylogenetica.gui.CyclicOrderPanel;
import bep.fylogenetica.gui.ProgressUpdate;
import bep.fylogenetica.model.Quartet;
import bep.fylogenetica.model.Tree;

/**
 * This action executes step 1, 2, 3, 4 and 5 of the algorithm:
 * <ul>
 * <li>create the matrix;</li>
 * <li>reduce the matrix;</li>
 * <li>determine a vector from the space;</li>
 * <li>reconstruct the circular ordering;</li>
 * <li>finding the splits.</li>
 * </ul>
 */
public class AlgoStep5Action extends AbstractAction {
	
	Fylogenetica f;
	
	public AlgoStep5Action(Fylogenetica f) {
		super("Find splits");
		this.f = f;

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_5, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Step 5 of the algorithm: find the splits (they are printed to standard output). (This also executes steps 1, 2, 3 and 4.)");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		f.gui.tp.startTask("Determine list of splits");
		f.gui.tp.setDeterminate(false);
		
		final SwingWorker<Void, ProgressUpdate> worker = new SwingWorker<Void, ProgressUpdate>() {

			@Override
			protected Void doInBackground() throws Exception {
				
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
				ArrayList<int[]> n = Level1NetworkSplitFinder.findListOfSplits(c, v, m);
				
				// print the splits
				for (int[] split : n) {
					printSplit(split[0], split[1], c);
				}
				
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Ready"));
				
				return null;
			}
			
			@Override
			protected void process(List<ProgressUpdate> chunks) {
				for (ProgressUpdate update : chunks) {
					f.gui.tp.handleProgressUpdate(update);
				}
			}
			
			@Override
			protected void done() {
				
				f.gui.qp.updateList();
				f.gui.np.repaint();
				f.gui.tp.taskReady();
				
				setEnabled(true);
				
				Void n;
				
				try {
					
					n = get();
					
				} catch (InterruptedException ex) {
					
					f.gui.tp.handleException(ex.getCause());
					
				} catch (ExecutionException ex) {
					
					if (ex.getCause() instanceof MatrixInconsistentException) {
						f.gui.tp.handleAbort("Inconsistent matrix");
						JOptionPane.showMessageDialog(f.gui, "<html><body style='width: 350px;'>" +
								"<big>The matrix is inconsistent</big><br>" +
								"<p>This means that your quartet input contains conflicting quartets. " +
								"For example, this error appears if you include all three of the " +
								"following quartets:" +
								"<ul>" +
								"<li>0 1 | 2 3</li>" +
								"<li>0 2 | 1 3</li>" +
								"<li>0 3 | 1 2</li>" +
								"</ul>" +
								"This happens since there is no solution in this case.</p>",
								"Algorithm failed", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (ex.getCause() instanceof NotCyclicException) {
						f.gui.tp.handleAbort("Non-cyclic vector");
						JOptionPane.showMessageDialog(f.gui, "<html><body style='width: 350px;'>" +
								"<big>Vector was not cyclic</big><br>" +
								"<p>This means that the vector we chose from the matrix was not cyclic. " +
								"That signifies that you have not enough data. For example, consider the " +
								"case where you only give the following quartet:" +
								"<ul>" +
								"<li>0 1 | 2 3</li>" +
								"</ul>" +
								"Now there is not enough data to determine an ordering.</p>",
								"Algorithm failed", JOptionPane.ERROR_MESSAGE);
						return;
					}
					f.gui.tp.handleException(ex.getCause());
				}
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
	
	private void printSplit(int start, int end, CyclicOrder c) {
		System.out.print("{");
		
		for (int i = start; i < end; i++) {
			System.out.print(c.taxa.get(i));
			
			if (i < end - 1) {
				System.out.print(", ");
			}
		}
		
		System.out.println("}");
	}
}
