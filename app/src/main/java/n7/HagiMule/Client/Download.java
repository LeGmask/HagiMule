package n7.HagiMule.Client;

public interface Download {

    public String getFileName();

    public int getProgress();

    public DownloadStatus getStatus();

    public void pause();

    public void resume();
}
