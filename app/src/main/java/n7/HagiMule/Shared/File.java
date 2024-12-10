package n7.HagiMule.Shared;

import java.io.IOException;

public interface File {
    
    public FileInfo getFileInfo();

    public Boolean fragmentIsAvailable(int fragment);

    public byte[] readFragment(int fragment) throws IOException;

    public void writeFragment(int fragment, byte[] data) throws IOException;


}
