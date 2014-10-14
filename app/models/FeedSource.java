package models;

import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.Logger;
import utils.DataStore;


/**
 * This represents where a feed comes from/came from.
 * @author mattwigway
 *
 */
public class FeedSource extends Model {    
    private static DataStore<FeedSource> sourceStore = new DataStore<FeedSource>("feedsources");
    
    /**
     * The collection of which this feed is a part
     */
    @JsonIgnore
    public String feedCollectionId;
    
    /**
     * Get the FeedCollection of which this feed is a part
     */
    @JsonIgnore
    public FeedCollection getFeedCollection () {
        return FeedCollection.get(feedCollectionId);
    }
    
    /** The name of this feed source, e.g. MTA New York City Subway */
    public String name;
    
    /** Is this feed public, i.e. should it be placed in deployments and listed on the
     * public feeds page for download?
     */
    public boolean isPublic;
    
    /**
     * Do we fetch this feed automatically?
     */
    public boolean autofetch;
    
    /**
     * When was this feed last fetched?
     */
    public Date lastFetched;
    
    /**
     * When was this feed last updated?
     */
    public Date lastUpdated;
    
    /**
     * How often is this feed fetched (in minutes)?
     */
    public Integer fetchFrequency;
    
    /**
     * From whence is this feed fetched?
     */
    public URL url;
    
    /**
     * Fetch the latest version of the feed.
     */
    public void fetch () {
        Logger.error("Feed fetch is unimplemented");
    }
    
    public void save () {
        sourceStore.save(this.id, this);
    }
    
    /**
     * Get the latest version of this feed
     * @return the latest version of this feed
     */
    // note: no JsonIgnore because we use the details on the latest feed validation in the overview.
    //public FeedVersion getLatest () {
        
    //}
    
    public static FeedSource get(String id) {
        return sourceStore.getById(id);
    }

    public static Collection<FeedSource> getAll() {
        return sourceStore.getAll();
    }
}
