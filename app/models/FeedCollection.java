package models;

import java.io.Serializable;
import java.util.Collection;

import utils.DataStore;

/**
 * Represents a collection of feed sources that can be made into a deployment.
 * Generally, this would represent one agency that is managing the data.
 * For now, there is one FeedCollection per deployment, but we're trying to write the code in such a way that this
 * is not necessary.
 * 
 * @author mattwigway
 *
 */
public class FeedCollection extends Model implements Serializable {
    private static DataStore<FeedCollection> collectionStore = new DataStore<FeedCollection>("feedcollections");
    
    /** The name of this feed collection, e.g. NYSDOT. */
    public String name;
    
    /**
     * Get all of the FeedCollections that are defined
     */
    public static Collection<FeedCollection> getAll () {
        return collectionStore.getAll();
    }
    
    public static FeedCollection get(String id) {
        return collectionStore.getById(id);
    }

    public void save() {
        collectionStore.save(this.id, this);
    }
}
