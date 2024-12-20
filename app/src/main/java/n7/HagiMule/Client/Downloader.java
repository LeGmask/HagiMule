package n7.HagiMule.Client;

import java.util.List;
import n7.HagiMule.Shared.FileInfo;

public interface Downloader {

    public int submit(FileInfo info, String savePath);

    public List<Download> getAll();

    public Download get(int id);

    public void pauseAll();

    public void resumeAll();
}
