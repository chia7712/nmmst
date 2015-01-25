package net.nmmst.movie;


public class Sample 
{
    private final byte[]  data;
    private final MovieAttribute attribute;
    public Sample(MovieAttribute attribute, byte[] data)
    {
        this.attribute = attribute;
        this.data = data;
    }
    public MovieAttribute getMovieAttribute()
    {
        return attribute;
    }
    public byte[] getData()
    {
        return data;
    }
    public static Sample newNullSample()
    {
        return new Sample(null, null);
    }
}
