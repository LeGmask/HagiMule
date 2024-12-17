package n7.HagiMule.Client;

public interface Download extends Runnable {

	public int getProgress();

	public void start();

	public void pause();

	public int getWorkingThreads();

	public Boolean isFinished();
}
