package n7.HagiMule.Client.Tui.Controls;

import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import java.io.File;
import n7.HagiMule.Client.Tui.Tui;
import n7.HagiMule.Client.Tui.TuiComponent;

public class Upload implements TuiComponent {
    Tui tui;

    public Upload(Tui tui) {
        this.tui = tui;
    }

    public Component getComponent() {
        return new Button(
                "Upload",
                new Runnable() {
                    @Override
                    public void run() {
                        uploadFile();
                    }
                });
    }

    private void uploadFile() {
        File input = new FileDialogBuilder()
                .setTitle("Open File")
                .setDescription("Choose a file")
                .setActionLabel("Open")
                .build()
                .showDialog(tui.textGUI);

        if (input == null)
            return;

        tui.daemon.addFichier(input.getPath());
    }
}
