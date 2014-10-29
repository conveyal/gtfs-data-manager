package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

import utils.DataStore;

/**
 * A deployment of (a given version of) OTP on a given set of feeds.
 * @author mattwigway
 *
 */
@JsonInclude(Include.ALWAYS)
public class Deployment extends Model {
    private static DataStore<Deployment> deploymentStore = new DataStore<Deployment>("deployments");
    
    @JsonView(JsonViews.DataDump.class)
    public String feedCollectionId;
    
    @JsonView(JsonViews.UserInterface.class)
    public FeedCollection getFeedCollection () {
        return FeedCollection.get(feedCollectionId);
    }
    
    public void setFeedCollection (FeedCollection feedCollection) {
        this.feedCollectionId = feedCollection.id;
    }
    
    @JsonView(JsonViews.DataDump.class)
    public Collection<String> feedVersionIds;
    
    // future use
    public String osmFileId;
    
    /** The commit of OTP being used on this deployment */
    public String otpCommit;
    
    /** All of the feed versions used in this deployment */
    @JsonView(JsonViews.UserInterface.class)
    public Collection<FeedVersion> getFeedVersions () {
        ArrayList<FeedVersion> ret = new ArrayList<FeedVersion>(feedVersionIds.size());
        
        for (String id : feedVersionIds) {
            ret.add(FeedVersion.get(id));
        }
        
        return ret;
    }
    
    public void setFeedVersions (Collection<FeedVersion> versions) {
        feedVersionIds = new ArrayList<String>(versions.size());
        
        for (FeedVersion version : versions) {
            feedVersionIds.add(version.id);
        }
    }
    
    /** Create a new deployment plan for the given feed collection */
    public Deployment (FeedCollection feedCollection) {
        super();
        
        this.setFeedCollection(feedCollection);
        
        this.feedVersionIds = new ArrayList<String>();
        
        FEEDSOURCE: for (FeedSource s : feedCollection.getFeedSources()) {
            // only include public feeds
            if (s.isPublic) {
                FeedVersion latest = s.getLatest();
                
                // find the newest version that can be deployed
                while (latest.hasCriticalErrors()) {
                    latest = latest.getPreviousVersion();
                    
                    // silently ignore feed sources that have no valid versions
                    // TODO: expose this to the UI
                    if (latest == null) {
                        continue FEEDSOURCE;
                    }
                }

                // this version is the latest good version
                this.feedVersionIds.add(latest.id);
            }
        }
    }
    
    /**
     * Create an empty deployment, for use with dump/restore.
     */
    public Deployment () {
        // do nothing.
    }
    
    /** Get a deployment by ID */
    public static Deployment get (String id) {
        return deploymentStore.getById(id);
    }
    
    /** Save this deployment and commit it */
    public void save () {
        this.save(true);
    }
    
    /** Save this deployment */
    public void save (boolean commit) {
        if (commit)
            deploymentStore.save(id, this);
        else
            deploymentStore.saveWithoutCommit(id, this);
    }
    
    /**
     * Commit changes to the datastore
     */
    public static void commit () {
        deploymentStore.commit();
    }
    
    /**
     * Get all of the deployments.
     */
    public static Collection<Deployment> getAll () {
        return deploymentStore.getAll();
    }
}
