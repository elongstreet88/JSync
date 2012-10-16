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
        
        ChecksumFile file1 = ChecksumFile.generateChecksumFile("C:/block/block1.bin", 1024*1024);
        ChecksumFile file2 = ChecksumFile.generateChecksumFile("C:/block/block2.bin", 1024*1024);

        
        
        Delta delta = Delta.generateDelta(file1,file2);
        boolean result = Tools.delete(file1.name,file2.name);
        
        System.out.println(file1);
    }
}
