package n7.HagiMule.Shared;

import java.net.InetAddress;
import java.util.Objects;

public class PeerImpl implements Peer {
    private InetAddress ipAddress;
    private int port;
    private int hashCode;
    private float load;

    public PeerImpl(InetAddress ipAddress, int port, float load) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.hashCode = Objects.hash(this.ipAddress, this.port);
        this.load = load;
    }

    public PeerImpl(InetAddress ipAddress, int port) {
        this(ipAddress, port, 0f);
    }

    @Override
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public float getLoad() {
        return load;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer that = (Peer) o;
        return ipAddress.equals(that.getIpAddress()) && port == that.getPort();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
