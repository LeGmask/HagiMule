package n7.HagiMule.Diary;

import java.rmi.Remote;
import java.rmi.RemoteException;

import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.Peer;


public interface Diary extends Remote {
    
	/**
	 * Request a file from the network
	 * 
	 * @param hash the hash of the file
	 * @return the file requested
	 */
    public FileInfo RequestFile(String hash) throws RemoteException;
    
    /**
     * Register a file avaible on the network
	 * 
     * @param file the file to register
     * @param hash the hash of the file
     * @param ip the ip of the host
     * @param port the port of the host
     */
	public void RegisterFile(FileInfo file, Peer peer) throws RemoteException;


	/**
	 * Search a file on the network
	 * 
	 * @param nom the name of the file
	 * @return the list of files found
	 */
    public FileInfo[] SearchFile(String nom) throws RemoteException;

	/**
	 * Get the list of peers that have the file
	 * 
	 * @param file the file to search
	 * @return the list of peers
	 */
	public Peer[] getPeers(String hash) throws RemoteException;
}