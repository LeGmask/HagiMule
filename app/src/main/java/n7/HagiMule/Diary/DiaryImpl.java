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
import java.util.Map;
import java.util.Set;

import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.Peer;
import n7.HagiMule.Shared.PeerImpl;

public class DiaryImpl extends UnicastRemoteObject implements Diary {
    private Map<String, FileInfo> files;
    private Map<Peer, Set<String>> peers;

    public DiaryImpl() throws java.rmi.RemoteException {
        files = new HashMap<String, FileInfo>();
        peers = new HashMap<Peer, Set<String>>();
    }

    @Override
    public FileInfo RequestFile(String hash) throws RemoteException {
        System.out.println("Requête de fichier dans l'index : " + hash);
        return files.get(hash);
    }

    @Override
    public void RegisterFile(FileInfo file, Peer peer) throws RemoteException {
        System.out.println("Enregistrement du fichier dans l'Index : " + file.getNom() + " " + file.getHash());

        // create the file if needed
        this.files.putIfAbsent(file.getHash(), file);

        try {
            String remoteIP = super.getClientHost();
            Peer nPeer = new PeerImpl((Inet4Address) Inet4Address.getByName(remoteIP), peer.getPort());

            // update peers
            this.peers.putIfAbsent(nPeer, new HashSet<String>());
            this.peers.get(nPeer).add(file.getHash());
        } catch (ServerNotActiveException e) {
            System.out.println(
                    "RegisterFile not called from a RMI Client but locally. \nThis behaviour is not supported.");
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
        for (Peer p : this.peers.keySet()) {
            if (this.peers.get(p).contains(hash)) {
                results.add(p);
            }
        }
        return results.toArray(new Peer[results.size()]);
    }

    @Override
    public void UnregisterPeer(Peer peer) {
        try {
            String remoteIP = super.getClientHost();
            Peer nPeer = new PeerImpl((Inet4Address) Inet4Address.getByName(remoteIP), peer.getPort());
            System.out.println("Désenregistrement du pair : " + nPeer.getIpAddress());
            this.peers.remove(nPeer);

            for(String f : files.keySet()) {
                if (this.getPeers(f).length == 0) {
                    files.remove(f);
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } 
        catch (ServerNotActiveException e) {
            System.out.println(
                    "RegisterFile not called from a RMI Client but locally. \nThis behaviour is not supported.");
        } catch (UnknownHostException e) {
            System.out.println("Hôte distant inconnu");
        }
    }

    public static void main(String args[]) throws RemoteException, MalformedURLException, AlreadyBoundException {
        LocateRegistry.createRegistry(4000);
        Naming.bind("//localhost:4000/Diary", new DiaryImpl());
        System.out.println("Diary is listening on port 4000");
    }

}
