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
public class RemoveRandomQuartetsAction extends AbstractAction {
	
	Fylogenetica f;
	
	public RemoveRandomQuartetsAction(Fylogenetica f) {
		super("Remove random quartets");
		this.f = f;
		
        putValue(SHORT_DESCRIPTION, "Removes a specified number of random quartets over the taxa.");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String countS = JOptionPane.showInputDialog(f.gui, "How many quartets to Remove?", f.model.quartets.size()/2);
	
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
		if (count > f.model.quartets.size()) {
			JOptionPane.showMessageDialog(f.gui, "This number is more then the amount of quartets...");
			return;
		}
		
		setEnabled(false);
		
		f.gui.tp.sb.showBar("Removing random quartets...");
		f.gui.tp.sb.setDeterminate(false);
		
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				
				Random r = new Random();
				
				for (int i = 0; i < count; i++) {
					f.model.quartets.remove(r.nextInt(f.model.quartets.size()));
					
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
