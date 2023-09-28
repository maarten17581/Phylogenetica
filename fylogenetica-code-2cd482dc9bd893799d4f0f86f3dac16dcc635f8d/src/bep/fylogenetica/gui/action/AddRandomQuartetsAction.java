package bep.fylogenetica.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.ProgressListener;
import bep.fylogenetica.gui.MainMenuBar;
import bep.fylogenetica.model.Quartet;

/**
 * This action creates random quartets.
 */
public class AddRandomQuartetsAction extends AbstractAction {
	
	Fylogenetica f;
	
	public AddRandomQuartetsAction(Fylogenetica f) {
		super("Add random quartets");
		this.f = f;
		
        putValue(SHORT_DESCRIPTION, "Adds a specified number of random quartets over the taxa.");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String countS = JOptionPane.showInputDialog(f.gui, "How many quartets to add?", 100);
	
		if (countS == null) {
			return;
		}
		
		final int count;
		
		try {
			count = Integer.valueOf(countS);
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(f.gui, "This is not a valid integer...");
			return;
		}
		
		setEnabled(false);
		
		f.gui.tp.sb.showBar("Adding random quartets...");
		f.gui.tp.sb.setDeterminate(false);
		
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				
				Random r = new Random();
				
				for (int i = 0; i < count; i++) {
					int r1, r2, r3, r4;
					r1 = r.nextInt(f.model.taxonCount);
					do {
						r2 = r.nextInt(f.model.taxonCount - 1) + 1;
					} while (r2 == r1);
					do {
						r3 = r.nextInt(f.model.taxonCount - 1) + 1;
					} while (r3 == r1 || r3 == r2);
					do {
						r4 = r.nextInt(f.model.taxonCount - 1) + 1;
					} while (r4 == r1 || r4 == r2 || r4 == r3);
					f.model.quartets.add(new Quartet(r1, r2, r3, r4));
					
					if (i % 10000 == 0) {
						setProgress(i / count);
					}
				}
				
				return null;
			}
			
			@Override
			protected void done() {
				f.gui.qp.updateList();
				f.gui.tp.sb.hideBar();
				
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
