package n7.HagiMule.Client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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
            long net = 0;
            long io = 0;
            long start = 0;
            try {

                InputStream ris = s.getInputStream();
                BufferedInputStream rbis = new BufferedInputStream(ris);
                DataOutputStream rbos = new DataOutputStream(s.getOutputStream());
                byte[] buff = new byte[(int)info.getFragmentSize()];

                // send filehash to peer so it knows which file we are downloading
                // only send it once for the whole transaction
                rbos.writeUTF(info.getHash());
                rbos.flush();
                
                while (!queue.isEmpty()) {
                    int fragNb = queue.poll();
                    System.out.println("Requesting fragment " + fragNb);
                    start = System.currentTimeMillis();
                    rbos.writeInt(fragNb);
                    rbos.flush();
                    
                    int target = FileInfoImpl.getTailleOfFrag(info, fragNb);
                    int recv = 0;
                    while (recv < target) {
                        recv = recv + rbis.read(buff, recv, target - recv);   
                    }
                    net = net + System.currentTimeMillis() - start;

                    start = System.currentTimeMillis();
                    System.out.println(fragNb + " - " + recv);
                    file.writeFragment(fragNb, buff, recv);
                    io = io + System.currentTimeMillis() - start;
                    
                    System.out.println("Downloader : IO : " + io + " | NET : " + net);
                }

                s.close();
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
