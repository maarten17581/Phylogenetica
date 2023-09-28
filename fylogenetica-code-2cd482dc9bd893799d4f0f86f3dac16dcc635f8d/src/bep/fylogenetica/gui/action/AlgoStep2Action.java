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
import bep.fylogenetica.model.Quartet;

/**
 * This action executes step 1 and 2 of the algorithm:
 * <ul>
 * <li>create the matrix;</li>
 * <li>reduce the matrix.</li>
 * </ul>
 */
public class AlgoStep2Action extends AbstractAction {
	
	Fylogenetica f;
	
	public AlgoStep2Action(Fylogenetica f) {
		super("Reduce matrix");
		this.f = f;

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Step 2 of the algorithm: reduce the matrix. (This also executes step 1.)");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		f.gui.tp.sb.showBar("Starting algorithm...");
		f.gui.tp.sb.setDeterminate(false);
		
		final SwingWorker<GF2Matrix, String> worker = new SwingWorker<GF2Matrix, String>() {

			@Override
			protected GF2Matrix doInBackground() throws Exception {
				
				// step 1
				publish("Creating matrix...");
				GF2Matrix m = new GF2MatrixDense(f.model.taxonCount);
				
				for (Quartet q : f.model.quartets) {
					m.addRowForQuartet(q);
				}

				// step 2
				publish("Reducing matrix...");
				m.rowReduce(false);
				
				return m;
			}
			
			@Override
			protected void process(List<String> chunks) {
				f.gui.tp.sb.setTask(chunks.get(chunks.size() - 1));
			}
			
			@Override
			protected void done() {
				
				f.gui.qp.updateList();
				f.gui.tp.sb.hideBar();
				
				GF2Matrix m;
				try {
					m = get();
				} catch (InterruptedException | ExecutionException ex) {
					ex.printStackTrace();
					return;
				}
				
				JOptionPane.showMessageDialog(f.gui, new JLabel(new ImageIcon(m.toImage())));
				
				setEnabled(true);
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
