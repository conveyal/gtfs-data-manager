package utils;

import controllers.Application;
import org.mapdb.*;
import org.mapdb.Fun.Function2;
import play.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class DataStore<T> {

    DB db;
    BTreeMap<String,T> map;

    public DataStore(String dataFile) {

        this(new File(Application.dataPath), dataFile);
    }

    public DataStore(File directory, String dataFile) {

        if(!directory.exists())
            directory.mkdirs();

        try {
            Logger.info(directory.getCanonicalPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        db = DBMaker.fileDB(new File(directory, dataFile + ".db"))
                .cacheHashTableEnable()
                .closeOnJvmShutdown()
                .make();

        DB.BTreeMapMaker maker = db.treeMapCreate(dataFile);
        //TODO specify value serializer, add constructor argument?
        maker.valueSerializer(new ClassLoaderSerializer());
        maker.keySerializer(Serializer.STRING);
        map = maker.makeOrGet();
    }

    public DataStore(File directory, String dataFile, List<Fun.Pair<String,T>>inputData) {

        if(!directory.exists())
            directory.mkdirs();

        try {
            Logger.info(directory.getCanonicalPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        db = DBMaker.fileDB(new File(directory, dataFile + ".db"))
                .transactionDisable()
                .cacheHashTableEnable()
                .closeOnJvmShutdown()
                .make();

        Comparator<Fun.Pair<String, T>> comparator = new Comparator<Fun.Pair<String,T>>(){

            @Override
            public int compare(Fun.Pair<String, T> o1,
                               Fun.Pair<String, T> o2) {
                return o1.a.compareTo(o2.a);
            }
        };

        // need to reverse sort list
        Iterator<Fun.Pair<String,T>> iter = Pump.sort(inputData.iterator(),
                true, 100000,
                Collections.reverseOrder(comparator), //reverse  order comparator
                db.getDefaultSerializer(),
                null
                );


        map = db.treeMapCreate(dataFile)
                .pumpSource(iter)
                .pumpPresort(100000)
                //TODO specify value serializer, add constructor argument?
                .keySerializer(Serializer.STRING)
                .make();



        // close/flush db 
        db.close();

        // re-connect with transactions enabled
        db = DBMaker.fileDB(new File(directory, dataFile + ".db"))
                .cacheHashTableEnable()
                .closeOnJvmShutdown()
                .make();

        map = db.treeMapCreate(dataFile)
                //TODO is expiration option restored here?
                .keySerializer(Serializer.STRING)
                .makeOrGet();
    }

    public void save(String id, T obj) {
        map.put(id, obj);
        db.commit();
    }

    public void saveWithoutCommit(String id, T obj) {
        map.put(id, obj);
    }

    public void commit() {
        db.commit();
    }

    public void delete(String id) {
        map.remove(id);
        db.commit();
    }

    public T getById(String id) {
        return map.get(id); 
    }
    
    /**
     * Does an object with this ID exist in this data store?
     * @param id
     * @return boolean indicating result
     */
    public boolean hasId(String id) {
        return map.containsKey(id);
    }

    public Collection<T> getAll() {
        return map.values();
    }

    public Integer size() {
        return map.keySet().size();
    }
    
    /** Create a secondary (unique) key */
    public <K2> void secondaryKey (String name, BTreeKeySerializer keySerializer, Function2<K2, String, T> fun) {
        Map<K2, String> index = db.treeMapCreate(name)
                .keySerializer(keySerializer)
                .valueSerializer(Serializer.STRING)
                .makeOrGet();
        Bind.secondaryKey(map, index, fun);
    }
    
    /** search using a secondary unique key */
    public <K2> T find(String name, K2 value) {
        Map<K2, String> index = db.treeMapCreate(name)
                .valueSerializer(Serializer.STRING)
                .makeOrGet();

        String id = index.get(value);
        
        if (id == null)
            return null;
        
        return map.get(id);
    }
    
    /** find the value with largest key less than or equal to key */
    public <K2> T findFloor (String name, K2 floor) {
        BTreeMap<K2, String> index = db.treeMapCreate(name)
                .valueSerializer(Serializer.STRING)
                .makeOrGet();
        
        Entry<K2, String> key = index.floorEntry(floor);
        
        if (key == null || key.getValue() == null)
            return null;
        
        return map.get(key.getValue());
    }
}
