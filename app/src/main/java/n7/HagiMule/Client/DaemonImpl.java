package n7.HagiMule.Client;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.File;
import n7.HagiMule.Shared.FileImpl;
import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.FileInfoImpl;
import n7.HagiMule.Shared.FileUtils;
import n7.HagiMule.Shared.Peer;
import n7.HagiMule.Shared.PeerImpl;

public class DaemonImpl extends Thread implements Daemon {

    public static final int NBPEER = 10;
    public static final int DEFAULT_FRAGSIZE = 40000000;

    private Diary index;
    private Map<String, File> files;
    private ExecutorService executor;
    private ServerSocket ss;
    private int port;

    private class PeerConnexion implements Runnable {

        private Socket remote;

        public PeerConnexion(Socket s) {
            System.out.println("Connexion établie avec client " + s.getInetAddress().toString());
            remote = s;
        }

        @Override
        public void run() {
            try {

                OutputStream ros = remote.getOutputStream();
                DataInputStream rdis = new DataInputStream(remote.getInputStream());
                String fileHash = rdis.readUTF();
                File fichier = files.get(fileHash);

                if (fichier != null) {
                    while (true) {
                        int fragmentNumber = rdis.readInt();
                        long start = System.currentTimeMillis();
                        ros.write(fichier.readFragment(fragmentNumber));
                        System.out.println(
                                "Daemon : frag time : "
                                        + (System.currentTimeMillis() - start)
                                        + "ms");
                    }
                } else {
                    System.out.println("Le fichier demander n'est pas proposé par le Daemon.");
                }

            } catch (EOFException e) {
                System.out.println("Daemon Worker : pair s'est déconnecté.");
            } catch (SocketException e) {
                System.out.println("Daemon Worker : connection avec le pair rompue.");
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                System.out.println("Daemon : fermeture connexion socket client");
                remote.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DaemonImpl(Diary diary) throws RemoteException, IOException {
        index = diary;
        files = new HashMap<String, File>();
        this.executor = Executors.newFixedThreadPool(NBPEER);
        ss = new ServerSocket(0);
        port = ss.getLocalPort();
        System.out.println("Daemon : lancé sur le port " + port);
    }

    @Override
    public void run() {
        try {
            long ttl = index.getTTL();
            Thread keepAlive =
                    new Thread() {
                        public void run() {
                            try {
                                while (true) {
                                    Thread.sleep(ttl / 2);
                                    index.peerKeepAlive(new PeerImpl(null, port));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
            keepAlive.start();

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
        index.UnregisterPeer(new PeerImpl(null, port));
        ss.close();
    }

    @Override
    public void addFichier(String filepath) {
        try {
            long size = Files.size(Paths.get(filepath));
            FileInfo info =
                    new FileInfoImpl(
                            Paths.get(filepath).getFileName().toString(),
                            size,
                            FileUtils.md5Hash(filepath),
                            DEFAULT_FRAGSIZE);

            System.out.println("Enregistrement du fichier dans le Daemon : " + info.getNom());
            files.put(info.getHash(), new FileImpl(info, filepath, false));
            Peer p = new PeerImpl(null, port);
            try {
                index.RegisterFile(info, p);
            } catch (RemoteException e) {
                System.out.println(
                        "Daemon : une erreur avec le Diary est survenu : "
                                + e.getLocalizedMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
