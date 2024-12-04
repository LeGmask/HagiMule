package n7.HagiMule.Shared;

import java.io.Serializable;
import java.net.Inet4Address;

public interface Peer extends Serializable {
	/**
	 * Getthe peer's IP address.
	 * @return the peer's IP address
	 */
    public Inet4Address getIpAddress();

	/**
	 * Get the peer's port.
	 * @return the peer's port
	 */
	public int getPort();

}
