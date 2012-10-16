/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 *
 * @author Administrator
 */
public class Tools
{
    public static ByteBuffer convert(int value)
    {
        return ByteBuffer.wrap(new byte[]
                {
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value
                });
    }
    public static ByteBuffer convert(long value)
    {
        return ByteBuffer.wrap(new byte[]
                {
                    (byte) (value >>> 56),
                    (byte) (value >>> 48),
                    (byte) (value >>> 40),
                    (byte) (value >>> 32),
                    (byte) (value >>> 24),
                    (byte) (value >>> 16),
                    (byte) (value >>> 8),
                    (byte) value
                });
    }
    public static long convert(byte[] b)
    {
        return ByteBuffer.wrap(b).getLong();
    }
    public static boolean delete(String... fileNames)
    {
        boolean result = true;
        for (String fileName : fileNames)
        {
            result &= new File(fileName).delete();
        }
        return result;
    }
    public String getChecksum(String fileName)
    {
        try
        {
            FileChannel ch = new FileInputStream(fileName).getChannel();
            ByteBuffer bb = ByteBuffer.allocateDirect(1024 * 1024);

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
        catch (NoSuchAlgorithmException | IOException e)
        {
            return "MD5 could not be calculated";
        }
    }
    public static String toHexString(byte[] array)
    {
        return DatatypeConverter.printHexBinary(array);
    }
    public static byte[] toHexByteArray(String s)
    {
        return DatatypeConverter.parseHexBinary(s);
    }
}
