package models;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import play.Play;
import utils.DataStore;
import utils.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.io.ByteStreams;

import controllers.api.JsonManager;

/**
 * A deployment of (a given version of) OTP on a given set of feeds.
 * @author mattwigway
 *
 */
@JsonInclude(Include.ALWAYS)
public class Deployment extends Model {
    private static DataStore<Deployment> deploymentStore = new DataStore<Deployment>("deployments");
    
    public String name;
    
    /** What server is this currently deployed to? */
    public String deployedTo;
    
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
    public List<FeedVersion> getFullFeedVersions () {
        ArrayList<FeedVersion> ret = new ArrayList<FeedVersion>(feedVersionIds.size());
        
        for (String id : feedVersionIds) {
            ret.add(FeedVersion.get(id));
        }
        
        return ret;
    }
    
    /** All of the feed versions used in this deployment, summarized so that the Internet won't break */
    @JsonView(JsonViews.UserInterface.class)
    public List<SummarizedFeedVersion> getFeedVersions () {
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
     * The routerId of this deployment
     */
    public String routerId;
    
    /**
     * If this deployment is for a single feed source, the feed source this deployment is for.
     */
    public String feedSourceId;
    
    /**
     * Feed sources that had no valid feed versions when this deployment was created, and ergo were not added. 
     */
    @JsonInclude(Include.ALWAYS)
    @JsonView(JsonViews.DataDump.class)
    public Collection<String> invalidFeedSourceIds;
    
    /**
     * Get all of the feed sources which could not be added to this deployment.
     */
    @JsonView(JsonViews.UserInterface.class)
    @JsonInclude(Include.ALWAYS)
    public List<FeedSource> getInvalidFeedSources () {
        if (invalidFeedSourceIds == null)
            return null;
        
        ArrayList<FeedSource> ret = new ArrayList<FeedSource>(invalidFeedSourceIds.size());
        
        for (String id : invalidFeedSourceIds) {
            ret.add(FeedSource.get(id));
        }
        
        return ret;
    }
    
    /** Create a single-agency (testing) deployment for the given feed source */
    public Deployment (FeedSource feedSource) {
        super();
        
        this.feedSourceId = feedSource.id;
        this.setFeedCollection(feedSource.getFeedCollection());
        this.dateCreated = new Date();
        this.feedVersionIds = new ArrayList<String>();
        
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        
        this.name = StringUtils.getCleanName(feedSource.name) + "_" + df.format(dateCreated);
        
        // always use the latest, no matter how broken it is, so we can at least see how broken it is
        this.feedVersionIds.add(feedSource.getLatestVersionId());
        
        this.routerId = StringUtils.getCleanName(feedSource.name) + "_" + feedSourceId;
        
        this.deployedTo = null;
    }
    
    /** Create a new deployment plan for the given feed collection */
    public Deployment (FeedCollection feedCollection) {
        super();
        
        this.feedSourceId = null;
        
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
        
        this.deployedTo = null;
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
    
    /** Dump this deployment to the given file 
     * @throws IOException */
    public void dump (File output) throws IOException {
        // create the zipfile
        ZipOutputStream out;
        try {
            out = new ZipOutputStream(new FileOutputStream(output));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        // save the manifest at the beginning of the file, for read/seek efficiency
        
        ZipEntry manifestEntry = new ZipEntry("manifest.json");
        out.putNextEntry(manifestEntry);
        
        // create the json
        JsonManager<Deployment> jsonManifest = new JsonManager<Deployment>(Deployment.class, JsonViews.UserInterface.class);
        // this mixin gives us full feed validation results, not summarized
        jsonManifest.addMixin(Deployment.class, DeploymentFullFeedVersionMixin.class);
        
        byte[] manifest = jsonManifest.write(this).getBytes();
        
        out.write(manifest);
        
        out.closeEntry();
        
        // write each of the GTFS feeds
        for (FeedVersion v : this.getFullFeedVersions()) {
            File feed = v.getFeed();
            
            FileInputStream in;
            
            try {
                in = new FileInputStream(feed);
            } catch (FileNotFoundException e1) {
                throw new RuntimeException(e1);
            }
            
            ZipEntry e = new ZipEntry(feed.getName());
            out.putNextEntry(e);
            
            // copy the zipfile 100k at a time
            int bufSize = 100 * 1024;
            byte[] buff = new byte[bufSize];
            int readBytes;
            
            while (true) {
                try {
                    readBytes = in.read(buff);
                } catch (IOException e1) {
                    try {
                        in.close();
                    } catch (IOException e2) {
                        throw new RuntimeException(e2);
                    }
                    throw new RuntimeException(e1);
                }
                
                if (readBytes == -1)
                    // we've copied the whole file
                    break;
                
                out.write(buff, 0, readBytes);
            }

            try {
                in.close();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            
            out.closeEntry();
        }
        
        // extract OSM and insert it into the deployment bundle
        ZipEntry e = new ZipEntry("osm.pbf");
        out.putNextEntry(e);
        
        // figure out the bounds
        Rectangle2D bounds = getBounds();
        
        // call vex server
        URL vexUrl = new URL(
                String.format("%s/?n=%.6f&e=%.6f&s=%.6f&w=%.6f",
                        Play.application().configuration().getString("application.deployment.osm_vex"),
                        bounds.getMaxY(),
                        bounds.getMaxX(),
                        bounds.getMinY(),
                        bounds.getMinX()
                        ));
        
        HttpURLConnection conn = (HttpURLConnection) vexUrl.openConnection();
        conn.connect();
        
        ByteStreams.copy(conn.getInputStream(), out);
                
        out.closeEntry();
        
        out.close();
    }
    
    // Get the union of the bounds of all the feeds in this deployment
    private Rectangle2D getBounds() {
        List<SummarizedFeedVersion> versions = getFeedVersions();
        
        if (versions.size() == 0)
            return null;
        
        Rectangle2D bounds = (Rectangle2D) versions.get(0).validationResult.bounds.clone();
        
        // i = 1 because we've already included bounds 0
        // todo: NPE
        for (int i = 1; i < versions.size(); i++) {
            bounds.add(versions.get(i).validationResult.bounds);
        }
        
        // expand the bounds by (about) 10 km in every direction
        double degreesPerKmLat = 360D / 40008;
        double degreesPerKmLon = 
                // the diameter of the chord of the earth at this latitude
                360 /
                (2 * Math.PI * 6371 * Math.cos(Math.toRadians(bounds.getCenterY())));
        
        // south-west
        bounds.add(new Point2D.Double(
                // lon
                bounds.getMinX() - 10 * degreesPerKmLon,
                bounds.getMinY() - 10 * degreesPerKmLat
                ));
        
        // north-east
        bounds.add(new Point2D.Double(
                // lon
                bounds.getMaxX() + 10 * degreesPerKmLon,
                bounds.getMaxY() + 10 * degreesPerKmLat
                ));
        
        return bounds;        
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
     * Get the deployment currently deployed to a particular server.
     */
    public static Deployment getDeploymentForServerAndRouterId (String server, String routerId) {
        for (Deployment d : getAll()) {
            if (d.deployedTo != null && d.deployedTo.equals(server)) {
                if ((routerId != null && routerId.equals(d.routerId)) || d.routerId == routerId) {
                    return d;
                }
            }
        }
        
        return null;
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
    
    /**
     * A MixIn to be applied to this deployment, for generating manifests, so that full feed versions appear rather than
     * summarized feed versions.
     * 
     * Usually a mixin would be used on an external class, but since we are changing one thing about a single class, it seemed
     * unnecessary to define a new view just for generating deployment manifests.
     */
    public abstract static class DeploymentFullFeedVersionMixin {
        @JsonIgnore
        public abstract Collection<SummarizedFeedVersion> getFeedVersions();
        
        @JsonProperty("feedVersions")
        @JsonIgnore(false)
        public abstract Collection<FeedVersion> getFullFeedVersions ();
    }
}
