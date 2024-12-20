package n7.HagiMule.Client;

public interface Download {

    public int getProgress();

    public DownloadStatus getStatus();

    public void pause();

    public void resume();
}
