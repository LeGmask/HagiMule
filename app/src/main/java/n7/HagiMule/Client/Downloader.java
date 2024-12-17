package n7.HagiMule.Client;

import n7.HagiMule.Shared.FileInfo;

public interface Downloader {

    public void downloadFile(FileInfo info, String savingPath);

    public int getProgress();

    public void start();

    public void pause();

    public void resume();
}
