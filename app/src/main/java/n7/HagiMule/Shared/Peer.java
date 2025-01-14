package n7.HagiMule.Shared;

import java.io.Serializable;
import java.net.InetAddress;

public interface Peer extends Serializable {
    /**
     * Getthe peer's IP address.
     *
     * @return the peer's IP address
     */
    public InetAddress getIpAddress();

    /**
     * Get the peer's port.
     *
     * @return the peer's port
     */
    public int getPort();

    /**
     * Get the peer's load factor
     *
     * @return the peer's load factor
     */
    public float getLoad();
}
