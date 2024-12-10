package n7.HagiMule.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Daemon extends Remote {

    public byte[] requestFragment(String hash, int frag) throws RemoteException;

}