package n7.HagiMule.Client.Tui;

import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Separator;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;

import org.checkerframework.checker.units.qual.s;

import n7.HagiMule.Client.Daemon;
import n7.HagiMule.Client.Downloader;
import n7.HagiMule.Client.Tui.Controls.Controls;
import n7.HagiMule.Diary.Diary;

public class Tui {
    public Daemon daemon;
    public Diary index;
    public Downloader downloader;

    Terminal terminal = new DefaultTerminalFactory().createTerminal();
    Screen screen = new TerminalScreen(terminal);
    public final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

    public Downloads downloads;

    public Tui(Diary index, Daemon daemon, Downloader downloader) throws IOException {
        this.index = index;
        this.daemon = daemon;
        this.downloader = downloader;

        this.downloads = new Downloads(this);

        this.initializeTui();
    }

    public void initializeTui() throws IOException {
        // Setup terminal and screen layers
        screen.startScreen();

        // Create panel to hold components
        Panel panel = new Panel();

        // grid layout
        panel.setLayoutManager(new GridLayout(3));

        // title with 2 columns
        panel.addComponent(new Label("HagiMule"), GridLayout.createHorizontallyFilledLayoutData(3));
        panel.addComponent(new Controls(this).getComponent());
        panel.addComponent(new Separator(Direction.VERTICAL));
        panel.addComponent(downloads.getComponent());

        // Create window to hold the panel
        BasicWindow window = new BasicWindow();
        window.setComponent(panel);

        textGUI.addWindowAndWait(window);
    }
}
