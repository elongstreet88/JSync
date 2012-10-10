/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsync;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 *
 * @author Administrator
 */
public class FileActions
{
    final int BUFFER_SIZE = 1024 * 1024;
    public String getChecksum(String fileName)
    {
        try
        {
            FileChannel ch = new FileInputStream(fileName).getChannel();
            ByteBuffer bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
            
            int nRead;
            MessageDigest md = MessageDigest.getInstance("MD5");
            
            while ((nRead = ch.read(bb)) != -1)
            {
                if (nRead == 0)
                {
                    continue;
                }
                bb.position(0).limit(nRead);
                while (bb.hasRemaining())
                {
                    md.update(bb);
                }
                bb.clear();
            }
            return (new HexBinaryAdapter()).marshal(md.digest());
        }
        finally
        {
            return "MD5 could not be calculated";
        }
    }
}
