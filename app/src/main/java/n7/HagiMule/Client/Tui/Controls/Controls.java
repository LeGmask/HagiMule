package n7.HagiMule.Client.Tui.Controls;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Panel;
import n7.HagiMule.Client.Tui.Tui;
import n7.HagiMule.Client.Tui.TuiComponent;

public class Controls implements TuiComponent {
	Tui tui;

	public Controls(Tui tui) {
		this.tui = tui;
	}

	public Component getComponent() {
		Panel panel = new Panel();
		panel.addComponent(new Resume(tui).getComponent());
		panel.addComponent(new Pause(tui).getComponent());
		panel.addComponent(new Upload(tui).getComponent());
		panel.addComponent(new Download(tui).getComponent());
		return panel;
	}
}
