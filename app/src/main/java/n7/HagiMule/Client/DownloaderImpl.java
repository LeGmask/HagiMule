package n7.HagiMule.Client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            System.out.println("Connexion établie avec daemon " + peer.getIpAddress() + ":" + peer.getPort());
            this.info = info;
            this.file = fichier;
        }

        public void run() {
            long request = 0;
            long net = 0;
            long io = 0;
            long start = 0;
            try {
                InputStream ris = s.getInputStream();
                OutputStream ros = s.getOutputStream();
                
                BufferedInputStream rbis = new BufferedInputStream(ris);
                ObjectOutputStream roos = new ObjectOutputStream(ros);
                ByteBuffer buff = ByteBuffer.allocate((int)info.getFragmentSize());
                
                while (!queue.isEmpty()) {
                    // récup numéro fragment à dl
                    int fragNb = queue.poll();

                    // envoit requête au pair pour le fragment
                    System.out.println("Requesting fragment " + fragNb);
                    start = System.currentTimeMillis();
                    roos.writeObject(new FragmentRequest(info.getHash(), fragNb));
                    request = request + System.currentTimeMillis() - start;
                    
                    buff.position(0);                    
                    
                    // récupération du fragment demandé
                    System.out.println("Downloading fragment " + fragNb);
                    start = System.currentTimeMillis();
                    int target = FileInfoImpl.getTailleOfFrag(info, fragNb);
                    byte[] data = rbis.readNBytes(target);
                    buff.put(data);
                    net = net + System.currentTimeMillis() - start;


                    // écriture sur disque du fragment reçu
                    System.out.println("Writing fragment " + fragNb);
                    start = System.currentTimeMillis();
                    file.writeFragment(fragNb, buff);
                    io = io + System.currentTimeMillis() - start;
                    
                    System.out.println("Downloader : IO : " + io + " | NET : " + net + " | REQ : " + request);
                }

                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            } 
            // catch (ClassNotFoundException e) {
            //     e.printStackTrace();
            // }

            System.out.println("Fin Downloader Worker Thread");

        }
    }

    public DownloaderImpl(Diary diary) {
        this.diary = diary;
    }

    @Override
    public void downloadFile(FileInfo info, String savingPath) {
        
        FileImpl fichier = new FileImpl(info, savingPath);
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
            System.out.println("Downloader : Téléchargement terminé.");


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
