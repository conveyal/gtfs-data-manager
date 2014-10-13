package models;

import java.io.Serializable;

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
    private String name;
    
    /**
     * Get the ID of this FeedCollection.
     */
    public String getId () {
        return name;
    }

    /**
     * Set the name of this feed collection. This can only be called once.
     */
    public void setName (String name) {
        // make sure there isn't already a feedcollection by this name
        if (collectionStore.hasId(name)) {
            throw new IllegalArgumentException("FeedCollection with name " + name + " already exists.");
        }
        
        if (this.name != null) {
            throw new IllegalArgumentException("Name for this feed collection has already been set");
        }
        
        this.name = name;
    }
    
    /**
     * Get the name of this feed collection.
     */
    
    public static FeedCollection get(String id) {
        return collectionStore.getById(id);
    }

    public void save() {
        if (this.name == null) {
            throw new IllegalStateException("Please set a name for this feedcollection before saving it");
        }
        
        collectionStore.save(this.name, this);
    }
}
