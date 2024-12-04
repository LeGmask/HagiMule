package n7.HagiMule.Shared;

import java.io.Serializable;

public interface FileInfo extends Serializable {
    
    public int getTaille();

    public String getNom();

    public String getHash();

    public int getFragmentSize();
}


