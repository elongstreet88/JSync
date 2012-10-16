/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsync;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import tools.Tools;

/**
 *
 * @author Administrator
 */
public class ChecksumFile extends Tools
{
    int BUFFER_SIZE;
    long size;
    String name;
    String checksum;
    ArrayList<String> checksums = new ArrayList<>();
    private ChecksumFile(int BUFFER_SIZE, long size, String name, String checksum, ArrayList<String> checksums)
    {
        this.BUFFER_SIZE = BUFFER_SIZE;
        this.size = size;
        this.name = name;
        this.checksum = checksum;
        this.checksums = checksums;
    }
    public ChecksumFile(String checksumFile)
    {
        this.name = checksumFile;
        try (FileChannel checksumFileChannel = new FileInputStream(checksumFile).getChannel())
        {
            ByteBuffer bb = ByteBuffer.allocateDirect(32);
            int nRead;
            while ((nRead = checksumFileChannel.read(bb)) != -1)
            {
                if (nRead == 0)
                {
                    continue;
                }
                break;
            }
            bb.position(0).limit(nRead);

            //Read Header
            this.BUFFER_SIZE = bb.getInt();
            this.size = bb.getLong();
            bb.position(16);//Skip reserved
            
            byte[] checksumBytes = new byte[16];
            bb.get(checksumBytes);
            this.checksum = getChecksumString(checksumBytes);
            
            //load cheksums
            ByteBuffer readBytes = ByteBuffer.allocateDirect(BUFFER_SIZE);
            byte[] thisChecksum = new byte[16];
            while ((nRead = checksumFileChannel.read(readBytes)) != -1)
            {
                if (nRead == 0)
                {
                    continue;
                }
                readBytes.flip();
                while (readBytes.hasRemaining())
                {
                    readBytes.get(thisChecksum);
                    checksums.add((new HexBinaryAdapter()).marshal(thisChecksum));
                }
                readBytes.flip();
             }
            
        }
        catch (Exception ex)
        {
            Logger.getLogger(ChecksumFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static ChecksumFile generateChecksumFile(String fileName, int BUFFER_SIZE)
    {
        if(BUFFER_SIZE<16||BUFFER_SIZE%16!=0)
        {
            System.out.println("Error with Buffer Size "+BUFFER_SIZE);
        }
        String checksumFileName = fileName + ".checksum.bin";
        delete(checksumFileName);
        try (FileChannel sourceFileChannel = new FileInputStream(fileName).getChannel(); FileChannel checksumFileChannel = new RandomAccessFile(checksumFileName, "rw").getChannel())
        {
            ArrayList<String> checksums = new ArrayList<>();
            MessageDigest fileChecksum = MessageDigest.getInstance("MD5");
            MessageDigest rollingChecksum = MessageDigest.getInstance("MD5");
            
            long size = sourceFileChannel.size();
            
            checksumFileChannel.write(convert(BUFFER_SIZE));//4
            checksumFileChannel.write(convert(size));//8
            checksumFileChannel.write(ByteBuffer.allocate(4));//4 to pad
            
            checksumFileChannel.write(ByteBuffer.allocate(16));//16 pad reserved for checksum later
            
            ByteBuffer readBytes = ByteBuffer.allocateDirect(BUFFER_SIZE);
            ByteBuffer hashBytes = ByteBuffer.allocateDirect(BUFFER_SIZE);
            
            int nRead;
            while ((nRead = sourceFileChannel.read(readBytes)) != -1)
            {
                if (nRead == 0)
                {
                    continue;
                }
                readBytes.flip();
                while (readBytes.hasRemaining())
                {
                    //update rolling chekcsum
                    rollingChecksum.update(readBytes);
                    byte[] rollingBytes = rollingChecksum.digest();
                    checksums.add(getChecksumString(rollingBytes));
                    hashBytes.put(ByteBuffer.wrap(rollingBytes));
                    //update file checksum
                    readBytes.flip();
                    fileChecksum.update(readBytes);
                    //flush buffer if full
                    if(!hashBytes.hasRemaining())
                    {
                        hashBytes.flip();
                        checksumFileChannel.write(hashBytes);
                        hashBytes.clear();
                        
                    }
                }
                readBytes.clear();
            }
            //flush leftovers
             hashBytes.flip();
             checksumFileChannel.write(hashBytes);
             hashBytes.clear();
                        
            //write back md5 in beginiing
            byte[] fileChecksumBytes = fileChecksum.digest();
            checksumFileChannel.position(16);
            checksumFileChannel.write(ByteBuffer.wrap(fileChecksumBytes));
            return new ChecksumFile(BUFFER_SIZE,size,checksumFileName,getChecksumString(fileChecksumBytes),checksums);
        }
        catch (Exception e)
        {
            System.out.println("Error creating "+checksumFileName);
            return null;
        }

    }
}
