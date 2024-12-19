package n7.HagiMule.Shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.StandardOpenOption;


public class FileUtils {
    

    public static String md5Hash(String filepath) {
        String checksum = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            InputStream frd = Files.newInputStream(Paths.get(filepath), StandardOpenOption.READ);            
            ByteBuffer b = ByteBuffer.allocate(4096);
            int i;
            while((i = frd.read(b.array(), 0, 4096)) > 0 ) {
                b.limit(i);
                b.position(0);
                md.update(b);
                b.clear();
            }
            frd.close();
            checksum = new BigInteger(1, md.digest()).toString(16); 
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algo de hashage md5 non disponible.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return checksum;
    }
}
