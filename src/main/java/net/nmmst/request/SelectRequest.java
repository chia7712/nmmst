package net.nmmst.request;

import java.io.Serializable;
import java.util.Arrays;

public class SelectRequest  implements Serializable
{
    private static final long serialVersionUID = -4511120394426638697L;
    private final int[] indexs;
    private final boolean[] values;
    public SelectRequest(int[] indexs, boolean[] values)
    {
        this.indexs = Arrays.copyOf(indexs, indexs.length);
        this.values = Arrays.copyOf(values, values.length);
    }
    public int[] getIndexs()
    {
        return Arrays.copyOf(indexs, indexs.length);
    }
    public boolean[] getValues()
    {
        return Arrays.copyOf(values, values.length);
    }
}
