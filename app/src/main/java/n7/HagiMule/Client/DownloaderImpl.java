package n7.HagiMule.Client;

import java.util.ArrayList;
import java.util.List;
import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.FileInfo;

public class DownloaderImpl implements Downloader {

    private List<Thread> tasks;
    private List<Download>
            downloads; // très moche d'avoir les deux séparés. les indices *doivent* rester cohérent

    private Diary index;

    public DownloaderImpl(Diary d) {
        tasks = new ArrayList<>();
        downloads = new ArrayList<>();
        index = d;
    }

    @Override
    public int submit(FileInfo info, String savePath) {
        Download d = new DownloadImpl(index, info, savePath);
        Thread t = new Thread((Runnable) d);
        int id = tasks.size();
        t.start();
        tasks.add(t);
        downloads.add(d);
        return id;
    }

    @Override
    public List<Download> getAll() {
        return downloads;
    }

    @Override
    public Download get(int id) {
        return downloads.get(id);
    }

    @Override
    public void pauseAll() {
        for (Download d : downloads) {
            d.pause();
        }
    }

    @Override
    public void resumeAll() {
        for (Download d : downloads) {
            d.resume();
        }
    }
}
