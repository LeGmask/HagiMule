package n7.HagiMule.Shared;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileImpl implements File {
    
    private FileInfo fileInfo;
    private Path filePath;
    private Lock mutex;
    private FileChannel channel;

    public FileImpl(FileInfo fileInfo, String path) {
        this.fileInfo = fileInfo;
        this.filePath = Paths.get(path);
        this.mutex = new ReentrantLock();
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

    public ByteBuffer readFragment(int fragment) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate((int) FileInfoImpl.getTailleOfFrag(fileInfo, fragment));
        long start = getFileInfo().getFragmentSize() * (long)fragment;
        
        // section critique
        this.mutex.lock();
        this.channel.read(buffer, start);
        this.mutex.unlock();
        
        return buffer;
    }

    public void writeFragment(int fragment, ByteBuffer data) throws IOException {
        long start = getFileInfo().getFragmentSize() * (long)fragment;
        
        // section critique
        this.mutex.lock();
        this.channel.write(data, start);
        this.mutex.unlock();
    }

}