package models;

import java.util.List;

import utils.DataStore;


/**
 * This represents where a feed comes from/came from.
 * @author mattwigway
 *
 */
public class FeedSource extends Model {    
    private static DataStore<FeedSource> sourceStore = new DataStore<FeedSource>("feedsources");
    
    /** The name of this feed source, e.g. MTA New York City Subway */
    public String name;
    
    /** Is this feed public, i.e. should it be placed in deployments and listed on the
     * public feeds page for download?
     */
    public boolean isPublic;
    
    public void save () {
        sourceStore.save(this.id, this);
    }
    
    /**
     * Get the latest version of this feed
     * @return the latest version of this feed
     */
    //public FeedVersion getLatest () {
        
    //}
    
    public static FeedSource get(String id) {
        return sourceStore.getById(id);
    }
}
