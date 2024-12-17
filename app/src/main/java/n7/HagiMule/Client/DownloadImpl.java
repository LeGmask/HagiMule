package n7.HagiMule.Client;

import com.google.common.collect.ConcurrentHashMultiset;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import n7.HagiMule.Diary.Diary;
import n7.HagiMule.Shared.FileImpl;
import n7.HagiMule.Shared.FileInfo;
import n7.HagiMule.Shared.FileInfoImpl;
import n7.HagiMule.Shared.Peer;

public class DownloadImpl implements Download {
	private FileImpl file;
	private Diary diary;
	private long fragmentsCount;
	private ConcurrentLinkedQueue<Integer> queue;
	private Boolean isFinished = false;

	private ConcurrentHashMap<Peer, Socket> connections = new ConcurrentHashMap<>();
	private ConcurrentHashMultiset<Peer> activePeers = ConcurrentHashMultiset.create();
	private Peer[] peers;
	private Semaphore semaphore;
	// private AtomicInteger WorkingThreads = new AtomicInteger(0);

	public DownloadImpl(Diary diary, FileInfo info, String savingPath, Semaphore semaphore) {
		this.diary = diary;
		this.file = new FileImpl(info, savingPath, true);
		this.semaphore = semaphore;
		this.fragmentsCount = FileInfoImpl.getFragmentsCount(info);
		this.queue = new ConcurrentLinkedQueue<>();

		for (int i = 0; i < this.fragmentsCount; i++) {
			this.queue.add(i);
		}

		System.out.println(
				"New download created for file \""
						+ info.getNom()
						+ "\" with "
						+ this.fragmentsCount
						+ " fragments");
	}

	private Socket connect(Peer peer) throws IOException {
		System.out.println(
				"Oppening connection with " + peer.getIpAddress() + ":" + peer.getPort());
		Socket s = new Socket();
		s.connect(new InetSocketAddress(peer.getIpAddress(), peer.getPort()), 10 * 1000);
		s.setSoTimeout(60 * 1000); // max read timeout (1 minute)
		setupConnection(s);
		this.connections.put(peer, s);
		System.out.println(
				"Connection established with daemon " + peer.getIpAddress() + ":" + peer.getPort());
		return s;
	}

	private Socket getConnection(Peer peer) throws IOException {
		System.out.println("Getting connection to " + peer.getIpAddress() + ":" + peer.getPort());
		return this.connections.getOrDefault(peer, connect(peer));
	}

	private void setupConnection(Socket s) throws IOException {
		// Send file hash to peer so it knows which file we are downloading
		// Only send it once for the whole transaction
		DataOutputStream rbos = new DataOutputStream(s.getOutputStream());
		rbos.writeUTF(this.file.getFileInfo().getHash());
		rbos.flush();
	}

	private Peer getRandomPeer() throws RemoteException {
		System.out.println("Getting random peer");
		peers = diary.getPeers(file.getFileInfo().getHash());
		System.out.println("Active peers: " + peers);

		// if (peers.length == 0) {
		// try {
		// System.out.println("Got " + peers.length + " peers");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }

		List<Peer> possiblePeers = new ArrayList<>(Arrays.asList(peers));
		// Filter out peers that are already connected
		possiblePeers.removeIf(peer -> activePeers.contains(peer));

		Peer peer = possiblePeers.get((int) (Math.random() * possiblePeers.size()));
		System.out.println("Selected peer " + peer.getIpAddress() + ":" + peer.getPort());
		return peer;
	}

	@Override
	public int getProgress() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getProgress'");
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'start'");
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'pause'");
	}

	private void cleanup() {
		// Close all connections
		for (Socket s : connections.values()) {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Remove all connections
		connections.clear();

		// Remove all active peers
		activePeers.clear();
	}

	@Override
	public void run() {
		System.out.println("Scheduled download fragment request");
		// Get a connection to a random peer
		if (this.queue.isEmpty()) {
			System.out.println("Download is already finished");
			isFinished = true;
			// cleanup();
			return;
		}

		try {
			Peer peer = getRandomPeer();
			Socket s = getConnection(peer);
			activePeers.add(peer);

			BufferedInputStream rbis = new BufferedInputStream(s.getInputStream());
			DataOutputStream rbos = new DataOutputStream(s.getOutputStream());
			byte[] buff = new byte[(int) file.getFileInfo().getFragmentSize()];

			int currentFrag = queue.poll();
			System.out.println("Requesting fragment " + currentFrag);

			rbos.writeInt(currentFrag);
			rbos.flush();

			int target = FileInfoImpl.getTailleOfFrag(file.getFileInfo(), currentFrag);
			int recv = 0;
			while (recv < target) {
				recv = recv + rbis.read(buff, recv, target - recv);
			}
			file.writeFragment(currentFrag, buff, recv);
			System.out.println("Fragment " + currentFrag + " downloaded");
			activePeers.remove(peer);

		} catch (IOException e) {
			e.printStackTrace();
		}

		semaphore.release();
		return;
	}

	@Override
	public Boolean isFinished() {
		return isFinished;
	}

	@Override
	public int getWorkingThreads() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getWorkingThreads'");
	}
}
