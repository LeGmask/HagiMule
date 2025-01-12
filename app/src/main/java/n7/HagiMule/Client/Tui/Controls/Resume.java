package n7.HagiMule.Client.Tui.Controls;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import n7.HagiMule.Client.Tui.Tui;
import n7.HagiMule.Client.Tui.TuiComponent;

public class Resume implements TuiComponent {
    Tui tui;

    public Resume(Tui tui) {
        this.tui = tui;
    }

    public Component getComponent() {
        return new Button(
                "Resume all",
                new Runnable() {
                    @Override
                    public void run() {
                        tui.downloader.resumeAll();
                    }
                });
    }
}
