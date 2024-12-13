package n7.HagiMule.Shared;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface File {
    
    public FileInfo getFileInfo();

    public Boolean fragmentIsAvailable(int fragment);

    public ByteBuffer readFragment(int fragment) throws IOException;

    public void writeFragment(int fragment, ByteBuffer buff) throws IOException;


}
