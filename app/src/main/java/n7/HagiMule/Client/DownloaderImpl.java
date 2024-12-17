package n7.HagiMule.Client;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.FileInfo;

public class DownloaderImpl extends Thread implements Downloader {

    public static final int NBDLSIMUL = 5;
    private final Semaphore availableDownloads = new Semaphore(NBDLSIMUL, true);
    private final ReentrantLock downloadsLock = new ReentrantLock();
    private final Condition newDownloadLock = downloadsLock.newCondition();

    private Diary diary;
    private ThreadPoolExecutor executor;
    private Boolean running = true;
    private ArrayList<Download> downloads = new ArrayList<>();

    @Override
    public void run() {
        System.out.println("Download is running...");
        while (true) {
            downloadsLock.lock();
            try {
                if (!(downloads.size() > 0))
                    newDownloadLock.await();

                System.out.println("Downloads: " + downloads.size());

                for (Download download : downloads) {
                    // acquire a permit to download
                    availableDownloads.acquire();
                    executor.submit(download);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                downloadsLock.unlock();
            }
        }
    }

    public DownloaderImpl(Diary diary) {
        this.diary = diary;
        this.executor = new ThreadPoolExecutor(NBDLSIMUL, NBDLSIMUL, 0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());
    }

    private boolean canSchedule() {
        System.out.println("Active count: " + this.executor.getActiveCount());
        return this.executor.getActiveCount() < NBDLSIMUL;
    }

    @Override
    public void downloadFile(FileInfo info, String savingPath) {
        downloadsLock.lock();
        try {
            downloads.add(new DownloadImpl(diary, info, savingPath, availableDownloads));
            newDownloadLock.signal();
        } finally {
            downloadsLock.unlock();
        }
    }

    @Override
    public int getProgress() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProgress'");
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pause'");
    }

    @Override
    public void shutdown() {
        this.executor.shutdown();
    }
}
