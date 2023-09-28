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
import bep.fylogenetica.algorithm.GF2Matrix;
import bep.fylogenetica.algorithm.GF2MatrixDense;
import bep.fylogenetica.algorithm.ProgressListener;
import bep.fylogenetica.gui.MainMenuBar;
import bep.fylogenetica.gui.ProgressUpdate;
import bep.fylogenetica.model.Quartet;

/**
 * This action executes step 1 of the algorithm:
 * <ul>
 * <li>create the matrix.</li>
 * </ul>
 */
public class AlgoStep1Action extends AbstractAction {
	
	Fylogenetica f;
	
	public AlgoStep1Action(Fylogenetica f) {
		super("Create matrix");
		this.f = f;

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Step 1 of the algorithm: create the matrix.");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		f.gui.tp.startTask("Algorithm step 1");
		f.gui.tp.setDeterminate(false);
		
		final SwingWorker<GF2Matrix, ProgressUpdate> worker = new SwingWorker<GF2Matrix, ProgressUpdate>() {

			@Override
			protected GF2Matrix doInBackground() throws Exception {
				
				long startTime = System.currentTimeMillis();
				
				// step 1
				publish(new ProgressUpdate(0, "Creating matrix"));
				GF2Matrix m = new GF2MatrixDense(f.model.taxonCount);
				
				for (Quartet q : f.model.quartets) {
					m.addRowForQuartet(q);
				}
				
				publish(new ProgressUpdate(System.currentTimeMillis() - startTime, "Ready"));
				
				return m;
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
				
				GF2Matrix m;
				try {
					m = get();
				} catch (InterruptedException | ExecutionException ex) {
					f.gui.tp.handleException(ex.getCause());
					return;
				}
				
				JOptionPane.showMessageDialog(f.gui, new JLabel(new ImageIcon(m.toImage())));
				
				
				f.gui.qp.updateList();
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
