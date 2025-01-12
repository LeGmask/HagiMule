package n7.HagiMule.Client.Tui;

import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.ProgressBar;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import n7.HagiMule.Client.Download;
import n7.HagiMule.Client.DownloadStatus;

public class Downloads implements TuiComponent {
	Tui tui;

	ArrayList<Integer> downloads = new ArrayList<Integer>();
	Panel panel;

	public Downloads(Tui tui) {
		this.tui = tui;
		this.panel = new Panel();
	}

	@Override
	public Component getComponent() {
		panel.setLayoutManager(new GridLayout(3));
		setupRenderLoop();
		return panel;
	}

	private void setupRenderLoop() {
		Timer t = new Timer();
		t.schedule(
				new TimerTask() {
					@Override
					public void run() {
						update();
					}
				},
				1,
				100);
	}

	private void addHeader() {
		panel.addComponent(new Label("File"));
		panel.addComponent(new Label("Progress"));
		panel.addComponent(new Label("Status"));
	}

	private void update() {
		panel.removeAllComponents();
		addHeader();
		for (int id : downloads) {
			Download download = tui.downloader.get(id);

			panel.addComponent(new Label(download.getFileName()));
			ProgressBar progressBar = new ProgressBar();
			progressBar.setPreferredWidth(25);
			progressBar.setValue(download.getProgress());
			panel.addComponent(progressBar);
			panel.addComponent(new Label(getStatus(download)));
		}
		panel.invalidate();
	}

	private static String getStatus(Download download) {
		DownloadStatus status = download.getStatus();
		if (status == null) {
			return "Unknown";
		}

		return status.toString();

	}

	public void addDownload(int id) {
		downloads.add(id);
	}
}
