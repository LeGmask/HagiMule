package n7.HagiMule.Diary;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.Peer;

public class DiaryImpl extends UnicastRemoteObject implements Diary {
    private Map<String, FileInfo> files;
    private Map<Peer, Set<String>> peers;

    public DiaryImpl() throws java.rmi.RemoteException {
        files = new HashMap<String, FileInfo>();
        peers = new HashMap<Peer, Set<String>>();
    }

    @Override
    public FileInfo RequestFile(String hash) throws RemoteException {
        return files.get(hash);
    }

    @Override
    public void RegisterFile(FileInfo file, Peer peer) throws RemoteException {
        // create the file if needed
        this.files.putIfAbsent(file.getHash(), file);

        // update peers
        this.peers.putIfAbsent(peer, new HashSet<String>());
        this.peers.get(peer).add(file.getHash());
    }

    @Override
    public FileInfo[] SearchFile(String nom) throws RemoteException {
        Set<FileInfo> results = new HashSet<FileInfo>();
        for (FileInfo f : this.files.values()) {
            if (f.getNom().contains(nom)) {
                results.add(f);
            }

        }
        return results.toArray(new FileInfo[results.size()]);
    }

    @Override
    public Peer[] getPeers(String hash) throws RemoteException {
        Set<Peer> results = new HashSet<Peer>();
        for (Peer p : this.peers.keySet()) {
            if (this.peers.get(p).contains(hash)) {
                results.add(p);
            }
        }
        return results.toArray(new Peer[results.size()]);
    }

    public static void main(String args[]) throws RemoteException, MalformedURLException, AlreadyBoundException {
        LocateRegistry.createRegistry(4000);
        Naming.bind("//localhost:4000/Diary", new DiaryImpl());
    }

}
