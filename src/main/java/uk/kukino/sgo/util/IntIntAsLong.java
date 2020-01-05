package uk.kukino.sgo.util;

public class IntIntAsLong
{

    public static long enc(final int left, final int right)
    {
        return ((long) left << 32) + (right & (long) 0xffffffff);
    }

    public static int left(final long encoded)
    {
        return (int) ((encoded >>> 32) & 0xffffffff);
    }

    public static int right(final long encoded)
    {
        return (int) (encoded & (long) 0xffffffff);
    }

}
