/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsync;

import tools.FileActions;
import tools.Tools;

/**
 *
 * @author Administrator
 */
public class Runner
{
    public static void main(String[] args)
    {
        FileActions fa = new FileActions();
        //System.out.println(fa.getChecksum("C:/block/block1.bin"));
        
        Checksum file1 = Checksum.generateChecksumFile("C:/block/block1.bin", 1024*1024,true);
        Checksum file2 = Checksum.generateChecksumFile("C:/block/block2.bin", 1024*1024,true);

        
        
        Delta delta = Delta.generateDelta(file1,file2);
        System.out.println(delta);
        //boolean result = Tools.delete(file1.getChecksumFileName(),file2.getChecksumFileName());
        
        System.out.println(file1);
    }
}
