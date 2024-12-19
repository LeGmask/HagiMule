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
import java.util.NoSuchElementException;
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

public class DownloaderImpl extends Thread implements Downloader {

    public static final int NBDLSIMUL = 5;

    private Diary diary;
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Integer> queue;

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
                queue.add(currentFrag);
                queue.add(nextFrag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DownloaderImpl(Diary diary) {
        this.diary = diary;
    }

    @Override
    public void downloadFile(FileInfo info, String savingPath) {

        FileImpl fichier = new FileImpl(info, savingPath, true);
        long nbFrag = FileInfoImpl.getFragmentNumber(info);

        executor = Executors.newFixedThreadPool(NBDLSIMUL);
        queue = new ConcurrentLinkedQueue<Integer>();

        System.out.println("Téléchargement de " + nbFrag + " fragments");
        for (int i = 0; i < nbFrag; i++) {
            queue.add(i);
        }
        try {
            Peer[] peers = diary.getPeers(info.getHash());
            System.out.println("Downloader : " + peers.length + " pair(s) trouvé(s)");
            for (Peer p : peers) {
                try {
                    PeerConnexion conn = new PeerConnexion(p, info, fichier);
                    executor.submit(conn);
                } catch (SocketTimeoutException e) {
                    System.out.println("Downloader : pair " + p.getIpAddress() + " injoignable.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // TODO : change for active monitoring + résilience

            // STUB : Wait for the end of the download and exit properly
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("Downloader : Téléchargement interrompu !");
            }

            if (queue.isEmpty()) {
                System.out.println("Downloader : Téléchargement terminé avec succès.");
            } else {
                System.out.println("Downloader : Téléchargement échoué (morceaux manquants)");
            }

        } catch (RemoteException e) {
            System.out.println("Diary failed");
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