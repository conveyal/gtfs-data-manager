package utils;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.mapdb.Serializer;

import java.io.*;

/**
 * Deserialize using the thread's class loader, not the root class loader.
 */
public class ClassLoaderSerializer extends Serializer<Object> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public void serialize(DataOutput out, Object value) throws IOException {
        ObjectOutputStream out2 = new ObjectOutputStream((OutputStream) out);
        out2.writeObject(value);
        out2.flush();
    }

    @Override
    public Object deserialize(DataInput in, int available) throws IOException {
        try {
            ObjectInputStream in2 = new ClassLoaderObjectInputStream(Thread.currentThread().getContextClassLoader(), (InputStream) in);
            return in2.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

}
