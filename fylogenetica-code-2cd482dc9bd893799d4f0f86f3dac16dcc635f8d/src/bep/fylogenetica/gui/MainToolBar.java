package bep.fylogenetica.gui;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.action.Actions;

/**
 * The main tool bar of the application.
 * 
 * <p>The tool bar uses the {@link Action} objects from the {@link Actions} class.</p>
 */
public class MainToolBar extends JToolBar {
	
	/**
	 * The {@link Fylogenetica} object.
	 */
	Fylogenetica f;
	
	/**
	 * Creates the tool bar.
	 * 
	 * @param f The {@link Fylogenetica} application.
	 * @param actions The {@link Actions} element to use for this tool bar. Note: this
	 * is actually a hack, since this would just be <code>f.gui.actions</code>. However
	 * since this constructor is called during the construction of <code>f.gui</code>,
	 * this reference is still <code>null</code>. Therefore the <code>actions</code> is
	 * given separately.
	 */
	public MainToolBar(Fylogenetica f, Actions actions) {
		
		this.f = f;
		
		add(new JButton(actions.open));
		add(new JButton(actions.save));
		
		addSeparator();
		
		add(new JButton(actions.reconstructNetwork));
		add(new JButton(actions.reconstructTree));
		
		addSeparator();

		add(new JButton(actions.randomTree));
		add(new JButton(actions.layoutNetwork));
	}
}
