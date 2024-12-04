package n7.HagiMule.Shared;

import java.net.Inet4Address;

public class PeerImpl implements Peer {
	private Inet4Address ipAddress;
	private int port;

	public PeerImpl(Inet4Address ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}

	@Override
	public Inet4Address getIpAddress() {
		return ipAddress;
	}

	@Override
	public int getPort() {
		return port;
	}

}