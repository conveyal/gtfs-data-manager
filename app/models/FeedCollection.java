package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import utils.DataStore;

/**
 * Represents a collection of feed sources that can be made into a deployment.
 * Generally, this would represent one agency that is managing the data.
 * For now, there is one FeedCollection per instance of GTFS data manager, but
 * we're trying to write the code in such a way that this is not necessary.
 * 
 * @author mattwigway
 *
 */
@JsonInclude(Include.ALWAYS)
public class FeedCollection extends Model implements Serializable {
    private static DataStore<FeedCollection> collectionStore = new DataStore<FeedCollection>("feedcollections");
    
    /** The name of this feed collection, e.g. NYSDOT. */
    public String name;

    public Boolean useCustomOsmBounds;

    public Double osmNorth, osmSouth, osmEast, osmWest;
    
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
        save(true);
    }
        
    public void save(boolean commit) {
        if (commit)
            collectionStore.save(this.id, this);
        else
            collectionStore.saveWithoutCommit(this.id, this);
    }
    
    public static void commit () {
        collectionStore.commit();
    }
    
    /**
     * Get all the feed sources for this feed collection
     */
    @JsonIgnore
    public Collection<FeedSource> getFeedSources () {
        ArrayList<FeedSource> ret = new ArrayList<FeedSource>();
        
        // TODO: use index, but not important for now because we generally only have one FeedCollection
        for (FeedSource fs : FeedSource.getAll()) {
            if (this.id.equals(fs.feedCollectionId)) {
                ret.add(fs);
            }
        }
        
        return ret;
    }
    
    /**
     * Get all the deployments for this feed collection
     */
    @JsonIgnore
    public Collection<Deployment> getDeployments () {
        ArrayList<Deployment> ret = new ArrayList<Deployment>();
        
        for (Deployment d : Deployment.getAll()) {
            if (this.id.equals(d.feedCollectionId)) {
                ret.add(d);
            }
        }
        
        return ret;
    }
}
