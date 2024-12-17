package n7.HagiMule.Diary;

import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.Peer;
import n7.HagiMule.Shared.PeerImpl;

public class DiaryImpl extends UnicastRemoteObject implements Diary {

    public static final int TTL = 300000; // 5 minutes TTL

    private Map<String, FileInfo> files;
    private Map<Peer, Set<String>> peers;
    private Map<Peer, Long> lastSeen;

    public DiaryImpl() throws java.rmi.RemoteException {
        files = new HashMap<String, FileInfo>();
        peers = new HashMap<Peer, Set<String>>();
        lastSeen = new HashMap<Peer, Long>();
    }

    @Override
    public FileInfo RequestFile(String hash) throws RemoteException {
        System.out.println("Requête de fichier dans l'index : " + hash);
        return files.get(hash);
    }

    @Override
    public void RegisterFile(FileInfo file, Peer peer) throws RemoteException {
        System.out.println(
                "Enregistrement du fichier dans l'Index : " + file.getNom() + " " + file.getHash());

        // create the file if needed
        this.files.putIfAbsent(file.getHash(), file);

        try {
            String remoteIP = super.getClientHost();
            Peer nPeer =
                    new PeerImpl((Inet4Address) Inet4Address.getByName(remoteIP), peer.getPort());

            // update peers
            this.peers.putIfAbsent(nPeer, new HashSet<String>());
            this.peers.get(nPeer).add(file.getHash());
            this.lastSeen.put(nPeer, System.currentTimeMillis());
        } catch (ServerNotActiveException e) {
            System.out.println(
                    "RegisterFile not called from a RMI Client but locally. \n"
                            + "This behaviour is not supported.");
        } catch (UnknownHostException e) {
            System.out.println("Hôte distant inconnu");
        }
    }

    @Override
    public FileInfo[] SearchFile(String nom) throws RemoteException {
        System.out.println("Recherche du fichier : " + nom);
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
        System.out.println("Recherche des pairs possédant le fichier : " + hash);
        Set<Peer> results = new HashSet<Peer>();
        Set<Peer> dead = new HashSet<Peer>();

        for (Peer p : this.peers.keySet()) {
            if (this.peers.get(p).contains(hash)) {
                long d = System.currentTimeMillis() - this.lastSeen.get(p);
                if (d < TTL) {
                    results.add(p);
                } else {
                    dead.add(p);
                }
            }
        }

        // unregister dead peers
        for (Peer p : dead) {
            this.UnregisterPeer(p);
        }

        return results.toArray(new Peer[results.size()]);
    }

    @Override
    public void UnregisterPeer(Peer peer) {
        try {
            String remoteIP = super.getClientHost();
            Peer nPeer =
                    new PeerImpl((Inet4Address) Inet4Address.getByName(remoteIP), peer.getPort());
            System.out.println("Désenregistrement du pair : " + nPeer.getIpAddress());
            this.peers.remove(nPeer);
            this.lastSeen.remove(nPeer);

            // using another list to save file and not remove them while iterating over the list of
            // files
            List<String> toDelete = new LinkedList<String>();
            for (String f : files.keySet()) {
                System.out.println(f);
                if (getPeers(f).length == 0) {
                    toDelete.add(f);
                }
            }
            // remove orphaned files
            for (String f : toDelete) {
                files.remove(f);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (ServerNotActiveException e) {
            System.out.println(
                    "RegisterFile not called from a RMI Client but locally. \n"
                            + "This behaviour is not supported.");
        } catch (UnknownHostException e) {
            System.out.println("Hôte distant inconnu");
        }
    }

    @Override
    public void peerKeepAlive(Peer peer) throws RemoteException {
        try {
            String remoteIP = super.getClientHost();
            Peer nPeer =
                    new PeerImpl((Inet4Address) Inet4Address.getByName(remoteIP), peer.getPort());
            this.lastSeen.put(nPeer, System.currentTimeMillis());
        } catch (ServerNotActiveException e) {
            System.out.println("peerKeepAlive not called from a remote RMI. Not supported.");
        } catch (UnknownHostException e) {
            System.out.println("Hôte distant inconnu");
        }
    }

    @Override
    public long getTTL() throws RemoteException {
        return TTL;
    }

    public static void main(String args[])
            throws RemoteException, MalformedURLException, AlreadyBoundException {
        LocateRegistry.createRegistry(4000);
        Naming.bind("//" + args[0] + ":4000/Diary", new DiaryImpl());
        System.out.println("Diary is listening on port 4000");
    }
}
