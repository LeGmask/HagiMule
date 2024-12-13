package n7.HagiMule.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.File;
import n7.HagiMule.Shared.FileImpl;
import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.FileInfoImpl;
import n7.HagiMule.Shared.FragmentRequest;
import n7.HagiMule.Shared.Peer;

public class DownloaderImpl extends Thread implements Downloader {

    public static final int NBDLSIMUL = 5;

    private Diary diary;
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Integer> queue;

    private class PeerConnexion implements Runnable {

        private Socket s;
        private FileInfo info;
        private File file;

        public PeerConnexion(Peer peer, FileInfo info, File fichier) throws IOException {
            System.out.println("Opening peer connexion");
            s = new Socket(peer.getIpAddress(), peer.getPort());
            System.out.println("Connexion établie");
            this.info = info;
            this.file = fichier;
        }

        public void run() {
            long request = 0;
            long net = 0;
            long io = 0;
            long start = 0;
            try {
                ObjectInputStream rois = new ObjectInputStream(s.getInputStream());
                ObjectOutputStream roos = new ObjectOutputStream(s.getOutputStream());

                while (!queue.isEmpty()) {
                    int fragNb = queue.poll();
                    start = System.currentTimeMillis();
                    roos.writeObject(new FragmentRequest(info.getHash(), fragNb));
                    request = request + System.currentTimeMillis() - start;

                    start = System.currentTimeMillis();
                    byte[] data = (byte[]) rois.readObject();
                    net = net + System.currentTimeMillis() - start;

                    start = System.currentTimeMillis();
                    file.writeFragment(fragNb, data);
                    io = io + System.currentTimeMillis() - start;
                    System.out.println("Downloader : IO : " + io + " | NET : " + net + " | REQ : " + request);
                }

                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public DownloaderImpl(Diary diary) {
        this.diary = diary;
        executor = Executors.newFixedThreadPool(NBDLSIMUL);
        queue = new ConcurrentLinkedQueue<Integer>();
    }

    @Override
    public void downloadFile(FileInfo info, String savingPath) {
        FileImpl fichier = new FileImpl(info, savingPath);
        long nbFrag = FileInfoImpl.getFragmentNumber(info);

        System.out.println("Téléchargement de " + nbFrag + " fragments");
        for (int i = 0; i < nbFrag; i++) {
            queue.add(i);
        }
        try {
            Peer[] peers = diary.getPeers(info.getHash());
            for (Peer p : peers) {
                try {
                    PeerConnexion conn = new PeerConnexion(p, info, fichier);
                    executor.submit(conn);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // TODO : change for active monitoring + résilience
            // Boolean done = executor.awaitTermination(10, TimeUnit.DAYS);

        } catch (RemoteException e) {
            System.out.println("Diary failed");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

}
