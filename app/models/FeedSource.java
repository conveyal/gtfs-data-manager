package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.mapdb.Fun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;

import play.Logger;
import play.Play;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import utils.DataStore;


/**
 * This represents where a feed comes from/came from.
 * @author mattwigway
 *
 */
@JsonInclude(Include.ALWAYS)
public class FeedSource extends Model implements Comparable<FeedSource> {
    private static final long serialVersionUID = -5696893509128904129l;

    private static DataStore<FeedSource> sourceStore = new DataStore<FeedSource>("feedsources");
    
    /**
     * The collection of which this feed is a part
     */
    @JsonView(JsonViews.DataDump.class)
    public String feedCollectionId;

    /**
     * Get the FeedCollection of which this feed is a part
     */
    @JsonView(JsonViews.UserInterface.class)
    public FeedCollection getFeedCollection () {
        return FeedCollection.get(feedCollectionId);
    }
    
    public void setFeedCollection(FeedCollection c) {
        this.feedCollectionId = c.id;
    }
    
    /** The name of this feed source, e.g. MTA New York City Subway */
    public String name;
    
    /** Is this feed public, i.e. should it be listed on the
     * public feeds page for download?
     */
    public boolean isPublic;
    
    /** Is this feed deployable? */
    public boolean deployable;
    
    /**
     * How do we receive this feed?
     */
    public FeedRetrievalMethod retrievalMethod;
    
    /**
     * When was this feed last fetched?
     */
    public Date lastFetched;
    
    /**
     * When was this feed last updated?
     */
    //public transient Date lastUpdated;
    
    /**
     * From whence is this feed fetched?
     */
    public URL url;
    
    /**
     * What is the GTFS Editor ID of this feed?
     */
    public String editorId;

    /**
     * What is the GTFS Editor snapshot for this feed?
     *
     * This is the String-formatted snapshot ID, which is the base64-encoded ID and the version number.
     */
    public String snapshotVersion;

    public Collection<AgencyBranding> branding;

    /**
     * Create a new feed. This also creates a user to own this feed.
     */
    public FeedSource (String name) {
        super();
        
        this.name = name;
        
        // create a user for this feed
        String username = this.name;
        int i = 0;
        
        // feed source names are not always unique. find a name that is.
        while (true) {
            if (User.getUserByUsername(username) != null) {
                i++;
                username = this.name + "_" + i;
            }
            else {
                break;
            }
        }
        
        // create a new user to own this feed source, with no password (a login key will be generated) and no email address
        User u = new User(username, null, null);
        u.active = true;
        u.admin = false;
        u.autogenerated = true;
        u.save();
        this.userId = u.id;
    }
    
    /**
     * No-arg constructor to yield an uninitialized feed source, for dump/restore.
     * Should not be used in general code.
     */
    public FeedSource () {
        // do nothing
    }
    
    @Override
    public void setUser (User u) {
        throw new IllegalArgumentException("FeedSources are permanently associated with a single user");
    }
    
    /**
     * Fetch the latest version of the feed.
     */
    public FeedVersion fetch () {
        if (this.retrievalMethod.equals(FeedRetrievalMethod.MANUALLY_UPLOADED)) {
            Logger.info("not fetching feed {}, not a fetchable feed", this.toString());
            return null;
        }
        
        // fetchable feed, continue
        FeedVersion latest = getLatest();
        
        // We create a new FeedVersion now, so that the fetched date is (milliseconds) before
        // fetch occurs. That way, in the highly unlikely event that a feed is updated while we're
        // fetching it, we will not miss a new feed.
        FeedVersion newFeed = new FeedVersion(this);
        
        // build the URL from which to fetch
        URL url;
        String oauthToken = null;
        if (this.retrievalMethod.equals(FeedRetrievalMethod.FETCHED_AUTOMATICALLY))
            url = this.url;
        else if (this.retrievalMethod.equals(FeedRetrievalMethod.PRODUCED_IN_HOUSE)) {
            if (this.editorId == null || this.snapshotVersion == null) {
                Logger.error("Feed {} has no editor id; cannot fetch", this); 
                return null;
            }
            
            // get an OAuth token, etc.
            String baseUrl = Play.application().configuration().getString("application.editor.internal_url");
            
            if (!baseUrl.endsWith("/"))
                baseUrl += "/";
            
            String tokenUrl = baseUrl + "get_token?client_id=" + Play.application().configuration().getString("application.oauth.client_id");
            tokenUrl += "&client_secret=" + Play.application().configuration().getString("application.oauth.client_secret");
            
            WSResponse resp;
            try {
                resp = WS.url(tokenUrl).get().get(30 * 1000L);
            } catch (Exception e) {
                Logger.error("Could not get OAuth token, skipping feed {}", this);
                return null;
            }
            
            if (resp.getStatus() != 200) {
                Logger.error("Could not get OAuth token (status {}), skipping feed {}", resp.getStatus(), this);
                return null;
            }
            
            oauthToken = resp.getBody();
            
            // build the URL
            try {
                url = new URL(baseUrl + "api/snapshot/" + this.snapshotVersion + ".zip");
            } catch (MalformedURLException e) {
                Logger.error("Invalid URL for editor, check your config.");
                return null;
            }
        }
        else {
            Logger.error("Unknown retrieval method" + this.retrievalMethod);
            return null;
        }
        
        Logger.info(url.toString());
        
        // make the request, using the proper HTTP caching headers to prevent refetch, if applicable
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Logger.error("Unable to open connection to {}; not fetching feed {}", url, this);
            return null;
        }
        
