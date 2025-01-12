package n7.HagiMule.Client.Tui.Controls;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import n7.HagiMule.Client.Tui.Tui;
import n7.HagiMule.Client.Tui.TuiComponent;

public class Pause implements TuiComponent {
    Tui tui;

    public Pause(Tui tui) {
        this.tui = tui;
    }

    public Component getComponent() {
        return new Button(
                "Pause all",
                new Runnable() {
                    @Override
                    public void run() {
                        tui.downloader.pauseAll();
                    }
                });
    }
}
