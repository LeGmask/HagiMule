package n7.HagiMule.Client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.File;
import n7.HagiMule.Shared.FileImpl;
import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.FileInfoImpl;
import n7.HagiMule.Shared.Peer;

public class DownloadImpl implements Download, Runnable {

    public static final int NBSOURCES = 10; // at most use 5 sources simultaneously
    public static final int POLLRATE = 10000; // poll index for new peers every 10 seconds

    private Diary diary;
    private ExecutorService executor;
    private Set<Peer> activePeers;
    private ConcurrentLinkedQueue<Integer> queue;
    private DownloadStatus status;
    private File fichier;

    private long lastFragReceived;

    private class PeerConnexion implements Runnable {

        private Socket s;
        private FileInfo info;
        private File file;
        private int currentFrag;
        private int nextFrag;


        public PeerConnexion(Peer peer, FileInfo info, File fichier) throws IOException {
            System.out.println("Ouverture connexion avec " + peer.getIpAddress());
            s = new Socket();
            s.connect(new InetSocketAddress(peer.getIpAddress(), peer.getPort()), 10 * 1000);
            s.setSoTimeout(60 * 1000); // max read timeout (1 minute)
            System.out.println(
                    "Connexion établie avec daemon " + peer.getIpAddress() + ":" + peer.getPort());
            this.info = info;
            this.file = fichier;
        }

        public void run() {
            long net = 0;
            long io = 0;
            long start = 0;
            try {
                Boolean exit = false;

                InputStream ris = s.getInputStream();
                BufferedInputStream rbis = new BufferedInputStream(ris);
                DataOutputStream rbos = new DataOutputStream(s.getOutputStream());
                byte[] buff = new byte[(int) info.getFragmentSize()];

                // send filehash to peer so it knows which file we are downloading
                // only send it once for the whole transaction
                rbos.writeUTF(info.getHash());
                rbos.flush();

                // sending initial request
                try {
                    nextFrag = queue.remove();
                } catch (NoSuchElementException e) {
                    System.out.println("Queue is empty.");
                    exit = true;
                }
                rbos.writeInt(nextFrag);
                rbos.flush();

                while (!exit) {
                    currentFrag = nextFrag;

                    // requesting the next fragment in advance
                    try {
                        nextFrag = queue.remove();
                        System.out.println("Requesting fragment " + currentFrag);
                        rbos.writeInt(nextFrag);
                        rbos.flush();
                    } catch (NoSuchElementException e) {
                        exit = true;
                    }
                    start = System.currentTimeMillis();

                    int target = FileInfoImpl.getTailleOfFrag(info, currentFrag);
                    int recv = 0;
                    while (recv < target) {
                        recv = recv + rbis.read(buff, recv, target - recv);
                    }
                    net = net + System.currentTimeMillis() - start;

                    start = System.currentTimeMillis();
                    file.writeFragment(currentFrag, buff, recv);
                    io = io + System.currentTimeMillis() - start;
                    lastFragReceived = System.currentTimeMillis();
                    System.out.println("Downloader : IO : " + io + " | NET : " + net);
                }

                s.close();
            } catch (SocketException | SocketTimeoutException e) {
                System.out.println(
                        "Downloader : une erreur est survenue pour le fragment "
                                + currentFrag
                                + " : "
                                + e.getLocalizedMessage());
                // something went wrong while downloading fragment
                // re-adding to the queue for ulterior retry
                System.out.println(
                        "Downloader : si l'erreur survient pour le premier fragment, il est"
                                + " possible que la source soit hors-ligne ou trop occupée.");
                queue.add(currentFrag);
                queue.add(nextFrag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DownloadImpl(Diary diary, FileInfo info, String savingPath) {
        this.diary = diary;
        fichier = new FileImpl(info, savingPath, true);

        executor = Executors.newFixedThreadPool(NBSOURCES);
        queue = new ConcurrentLinkedQueue<Integer>();
        activePeers = new HashSet<Peer>();
    }

    @Override
    public void run() {
        long nbFrag = FileInfoImpl.getFragmentNumber(fichier.getFileInfo());

        System.out.println(
                "=== Starting to download "
                        + fichier.getFileInfo().getNom()
                        + " with "
                        + nbFrag
                        + " fragments ===");

        for (int i = 0; i < nbFrag; i++) {
            queue.add(i);
        }

        try {
            long start = System.currentTimeMillis();

            try {
                while (!queue.isEmpty()) {
                    // while download is not done
                    // get peers from diary
                    Set<Peer> remotePeers =
                            new HashSet<Peer>(
                                    Arrays.asList(diary.getPeers(fichier.getFileInfo().getHash())));
                    // only consider new peers                
                    remotePeers.removeAll(activePeers);
                    Peer[] newPeers = remotePeers.toArray(new Peer[remotePeers.size()]);
                    // sorting peers depending on their load factor, favoring light-loaded peers
                    Arrays.sort(
                            newPeers,
                            new Comparator<Peer>() {
                                public int compare(Peer p1, Peer p2) {
                                    return p1.getLoad() > p2.getLoad() ? 1 : (-1);
                                }
                            });

                    System.out.println(
                            "Downloader : " + newPeers.length + " nouveau pair(s) trouvé(s)");
                    for (Peer p : newPeers) {
                        try {
                            PeerConnexion conn =
                                    new PeerConnexion(p, fichier.getFileInfo(), fichier);
                            executor.submit(conn);
                            activePeers.add(p);
                        } catch (SocketTimeoutException e) {
                            System.out.println(
                                    "Downloader : pair " + p.getIpAddress() + " injoignable.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Thread.sleep(POLLRATE);
                }
            } catch (InterruptedException e) {
                System.out.println("Downloader : interrupted !");
            }

            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("Downloader : Téléchargement interrompu !");
            }

            if (queue.isEmpty()) {
                System.out.println(
                        "=== Download of "
                                + fichier.getFileInfo().getNom()
                                + " took "
                                + (lastFragReceived - start) / 1000
                                + "s, at a speed of "
                                + (fichier.getFileInfo().getTaille() / (lastFragReceived - start))
                                + "B/s ===");
                status = DownloadStatus.FINISHED;
            } else {
                System.out.println("Downloader : Téléchargement échoué (morceaux manquants)");
                status = DownloadStatus.FAILED;
            }

        } catch (RemoteException e) {
            System.out.println("Diary failed");
            e.printStackTrace();
        }
    }

    @Override
    public int getProgress() {
        int total = FileInfoImpl.getFragmentNumber(fichier.getFileInfo());
        return (int) Math.floor(100 * (total - queue.size()) / ((float) total));
    }

    @Override
    public void pause() {
        if (status == DownloadStatus.RUNNING) {
            status = DownloadStatus.PAUSED;
        }
    }

    @Override
    public void resume() {
        if (status == DownloadStatus.PAUSED) {
            status = DownloadStatus.RUNNING;
        }
    }

    @Override
    public DownloadStatus getStatus() {
        return status;
    }
}
