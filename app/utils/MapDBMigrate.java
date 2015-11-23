package utils;

import akka.util.Helpers;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import play.test.FakeApplication;
import play.test.WithApplication;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * Migrates data store from MapDB 1.0 format to 2.0 format.
 */
public class MapDBMigrate {


    static final String[] STORE_NAMES = new String[]{"deployments",
            "feedcollections", "feedsources", "feedversions",
            "notes", "users"};

    public static class ClassLoaderSerializer10 implements org.mapdb10.Serializer<Object>, Serializable {
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

        @Override
        public int fixedSize() {
            return -1;
        }
    }

    static class App extends WithApplication{
        @Override
        protected FakeApplication provideFakeApplication() {
            File tmpDir = new File(System.getProperty("java.io.tmpdir")+"/mapdbtest"+System.currentTimeMillis());
            tmpDir.mkdirs();
            return new FakeApplication(new java.io.File("."), Helpers.class.getClassLoader(),
                    ImmutableMap.of("application.data.mapdb",tmpDir.getAbsolutePath()), new ArrayList<String>(), null);
        }
    }

    public static void main(String[] args) {
        App a = new App();
        a.startPlay();

        if (args == null || args.length != 2) {
            System.out.println("Parameters: input (MapDB 1 file)   output (MapDB 2 file)");
            return;
        }
        File db1File = new File(args[0]);
        if (!db1File.exists()) {
            System.out.println("Input MapDB 1 file does not exists");
            return;
        }

        File db2File = new File(args[1]);
        if (db2File.exists()) {
            System.out.println("Output MapDB 2 file already exists");
            return;
        }

        db2File.mkdirs();

        for(String name:STORE_NAMES){
            org.mapdb10.DB db1 = org.mapdb10.DBMaker
                    .newFileDB(new File(db1File,name+".db"))
                    .make();

            //override value serializer
            db1.getCatalog().put(name+".valueSerializer", new ClassLoaderSerializer10());

            Map<String,Object> map1  = db1.getTreeMap(name);

            DataStore s = new DataStore(db2File, name);

            int counter = 0;
            for(Map.Entry<String, Object> e:map1.entrySet()){
                s.map.put(e.getKey(),e.getValue());
                counter++;
            }

            System.out.println("Converted "+counter+" in "+name);

            db1.rollback();
            db1.close();
            new File(db1File.getPath(),name+".db.t").delete();

            s.db.commit();
            s.db.close();

        }
        a.stopPlay();

    }
}