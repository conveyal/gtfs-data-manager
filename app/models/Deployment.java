package models;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import models.FeedSource.FeedRetrievalMethod;

import com.conveyal.gtfs.model.ValidationResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    
    public String name;
    
    public Date dateCreated;
    
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
    
    /** All of the feed versions used in this deployment */
    @JsonIgnore
    public Collection<FeedVersion> getFullFeedVersions () {
        ArrayList<FeedVersion> ret = new ArrayList<FeedVersion>(feedVersionIds.size());
        
        for (String id : feedVersionIds) {
            ret.add(FeedVersion.get(id));
        }
        
        return ret;
    }
    
    /** All of the feed versions used in this deployment, summarized so that the Internet won't break */
    @JsonView(JsonViews.UserInterface.class)
    public Collection<SummarizedFeedVersion> getFeedVersions () {
        ArrayList<SummarizedFeedVersion> ret = new ArrayList<SummarizedFeedVersion>(feedVersionIds.size());
        
        for (String id : feedVersionIds) {
            ret.add(new SummarizedFeedVersion(FeedVersion.get(id)));
        }
        
        return ret;
    }
    
    public void setFeedVersions (Collection<FeedVersion> versions) {
        feedVersionIds = new ArrayList<String>(versions.size());
        
        for (FeedVersion version : versions) {
            feedVersionIds.add(version.id);
        }
    }
    
    // future use
    public String osmFileId;
    
    /** The commit of OTP being used on this deployment */
    public String otpCommit;
    
    /**
     * Feed sources that had no valid feed versions when this deployment was created, and ergo were not added. 
     */
    @JsonView(JsonViews.DataDump.class)
    public Collection<String> invalidFeedSourceIds;
    
    /**
     * Get all of the feed sources which could not be added to this deployment.
     */
    @JsonView(JsonViews.UserInterface.class)
    public Collection<FeedSource> getInvalidFeedSources () {
        ArrayList<FeedSource> ret = new ArrayList<FeedSource>(invalidFeedSourceIds.size());
        
        for (String id : invalidFeedSourceIds) {
            ret.add(FeedSource.get(id));
        }
        
        return ret;
    }
    
    /** Create a new deployment plan for the given feed collection */
    public Deployment (FeedCollection feedCollection) {
        super();
        
        this.setFeedCollection(feedCollection);
        
        this.dateCreated = new Date();
        
        this.feedVersionIds = new ArrayList<String>();
        this.invalidFeedSourceIds = new ArrayList<String>();
        
        FEEDSOURCE: for (FeedSource s : feedCollection.getFeedSources()) {
            // only include public feeds
            if (s.isPublic) {
                FeedVersion latest = s.getLatest();
                                
                // find the newest version that can be deployed
                while (true) {                    
                    if (latest == null) {
                        invalidFeedSourceIds.add(s.id);
                        continue FEEDSOURCE;
                    }
                    
                    if (!latest.hasCriticalErrors()) {
                        break;
                    }
                    
                    latest = latest.getPreviousVersion();
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
    
    /**
     * A summary of a FeedVersion, leaving out all of the individual validation errors.
     */
    public static class SummarizedFeedVersion {
        public FeedValidationResultSummary validationResult;
        public FeedSource feedSource;
        public String id;
        public Date updated;
        public String previousVersionId;
        public String nextVersionId;
        public int version;
        
        public SummarizedFeedVersion (FeedVersion version) {
            this.validationResult = new FeedValidationResultSummary(version.validationResult);
            this.feedSource = version.getFeedSource();
            this.updated = version.updated;
            this.id = version.id;
            this.nextVersionId = version.nextVersionId;
            this.previousVersionId = version.previousVersionId;
            this.version = version.version;
        }
    }
}
