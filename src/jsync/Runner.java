/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsync;

/**
 *
 * @author Administrator
 */
public class Runner
{
    public static void main(String[] args)
    {
        FileActions fa = new FileActions();
        fa.getChecksum("C:/block/block1.bin");
    }
}
