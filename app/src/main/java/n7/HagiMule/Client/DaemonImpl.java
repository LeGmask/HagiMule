package n7.HagiMule.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.File;
import n7.HagiMule.Shared.FileImpl;
import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.FileInfoImpl;
import n7.HagiMule.Shared.FragmentRequest;
import n7.HagiMule.Shared.Peer;
import n7.HagiMule.Shared.PeerImpl;

public class DaemonImpl extends Thread implements Daemon {

    public static final int NBPEER = 10;
    public static final int PORT = 56739;
    public static final int DEFAULT_FRAGSIZE = 409600;
 
    private Diary index;
    private Map<String, File> files;
    private ExecutorService executor;
    private ServerSocket ss;

    private class PeerConnexion implements Runnable {

        private Socket remote;

        public PeerConnexion(Socket s) {
            System.out.println("Connection établie avec client " + s.getInetAddress().toString());
            remote = s;
        }

        @Override
        public void run() {
            long iotime = 0;
            long sendingtime = 0;
            try {
                InputStream ris = remote.getInputStream();
                OutputStream ros = remote.getOutputStream();

                ObjectOutputStream roos = new ObjectOutputStream(ros);
                ObjectInputStream rois = new ObjectInputStream(ris);

                while(true) {
                    FragmentRequest request = (FragmentRequest)rois.readObject();

                    File fichier = files.get(request.fileHash);
                    long start = System.currentTimeMillis();
                    byte[] data = fichier.readFragment(request.fragmentNumber);
                    iotime = iotime + System.currentTimeMillis()-start;
                    start = System.currentTimeMillis();
                    roos.writeObject(data);
                    sendingtime = sendingtime + System.currentTimeMillis() - start;
                    System.out.println("Daemon : IO : " + iotime + " | NET : " + sendingtime);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassCastException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }


    public DaemonImpl(Diary diary) throws RemoteException, IOException {
        index = diary;
        files = new HashMap<String, File>();
        this.executor = Executors.newFixedThreadPool(NBPEER);
        ss = new ServerSocket(PORT);
    }

    @Override 
    public void run() {
        try {
            while (!ss.isClosed()) {
                executor.submit(new PeerConnexion(ss.accept()));
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public void close() throws IOException {
        System.out.println("Closing Dameon...");
        // on se dé-enregistre du diary
        index.UnregisterPeer(new PeerImpl(null, PORT));
        ss.close();
    }


    @Override
    public void addFichier(String filepath) {
        try {
            long size = Files.size(Paths.get(filepath));
            FileInfo info = new FileInfoImpl(filepath, size, String.valueOf(Objects.hash(filepath)), DEFAULT_FRAGSIZE);
            
            System.out.println("Enregistrement du fichier dans le Daemon : " + info.getNom());
            files.put(info.getHash(), new FileImpl(info, filepath));
            Peer p = new PeerImpl(null, PORT);
            try {
                index.RegisterFile(info, p);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
