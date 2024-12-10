package n7.HagiMule.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.File;
import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.FragmentRequest;
import n7.HagiMule.Shared.Peer;
import n7.HagiMule.Shared.PeerImpl;

public class DaemonImpl extends Thread implements Daemon {

    public static final int NBPEER = 10;
    public static final int PORT = 56739;
 
    private Diary index;
    private Map<String, File> files;
    private ExecutorService executor;
    private ServerSocket ss;

    private class PeerConnexion implements Runnable {

        private Socket remote;

        public PeerConnexion(Socket s) {
            remote = s;
        }

        @Override
        public void run() {
            try {
                InputStream ris = remote.getInputStream();
                OutputStream ros = remote.getOutputStream();
        
                ObjectInputStream rois = new ObjectInputStream(ris);
                ObjectOutputStream roos = new ObjectOutputStream(ros);
    
                while(true) {
                    FragmentRequest request = (FragmentRequest)rois.readObject();
                    File fichier = files.get(request.fileHash);
                    byte[] data = fichier.readFragment(request.fragmentNumber);
                    roos.writeObject(data);
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
        ExecutorService executor = Executors.newFixedThreadPool(NBPEER);
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
    public void addFichier(FileInfo fileinfo) {
        System.out.println("Enregistrement du fichier dans le Daemon : " + fileinfo.getNom());
        Peer p = new PeerImpl(null, PORT);
        try {
            index.RegisterFile(fileinfo, p);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
