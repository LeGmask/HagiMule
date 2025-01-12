package n7.HagiMule.Client.Tui.Controls;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.DirectoryDialogBuilder;
import java.io.File;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import n7.HagiMule.Client.Tui.Tui;
import n7.HagiMule.Client.Tui.TuiComponent;
import n7.HagiMule.Shared.FileInfo;

public class Download implements TuiComponent {
    Tui tui;

    public Download(Tui tui) {
        this.tui = tui;
    }

    public Component getComponent() {
        return new Button(
                "Download",
                new Runnable() {
                    @Override
                    public void run() {
                        downloadFiles();
                    }
                });
    }

    private void downloadFiles() {
        BasicWindow window = new BasicWindow();
        ActionListBox actionListBox = new ActionListBox();
        try {
            for (FileInfo fileInfo : tui.index.SearchFile("")) {
                actionListBox.addItem(
                        fileInfo.getNom(),
                        new Runnable() {
                            @Override
                            public void run() {
                                downloadFile(fileInfo);
                                tui.textGUI.removeWindow(window);
                            }
                        });
            }
            actionListBox.addItem(
                    "Cancel",
                    new Runnable() {
                        @Override
                        public void run() {
                            tui.textGUI.removeWindow(window);
                        }
                    });
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        window.setComponent(actionListBox);
        tui.textGUI.addWindowAndWait(window);
    }

    private void downloadFile(FileInfo fileInfo) {
        //
        File input =
            new DirectoryDialogBuilder()
            .setTitle("Select directory")
            .setDescription("Choose a directory")
            .setActionLabel("Select")
            .build()
            .showDialog(tui.textGUI);
        
        if (input != null) {
            int id = tui.downloader.submit(fileInfo, Paths.get(input.getPath(), fileInfo.getNom()).toString());
            tui.downloads.addDownload(id);
            // close the window
            // downloads.addComponent(
            // new Label(fileInfo.getNom()));
            // ProgressBar pB = new ProgressBar();
            // pB.setPreferredWidth(50);
            // downloads.addComponent(pB);
    
            // textGUI.removeWindow(window);
        }

    }
}
