package models;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import play.Logger;
import play.Play;
import utils.DataStore;
import utils.FeedStore;
import utils.HashUtils;

import com.conveyal.gtfs.validator.json.FeedProcessor;
import com.conveyal.gtfs.validator.json.FeedValidationResult;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a version of a feed.
 * @author mattwigway
 *
 */
public class FeedVersion extends Model {    
    private static DataStore<FeedVersion> versionStore = new DataStore<FeedVersion>("feedversions");
    private static FeedStore feedStore = new FeedStore(Play.application().configuration().getString("application.data.gtfs")); 
    
    /**
     * We generate IDs manually, but we need a bit of information to do so
     */
    public FeedVersion (FeedSource source) {
        this.updated = new Date();
        
        // ISO time
        DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmssX");
        
        // since we store directly on the file system, this lets users look at the DB directly
        this.id = getCleanName(source.name) + "_" + df.format(this.updated) + "_" + source.id + ".zip";
    }

    /**
     * Clean a name to make it filesystem-friendly
     * @param name a name with any letters
     * @return a new name with weird letters removed/transliterated.
     */
    public static String getCleanName (String name) {
        return name.replace(' ', '_').replaceAll("[^A-Za-z0-9_-]", "");
    }
    
    /** The feed source this is associated with */
    @JsonIgnore
    public String feedSourceId;
    
    public FeedSource getFeedSource () {
        return FeedSource.get(feedSourceId);
    }
    
    public void setFeedSource (FeedSource source) {
        this.feedSourceId = source.id;
    }
    
    /** The hash of the feed file, for quick checking if the file has been updated */
    @JsonIgnore
    public String hash;
    
    public File getFeed() {
        return feedStore.getFeed(id);
    }
    
    public File newFeed() {
        return feedStore.newFeed(id);
    }
    
    /** The results of validating this feed */
    public FeedValidationResult validationResult;
    
    /** When this feed was uploaded to or fetched by GTFS Data Manager */
    public Date updated;
    
    /** The version of the feed, starting with 0 for the first and so on */
    public int version;

    public static FeedVersion get(String id) {
        // TODO Auto-generated method stub
        return versionStore.getById(id);
    }

    public static Collection<FeedVersion> getAll() {
        return versionStore.getAll();
    }

    public void validate() { 
        File feed = getFeed();
        FeedProcessor fp = new FeedProcessor(feed);
        
        try {
            fp.run();
        } catch (IOException e) {
            Logger.error("Unable to validate feed {}", this);
            this.validationResult = null;
            return;
        }
        
        this.validationResult = fp.getOutput();
    }

    public void save() {
        versionStore.save(this.id, this);
    }

    public void hash () {
        this.hash = HashUtils.hashFile(getFeed());
    }
}
