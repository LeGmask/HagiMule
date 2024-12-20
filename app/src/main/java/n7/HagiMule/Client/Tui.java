package n7.HagiMule.Client;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.ProgressBar;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;
import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.FileInfo;

public class Tui {
    private Daemon daemon;
    private Diary index;
    private Downloader downloader;

    public Tui(Diary index, Daemon daemon, Downloader downloader) throws IOException {
        this.index = index;
        this.daemon = daemon;
        this.downloader = downloader;

        this.initializeTui();
    }

    public void initializeTui() throws IOException {
        // Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        // Setup WindowBasedTextGUI for dialogs
        final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

        // Create panel to hold components
        Panel panel = new Panel();
        Panel downloads = new Panel();

        panel.addComponent(
                new Button(
                        "Upload",
                        new Runnable() {
                            @Override
                            public void run() {
                                File input =
                                        new FileDialogBuilder()
                                                .setTitle("Open File")
                                                .setDescription("Choose a file")
                                                .setActionLabel("Open")
                                                .build()
                                                .showDialog(textGUI);
                                daemon.addFichier(input.getPath());
                            }
                        }));

        panel.addComponent(
                new Button(
                        "Download",
                        new Runnable() {
                            @Override
                            public void run() {
                                BasicWindow window = new BasicWindow();
                                ActionListBox actionListBox = new ActionListBox();
                                try {
                                    for (FileInfo fileInfo : index.SearchFile("")) {
                                        actionListBox.addItem(
                                                fileInfo.getNom(),
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        System.out.println(
                                                                "Downloading " + fileInfo.getNom());
                                                        int dID =
                                                                downloader.submit(
                                                                        fileInfo,
                                                                        fileInfo.getNom());
                                                        // close the window
                                                        downloads.addComponent(
                                                                new Label(fileInfo.getNom()));
                                                        ProgressBar pB = new ProgressBar();
                                                        pB.setPreferredWidth(50);
                                                        downloads.addComponent(pB);
                                                        Timer t = new Timer();
                                                        t.schedule(
                                                                new TimerTask() {
                                                                    @Override
                                                                    public void run() {
                                                                        pB.setValue(
                                                                                (downloader
                                                                                        .get(dID)
                                                                                        .getProgress()));
                                                                    }
                                                                },
                                                                1,
                                                                100);
                                                        textGUI.removeWindow(window);
                                                    }
                                                });
                                    }
                                } catch (RemoteException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                                window.setComponent(actionListBox);
                                textGUI.addWindowAndWait(window);
                            }
                        }));

        // add download panel to main panel
        panel.addComponent(downloads);

        // Create window to hold the panel
        BasicWindow window = new BasicWindow();
        window.setComponent(panel);

        textGUI.addWindowAndWait(window);
    }
}
