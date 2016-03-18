package models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;

import org.mapdb.Fun.Function2;
import org.mapdb.Fun.Tuple2;

import play.Logger;
import play.Play;
import utils.DataStore;
import utils.FeedStore;
import utils.HashUtils;

import com.conveyal.gtfs.validator.json.FeedProcessor;
import com.conveyal.gtfs.validator.json.FeedValidationResult;
import com.conveyal.gtfs.validator.json.LoadStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

import static utils.StringUtils.getCleanName;

/**
 * Represents a version of a feed.
 * @author mattwigway
 *
 */
@JsonInclude(Include.ALWAYS)
public class FeedVersion extends Model implements Serializable {
    private static final long serialVersionUID = 1L;

    static DataStore<FeedVersion> versionStore = new DataStore<FeedVersion>("feedversions");
    private static FeedStore feedStore = new FeedStore();
    
    static {
        // set up indexing on feed versions by feed source, indexed by <FeedSource ID, version>
        versionStore.secondaryKey("version", new Function2<Tuple2<String, Integer>, String, FeedVersion> () {
            @Override
            public Tuple2<String, Integer> run(String key, FeedVersion fv) {
                return new Tuple2(fv.feedSourceId, fv.version);
            }            
        });
    }
    
    /**
     * We generate IDs manually, but we need a bit of information to do so
     */
    public FeedVersion (FeedSource source) {
        this.updated = new Date();
        this.feedSourceId = source.id;
        
        // ISO time
        DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmssX");
        
        // since we store directly on the file system, this lets users look at the DB directly
        this.id = getCleanName(source.name) + "_" + df.format(this.updated) + "_" + source.id + ".zip";
        
        // infer the version
        FeedVersion prev = source.getLatest();
        if (prev != null) {
            this.version = prev.version + 1;
        }
        else {
            this.version = 1;
        }
    }

    /**
     * Create an uninitialized feed version. This should only be used for dump/restore.
     */
    public FeedVersion () {
        // do nothing
    }
    
    /** The feed source this is associated with */
    @JsonView(JsonViews.DataDump.class)
    public String feedSourceId;
    
    @JsonView(JsonViews.UserInterface.class)
    public FeedSource getFeedSource () {
        return FeedSource.get(feedSourceId);
    }
    
    @JsonIgnore
    public FeedVersion getPreviousVersion () {
        return versionStore.find("version", new Tuple2(this.feedSourceId, this.version - 1));
    }
    
    @JsonView(JsonViews.UserInterface.class)
    public String getPreviousVersionId () {
        FeedVersion p = getPreviousVersion();
        return p != null ? p.id : null;
    }
    
    @JsonIgnore
    public FeedVersion getNextVersion () {
        return versionStore.find("version", new Tuple2(this.feedSourceId, this.version + 1));
    }
    
    @JsonView(JsonViews.UserInterface.class)
    public String getNextVersionId () {
        FeedVersion p = getNextVersion();
        return p != null ? p.id : null;
    }
    
    /** The hash of the feed file, for quick checking if the file has been updated */
    @JsonView(JsonViews.DataDump.class)
    public String hash;
    
    @JsonIgnore
    public File getFeed() {
        return feedStore.getFeed(id);
    }
    
    public File newFeed(InputStream inputStream) {
        return feedStore.newFeed(id, inputStream, getFeedSource());
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

    public void save () {
        save(true);
    }
    
    public void save(boolean commit) {
        if (commit)
            versionStore.save(this.id, this);
        else
            versionStore.saveWithoutCommit(this.id, this);
    }

    public void hash () {
        this.hash = HashUtils.hashFile(getFeed());
    }

    public static void commit() {
        versionStore.commit();
    }

    /**
     * Does this feed version have any critical errors that would prevent it being loaded to OTP?
     * @return
     */
    public boolean hasCriticalErrors() {
        if (hasCriticalErrorsExceptingDate() || (new Date()).after(validationResult.endDate))
            return true;
        
        else
            return false;
    }
    
    /**
     * Does this feed have any critical errors other than possibly being expired?
     */
    public boolean hasCriticalErrorsExceptingDate () {
        if (validationResult == null)
            return true;
        
        if (validationResult.loadStatus != LoadStatus.SUCCESS)
            return true;
        
        if (validationResult.stopTimesCount == 0 || validationResult.tripCount == 0 || validationResult.agencyCount == 0)
            return true;
        
        return false;
    }
    

    /**
     * Delete this feed version.
     */
    public void delete() {
        File feed = getFeed();
        if (feed.exists())
            feed.delete();
                
        for (Deployment d : Deployment.getAll()) {
            d.feedVersionIds.remove(this.id);
        }
        
        versionStore.delete(this.id);
    }
}
