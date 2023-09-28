package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.*;
import bep.fylogenetica.gui.CyclicOrderPanel;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.gui.ProgressUpdate;
import bep.fylogenetica.model.Network;
import bep.fylogenetica.model.Quartet;
import bep.fylogenetica.model.Tree;
import bep.fylogenetica.model.Witness;
import bep.fylogenetica.model.Inference;

/**
 * This action executes the entire algorithm:
 * <ul>
 * <li>create the matrix;</li>
 * <li>reduce the matrix;</li>
 * <li>determine a vector from the space;</li>
 * <li>reconstruct the circular ordering;</li>
 * <li>finding the splits <i>(for trees only).</li>
 * </ul>
 */
public class ReconstructTreeAction extends AbstractAction {
	
	Fylogenetica f;
	
	public ReconstructTreeAction(Fylogenetica f) {
		super("Reconstruct tree");
		this.f = f;
		
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Reconstructs the tree from the quartets. This was an attempt to create a faster algorithm for trees only, but it turns out it is slower than the generic algorithm for networks.");
		putValue(SMALL_ICON, IconHandler.getIcon("arrow-right-double-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("arrow-right-double-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		f.gui.tp.startTask("Reconstruct tree");
		f.gui.tp.setDeterminate(false);

		String countS = JOptionPane.showInputDialog(f.gui, "<html>What maximum inference input size do you use" +
				"<p>Choosing 1 means not using inference rules</p>", 1);
		
		if (countS == null) {
			return;
		}
		
		int count;
		
		try {
			count = Integer.valueOf(countS);
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(f.gui, "This is not a valid integer...");
			return;
		}
		
		final SwingWorker<Tree, ProgressUpdate> worker = new SwingWorker<Tree, ProgressUpdate>() {

			@Override
			protected Tree doInBackground() throws Exception {
				
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

				if (c == null) {
					c = new CyclicOrder(f.model.taxonCount);
					witnessHandling(startTime, m, v, c);
				}

				// step 5
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Finding splits"));
				Tree t = TreeSplitFinder.findSplits(c, v, m);

				if(t == null) {
					witnessHandling(startTime, m, v, c);

					// step 5
					publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Finding splits"));
					t = TreeSplitFinder.findSplits(c, v, m);
				}

				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Ready"));
				
				// display the tree
				f.model.setNetwork(t.toNetwork());
				
				// return the tree
				return t;
			}

			void witnessHandling(long startTime, GF2Matrix m, DenseVector v, CyclicOrder c) throws NotCyclicException, MatrixInconsistentException {
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Witness found, finding rest"));
				ArrayList<Inference> usable = new ArrayList<>();
				for (Inference inf : f.model.inferences) {
					if (inf.input.size() <= count) {
						usable.add(inf);
					}
				}
				ArrayList<Witness> witnesses = m.findWitnesses();
				if (witnesses == null) {
					throw new NotCyclicException();
				}
				Witness.makeGraph(witnesses);
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, witnesses.size()+" witnesses found. Now using inference"));
				//System.out.println(witnesses);
				//System.out.println(c);
				ArrayList<Quartet> added = new ArrayList<>();
				long time = System.currentTimeMillis();
				for (int i = 0; i < usable.size(); i++) {
					long now = System.currentTimeMillis();
					//System.out.println(i + " inference " + (now-time));
					time = now;
					Inference inf = usable.get(i);
					ArrayList<Quartet> add = inf.use(f.model.quartets, witnesses);
					if (add.size() > 0) {
						added.addAll(add);
						i = 0;
						System.out.println(add);
						f.model.quartets.addAll(add);
					}
					
					if (witnesses.isEmpty()) {
						witnesses = m.findWitnesses();
						System.out.println("witnesses were empty??");
						if (witnesses.isEmpty()) {
							break;
						}
						Witness.makeGraph(witnesses);
					}
				}

				// step 1
				publish(new ProgressUpdate(0, "Recreating matrix"));
				for (Quartet q : added) {
					m.addRowForQuartet(q);
				}

				// step 2
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Reducing matrix"));
				m.rowReduce(false);

				//System.out.println(added + " added");
				if (witnesses.size() > 0) {
					throw new NotCyclicException();
				}

				// step 3
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Determine conforming vector"));
				v.changeAll(m.determineConformingVector());

				// step 4
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Reconstructing ordering"));
				c.changeAll(v.determineOrder());

				witnesses = m.findWitnesses();
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
				
				Tree t;
				
				try {
					
					t = get();

					Network n = t.toNetwork();
					n.randomizePositions(f.gui.np.getWidth(), f.gui.np.getHeight());
					f.model.setNetwork(n);
					f.gui.qp.updateList();
					f.gui.np.triggerLayout();
					
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
}