        conn.setDefaultUseCaches(true);
        
        if (oauthToken != null)
            conn.addRequestProperty("Authorization", "Bearer " + oauthToken);
        
        // lastFetched is set to null when the URL changes
        if (latest != null && this.lastFetched != null)
            conn.setIfModifiedSince(Math.min(latest.updated.getTime(), this.lastFetched.getTime()));
        
        try {
            conn.connect();
        
            if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                Logger.info("Feed {} has not been modified", this);
                return null;
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
                    return null;
                }
                
                // copy the file
                ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
                outStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                outStream.close();
            }
            
            else {
                Logger.error("HTTP status {} retrieving feed {}", conn.getResponseMessage(), this);
                return null;
            }
        } catch (IOException e) {
            Logger.error("Unable to connect to {}; not fetching feed {}", url, this);
            return null;
        }
        
        // validate the fetched file
        // note that anything other than a new feed fetched successfully will have already returned from the function
        newFeed.hash();
        
        if (latest != null && newFeed.hash.equals(latest.hash)) {
            Logger.warn("Feed {} was fetched but has not changed; server operators should add If-Modified-Since support to avoid wasting bandwidth", this);
            newFeed.getFeed().delete();
            return null;
        }
        else {
            newFeed.userId = this.userId;
            newFeed.validate();            
            newFeed.save();
            
            this.lastFetched = newFeed.updated;
            this.save();
            
            return newFeed;
        }
    }
    
    public int compareTo(FeedSource o) {
        return this.name.compareTo(o.name);
    }
    
    public String toString () {
        return "<FeedSource " + this.name + " (" + this.id + ")>";
    }
    
    public void save () {
        save(true);
    }
    
    public void save (boolean commit) {
        if (commit)
            sourceStore.save(this.id, this);
        else
            sourceStore.saveWithoutCommit(this.id, this);
    }
    
    /**
     * Get the latest version of this feed
     * @return the latest version of this feed
     */
    @JsonIgnore
    public FeedVersion getLatest () {
        FeedVersion v = FeedVersion.versionStore.findFloor("version", new Fun.Tuple2(this.id, Fun.HI));
        
        // the ID doesn't necessarily match, because it will fall back to the previous source in the store if there are no versions for this source
        if (v == null || !v.feedSourceId.equals(this.id))
            return null;
        
        return v;
    }
    
    @JsonInclude(Include.NON_NULL)
    @JsonView(JsonViews.UserInterface.class)
    public String getLatestVersionId () {
        FeedVersion latest = getLatest();
        return latest != null ? latest.id : null;
    }
    
    /**
     * We can't pass the entire latest feed version back, because it contains references back to this feedsource,
     * so Jackson doesn't work. So instead we specifically expose the validation results and the latest update.
     * @param id
     * @return
     */
    // TODO: use summarized feed source here. requires serious refactoring on client side.
    @JsonInclude(Include.NON_NULL)
    @JsonView(JsonViews.UserInterface.class)
    public Date getLastUpdated() {
        FeedVersion latest = getLatest();
        return latest != null ? latest.updated : null;
    }
    
    @JsonInclude(Include.NON_NULL)
    @JsonView(JsonViews.UserInterface.class)
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
     * Get all of the feed versions for this source
     * @return
     */
    @JsonIgnore
    public Collection<FeedVersion> getFeedVersions() {
        // TODO Indices
        ArrayList<FeedVersion> ret = new ArrayList<FeedVersion>();
        
        for (FeedVersion v : FeedVersion.getAll()) {
            if (this.id.equals(v.feedSourceId)) {
                ret.add(v);
            }
        }
        
        return ret;
    }
    
    /**
     * Represents ways feeds can be retrieved
     */
    public static enum FeedRetrievalMethod {
        FETCHED_AUTOMATICALLY, // automatically retrieved over HTTP on some regular basis
        MANUALLY_UPLOADED, // manually uploaded by someone, perhaps the agency, or perhaps an internal user
        PRODUCED_IN_HOUSE // produced in-house in a GTFS Editor instance
    }
    
    public static void commit() {
        sourceStore.commit();
    }

    /**
     * Delete this feed source and everything that it contains.
     */
    public void delete() {
        for (FeedVersion v : getFeedVersions()) {
            v.delete();
        }
        
        sourceStore.delete(this.id);
    }

    @JsonIgnore
    public AgencyBranding getAgencyBranding(String agencyId) {
        if(branding != null) {
            for (AgencyBranding agencyBranding : branding) {
                if (agencyBranding.agencyId.equals(agencyId)) return agencyBranding;
            }
        }
        return null;
    }

    @JsonIgnore
    public void addAgencyBranding(AgencyBranding agencyBranding) {
        if(branding == null) {
            branding = new ArrayList<>();
        }
        branding.add(agencyBranding);
    }

}
