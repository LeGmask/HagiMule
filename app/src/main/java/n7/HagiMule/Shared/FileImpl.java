package n7.HagiMule.Shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileImpl implements File {
    
    private FileInfo fileInfo;
    private Path filePath;
    private int downloadedSize;

    private FileChannel channel;

    public FileImpl(FileInfo fileInfo, String path) {
        this.fileInfo = fileInfo;
        this.filePath = Paths.get(path);

        try {
            this.channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
    }

    @Override
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    @Override
    public Boolean fragmentIsAvailable(int fragment) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fragmentIsAvailable'");
    }

    public ByteBuffer getFragment(int fragment) throws IOException {
        int fragSize = fileInfo.getFragmentSize();
        int start = fragSize * fragment;
        int end = Math.min(fileInfo.getTaille(), (fragment+1)*fragSize);
        int taille = end - start;

        ByteBuffer buffer = ByteBuffer.allocate(taille);
        // seek le fichier au début du fragment
        this.channel.position(start);
        do {
            this.channel.read(buffer);
        } while (buffer.hasRemaining());

        return buffer;
    }

    public void setFragment(int fragment, ByteBuffer data) throws IOException {
        
        int start = getFileInfo().getFragmentSize() * fragment;
        this.channel.position(start);
        do {
            this.channel.write(data);
        } while (data.hasRemaining());

    }

}