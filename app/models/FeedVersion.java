package models;

import java.io.File;
import java.util.Date;
import java.util.List;

import utils.DataStore;

import com.conveyal.gtfs.validator.json.FeedValidationResult;

/**
 * Represents a version of a feed.
 * @author mattwigway
 *
 */
public class FeedVersion extends Model {
    private static DataStore<FeedVersion> versionStore = new DataStore<FeedVersion>("feedversions");
    
    /** The feed source this is associated with */
    public String feedSourceId;
    
    public FeedSource getFeedSource () {
        return FeedSource.get(feedSourceId);
    }
    
    public void setFeedSource (FeedSource source) {
        this.feedSourceId = source.id;
    }

    /** The file on the file system */
    public String feedFileId;
    
    /** The MD5 sum of the feed file, for quick checking if the file has been updated */
    public String md5sum;
    
    public File getFeed() {
        // TODO: hacked together
        return new File(feedFileId);
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
}
