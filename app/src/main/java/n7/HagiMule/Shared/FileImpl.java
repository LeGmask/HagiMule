package n7.HagiMule.Shared;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileImpl implements File {
    
    private FileInfo fileInfo;
    private Path filePath;

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

    public byte[] readFragment(int fragment) throws IOException {
        long fragSize = fileInfo.getFragmentSize();
        long start = fragSize * fragment;
        long end = Math.min(fileInfo.getTaille(), (fragment+1)*fragSize);
        long taille = end - start;

        ByteBuffer buffer = ByteBuffer.allocate((int)taille);
        // seek le fichier au début du fragment
        this.channel.position(start);
        do {
            this.channel.read(buffer);
        } while (buffer.hasRemaining());

        return buffer.array();
    }

    public void writeFragment(int fragment, byte[] data) throws IOException {
        
        long start = getFileInfo().getFragmentSize() * (long)fragment;
        ByteBuffer buff = ByteBuffer.wrap(data);
        this.channel.position(start);
        do {
            this.channel.write(buff);
        } while (buff.hasRemaining());

    }

}