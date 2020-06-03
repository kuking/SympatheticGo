package uk.kukino.sgo.valuators;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.agrona.collections.Long2ObjectHashMap;

import java.io.*;
import java.util.Collection;
import java.util.Map;

/***
 * A serialisable collection of heatmaps, handles storing/retrieving. Key is a long, so you have to fit your key within it, i.e. if
 * previous/next move is meant to be used, they should be bit-shifted into a long.
 */
public class Heatmaps implements Serializable
{

    private static final String FILE_SIGNATURE = "\r\033[2K" +
        "SympatheticGo Heatmap file\n" +
        "https://github.com/kuking/SympatheticGo\n" +
        "version=0.1\n" +
        "\n";

    private Long2ObjectHashMap<Heatmap> heatmaps;

    public Heatmaps()
    {
        initialize();
    }

    private void initialize()
    {
        heatmaps = new Long2ObjectHashMap<>();
    }

    public Heatmap get(final long key)
    {
        return heatmaps.get(key);
    }

    public Heatmap put(final long key, final Heatmap heatmap)
    {
        return heatmaps.put(key, heatmap);
    }

    public Long2ObjectHashMap.KeySet keys()
    {
        return heatmaps.keySet();
    }

    public Collection<Heatmap> values()
    {
        return heatmaps.values();
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException
    {
        oos.writeUTF(FILE_SIGNATURE);
        oos.writeInt(heatmaps.size());
        for (final Map.Entry<Long, Heatmap> entry : heatmaps.entrySet())
        {
            oos.writeLong(entry.getKey());
            oos.writeObject(entry.getValue());
        }
    }

    private void readObject(final ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        final String objSignature = ois.readUTF();
        if (!FILE_SIGNATURE.equals(objSignature))
        {
            throw new IOException("Object/File format not supported.");
        }
        this.initialize();
        final int size = ois.readInt();
        for (int i = 0; i < size; i++)
        {
            heatmaps.put(ois.readLong(), (Heatmap) ois.readObject());
        }
    }

    private void readObjectNoData() throws ObjectStreamException
    {
        heatmaps.clear();
    }

    public boolean writeToFile(final String filename)
    {
        try (FileOutputStream fout = new FileOutputStream(filename);
             XZCompressorOutputStream xzout = new XZCompressorOutputStream(fout);
             ObjectOutputStream oos = new ObjectOutputStream(xzout))
        {
            oos.writeObject(this);
            return true;
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static Heatmaps loadFromStream(final InputStream in)
    {
        try (XZCompressorInputStream xzin = new XZCompressorInputStream(in);
             ObjectInputStream ois = new ObjectInputStream(xzin))
        {
            return (Heatmaps) ois.readObject();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        catch (final ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Heatmaps loadFromFile(final String filename)
    {
        try (FileInputStream fin = new FileInputStream(filename))
        {
            return loadFromStream(fin);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Heatmaps loadFromResources(final String name)
    {
        try
        {
            return loadFromStream(Heatmaps.class.getResource(name).openStream());
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }


}
