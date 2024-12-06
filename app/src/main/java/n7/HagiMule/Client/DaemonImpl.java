package n7.HagiMule.Client;

import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Diary.DiaryImpl;
import n7.HagiMule.Shared.File;
import n7.HagiMule.Shared.FileImpl;
import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.FileInfoImpl;
import n7.HagiMule.Shared.Peer;
import n7.HagiMule.Shared.PeerImpl;

public class DaemonImpl extends UnicastRemoteObject implements Daemon {

    private Diary index;
    private Map<String, File> files;

    public DaemonImpl(Diary diary) throws RemoteException {
        index = diary;
        files = new HashMap<String, File>();

    }

    @Override
    public byte[] requestFragment(String hash, int frag) throws RemoteException {
        File f = files.get(hash);
        if (f == null) throw new RemoteException();
        try {
            return f.readFragment(frag);
        } catch (Exception e) {
            throw new RemoteException("File.readFragment failed");
        }
    }


    public void debugUploader() {
        FileInfo info = new FileInfoImpl("Mon super fichier", 5, "testhash", 1);
        File fichier = new FileImpl(info, "test.txt");
        files.put(info.getHash(), fichier);
        try {
            index.RegisterFile(info, new PeerImpl((Inet4Address)Inet4Address.getLocalHost(), 4001));

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            LocateRegistry.createRegistry(4001);
            Naming.bind("//localhost:4001/Daemon", this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void debugDownloader() {
        try {
            FileInfo f = index.RequestFile("testhash");
            File fichier = new FileImpl(f, "testDL.txt");
            Peer[] peers =  index.getPeers("testhash");

            String dIP = "//localhost"  + ":" + peers[0].getPort() + "/Daemon";
            System.out.println("Connecting to Daemon : " + dIP);
            Daemon peer = (Daemon) Naming.lookup(dIP);
            byte[] res = peer.requestFragment(f.getHash(), 0);

            fichier.writeFragment(0, res);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
