/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsync;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Administrator
 */
public class Delta
{
    public ArrayList<Integer> deltas;
    public Delta (ArrayList<Integer> deltas)
    {
        this.deltas=deltas;
    }
    static Delta generateDelta(Checksum liveFile, Checksum testFile)
    {
        if(liveFile.BUFFER_SIZE!=testFile.BUFFER_SIZE)
        {
            System.out.println("Buffer sizes have to be equal on checksum files");
            return null;
        }
        ArrayList<Integer> deltas = new ArrayList<>();
        int smallest = Math.min(liveFile.checksums.size(), testFile.checksums.size());
        boolean modeEnteredNewList = false, modeLongList = false;
        for(int i=0;i<smallest;i++)
        {
            if(!liveFile.checksums.get(i).equals(testFile.checksums.get(i)))
            {
                if(!modeEnteredNewList)
                {
                    modeEnteredNewList=true;
                    deltas.add(i);
                }
                else if(!modeLongList)
                {
                    modeLongList=true;
                    deltas.add(-1);
                }
            }
            else
            {
                if(modeEnteredNewList&&modeLongList)
                {
                    deltas.add(i-1);
                }
                modeEnteredNewList=modeLongList=false;
            }
        }
        if(modeEnteredNewList&&modeLongList)
        {
            deltas.add(smallest-1);
        }
        return new Delta(deltas);
    }
    
}
