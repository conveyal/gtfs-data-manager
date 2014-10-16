package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.conveyal.gtfs.model.ValidationResult;
import com.conveyal.gtfs.validator.json.FeedProcessor;
import com.conveyal.gtfs.validator.json.FeedValidationResult;
import com.conveyal.gtfs.validator.json.LoadStatus;
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
    public FeedCollection getFeedCollection () {
        return FeedCollection.get(feedCollectionId);
    }
    
    public void setFeedCollection(FeedCollection c) {
        this.feedCollectionId = c.id;
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
        if (!this.autofetch) {
            Logger.info("not fetching feed {}, not a fetchable feed", this.toString());
            return;
        }
        
        // fetchable feed, continue
        FeedVersion latest = getLatest();
        
        // We create a new FeedVersion now, so that the fetched date is (milliseconds) before
        // fetch occurs. That way, in the highly unlikely event that a feed is updated while we're
        // fetching it, we will not miss a new feed.
        FeedVersion newFeed = new FeedVersion(this);
        
        // make the request, using the proper HTTP caching headers to prevent refetch, if applicable
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Logger.error("Unable to open connection to {}; not fetching feed {}", url, this);
            return;
        }
        
        conn.setDefaultUseCaches(true);
        
        if (latest != null)
            conn.setIfModifiedSince(latest.updated.getTime());
        
        try {
            conn.connect();
        
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                Logger.info("Feed {} has not been modified", this);
                return;
            }

            // TODO: redirects
            else if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Logger.info("Saving feed {}", this);

                File out = newFeed.newFeed();
                
                FileOutputStream outStream;
                
                try {
                    outStream = new FileOutputStream(out);
                } catch (FileNotFoundException e) {
                    Logger.error("Unable to open {}", out);
                    return;
                }
                
                // copy the file
                ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
                outStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
            
            else {
                Logger.error("HTTP status {} retrieving feed {}", conn.getResponseMessage(), this);
                return;
            }
        } catch (IOException e) {
            Logger.error("Unable to connect to {}; not fetching feed {}", url, this);
            return;
        }
        
        // validate the fetched file
        // note that anything other than a new feed fetched successfully will have already returned from the function
        newFeed.hash();
        
        if (latest != null && newFeed.hash.equals(latest.hash)) {
            Logger.warn("Feed {} was fetched but has not changed; server operators should add If-Modified-Since support to avoid wasting bandwidth", this);
            newFeed.getFeed().delete();
        }
        else {
            newFeed.setFeedSource(this);
            newFeed.userId = this.userId;
            newFeed.validate();
            newFeed.save();
        }
    }
    
    public String toString () {
        return "<FeedSource " + this.name + " (" + this.id + ")>";
    }
    
    public void save () {
        sourceStore.save(this.id, this);
    }
    
    /**
     * Get the latest version of this feed
     * @return the latest version of this feed
     */
    @JsonIgnore
    public FeedVersion getLatest () {
        FeedVersion latest = null;
    
        for (FeedVersion version : FeedVersion.getAll()) {
            if (version.feedSourceId.equals(this.id)) {
                if (latest == null || version.updated.after(latest.updated)) {
                    latest = version;
                }
            }
        }
        
        return latest;
    }
    
    /**
     * We can't pass the entire latest feed source back, because it contains references back to this feedsource,
     * so Jackson doesn't work. So instead we specifically expose the validation results and the latest update.
     * @param id
     * @return
     */
    public Date getLastUpdated() {
        FeedVersion latest = getLatest();
        return latest != null ? latest.updated : null;
    }
    
    public FeedValidationResultSummary getLatestValidation () {
        FeedVersion latest = getLatest();
        return latest != null ? new FeedValidationResultSummary(latest.validationResult) : null;
    }
    
    public static FeedSource get(String id) {
        return sourceStore.getById(id);
    }

    public static Collection<FeedSource> getAll() {
        return sourceStore.getAll();
    }
    
    /**
     * Represents a subset of a feed validation result, just enough for display, without overwhelming the browser
     * or sending unnecessary amounts of data over the wire
     */
    public static class FeedValidationResultSummary implements Serializable {
        public LoadStatus loadStatus;
        public String loadFailureReason;
        public Collection<String> agencies;
        
        public int routeErrors;
        public int stopErrors;
        public int tripErrors;
        public int shapeErrors;

        // statistics
        public int agencyCount;
        public int routeCount;
        public int tripCount;
        public int stopTimesCount;
        
        /** The first date the feed has service, either in calendar.txt or calendar_dates.txt */
        public Date startDate;
        
        /** The last date the feed has service, either in calendar.txt or calendar_dates.txt */
        public Date endDate;
        
        /**
         * Construct a summarized version of the given FeedValidationResult.
         * @param result
         */
        public FeedValidationResultSummary (FeedValidationResult result) {
            this.loadStatus = result.loadStatus;
            this.loadFailureReason = result.loadFailureReason;
            this.agencies = result.agencies;
            this.routeErrors = result.routes.invalidValues.size();
            this.stopErrors = result.stops.invalidValues.size();
            this.tripErrors = result.trips.invalidValues.size();
            this.shapeErrors = result.shapes.invalidValues.size();
            this.agencyCount = result.agencyCount;
            this.routeCount = result.routeCount;
            this.stopTimesCount = result.stopTimesCount;
            this.startDate = result.startDate;
            this.endDate = result.endDate;
        }
    }
     }
