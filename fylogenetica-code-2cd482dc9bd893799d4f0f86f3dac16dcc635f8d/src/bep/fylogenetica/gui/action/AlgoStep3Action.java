package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.*;
import bep.fylogenetica.gui.MainMenuBar;
import bep.fylogenetica.model.Quartet;

/**
 * This action executes step 1, 2 and 3 of the algorithm:
 * <ul>
 * <li>create the matrix;</li>
 * <li>reduce the matrix;</li>
 * <li>determine a vector from the space.</li>
 * </ul>
 */
public class AlgoStep3Action extends AbstractAction {
	
	Fylogenetica f;
	
	public AlgoStep3Action(Fylogenetica f) {
		super("Determine vector");
		this.f = f;

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Step 3 of the algorithm: determine a vector in the space. (This also executes steps 1 and 2.)");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		f.gui.tp.sb.showBar("Starting algorithm...");
		f.gui.tp.sb.setDeterminate(false);
		
		final SwingWorker<DenseVector, String> worker = new SwingWorker<DenseVector, String>() {

			@Override
			protected DenseVector doInBackground() throws Exception {
				
				// step 1
				publish("Creating matrix...");
				GF2Matrix m = new GF2MatrixDense(f.model.taxonCount);
				
				for (Quartet q : f.model.quartets) {
					m.addRowForQuartet(q);
				}

				// step 2
				publish("Reducing matrix...");
				m.rowReduce(false);
				
				// step 3
				publish("Determine conforming vector...");
				DenseVector v = m.determineConformingVector();
				
				return v;
			}
			
			@Override
			protected void process(List<String> chunks) {
				f.gui.tp.sb.setTask(chunks.get(chunks.size() - 1));
			}
			
			@Override
			protected void done() {
				
				f.gui.qp.updateList();
				f.gui.tp.sb.hideBar();
				
				setEnabled(true);
				
				DenseVector v;
				
				try {
					
					v = get();
					
				} catch (InterruptedException ex) {
					
					throw new RuntimeException("InterruptedException during algorithm", ex);
					
				} catch (ExecutionException ex) {
					
					if (ex.getCause() instanceof MatrixInconsistentException) {
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
					throw new RuntimeException("Exception during algorithm", ex.getCause());
				}

				JOptionPane.showMessageDialog(f.gui, new JLabel(v.toString()));
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
