package n7.HagiMule.Shared;

import java.io.Serializable;

public interface FileInfo extends Serializable {

    public long getTaille();

    public String getNom();

    public String getHash();

    public long getFragmentSize();
}
