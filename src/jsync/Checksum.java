/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsync;


import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import tools.Tools;

/**
 *
 * @author Administrator
 */
public class Checksum extends Tools
{
    int BUFFER_SIZE;
    long size;
    String originalFileName;
    private String checksumFileName;
    String checksum;
    ArrayList<String> checksums = new ArrayList<>();
    private boolean fileBacked;
    
    public boolean isFileBacked()
    {
        return fileBacked;
    }
    public String getChecksumFileName()
    {
        if(!isFileBacked())
        {
            System.out.println(originalFileName +" checksums not backed by a file");
            return null;
        }
        return checksumFileName;
    }
    private Checksum(int BUFFER_SIZE, long size, String originalFileName, String checksum, ArrayList<String> checksums, boolean createBackingFile)
    {
        this.BUFFER_SIZE = BUFFER_SIZE;
        this.size = size;
        this.originalFileName = originalFileName;
        this.checksum = checksum;
        this.checksums = checksums;
        fileBacked = createBackingFile;
        if(fileBacked)
        {
            writeChecksumFile();
        }
    }
    public Checksum(String checksumFile)
    {
        this.originalFileName = checksumFile;
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
            this.checksum = toHexString(checksumBytes);
            
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
            Logger.getLogger(Checksum.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public final void writeChecksumFile()
    {
        this.checksumFileName = this.originalFileName + ".checksum.bin";
        delete(checksumFileName);
        try(FileChannel checksumFileChannel = new RandomAccessFile(checksumFileName, "rw").getChannel())
        {
            checksumFileChannel.write(convert(BUFFER_SIZE));//4
            checksumFileChannel.write(convert(size));//8
            checksumFileChannel.write(ByteBuffer.allocate(4));//4 to pad
            checksumFileChannel.write(ByteBuffer.wrap(checksum.getBytes()));
            
            ByteBuffer hashBytes = ByteBuffer.allocateDirect(BUFFER_SIZE);
            for(String thisChecksum:checksums)
            {
                hashBytes.put(toHexByteArray(thisChecksum));
                if(!hashBytes.hasRemaining())
                {
                    hashBytes.flip();
                    checksumFileChannel.write(hashBytes);
                    hashBytes.clear();
                }
            }
            //Flush leftovers
            hashBytes.flip();
            checksumFileChannel.write(hashBytes);
        }
        catch (Exception e)
        {
            System.out.println("Error creating "+checksumFileName);
        }
    }
    public static Checksum generateChecksumFile(String fileName, int BUFFER_SIZE, boolean createChecksumFile)
    {
        if(BUFFER_SIZE<16||BUFFER_SIZE%16!=0)
        {
            System.out.println("Error with Buffer Size "+BUFFER_SIZE);
            return null;
        }
        try (FileChannel sourceFileChannel = new FileInputStream(fileName).getChannel();)
        {
            ArrayList<String> checksums = new ArrayList<>();
            
            MessageDigest rollingChecksum = MessageDigest.getInstance("MD5");
            MessageDigest blockChecksum = MessageDigest.getInstance("MD5");
            
            ByteBuffer readBytes = ByteBuffer.allocateDirect(BUFFER_SIZE);
            
            int nRead;
            while ((nRead = sourceFileChannel.read(readBytes)) != -1)
            {
                if (nRead == 0)
                {
                    continue;
                }
                //update rolling chekcsum
                readBytes.flip();
                while (readBytes.hasRemaining())
                {
                    blockChecksum.update(readBytes);
                }
                checksums.add(toHexString(blockChecksum.digest()));
                //update file checksum
                readBytes.flip();
                while (readBytes.hasRemaining())
                {
                    rollingChecksum.update(readBytes);
                }
                readBytes.clear();
            }
            return new Checksum(BUFFER_SIZE,sourceFileChannel.size(),fileName,toHexString(rollingChecksum.digest()),checksums,createChecksumFile);
        }
        catch (Exception e)
        {
            System.out.println("Error creating checksums for "+fileName);
            return null;
        }

    }
}
