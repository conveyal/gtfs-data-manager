package models;

import java.util.ArrayList;
import java.util.Collection;

import utils.DataStore;

/**
 * A deployment of (a given version of) OTP on a given set of feeds.
 * @author mattwigway
 *
 */
public class Deployment extends Model {
    private static DataStore<Deployment> deploymentStore = new DataStore<Deployment>("deployments");
    
    private Collection<String> feedVersions;
    
    // future use
    private String osmFileId;
    
    /** The commit of OTP being used on this deployment */
    public String otpCommit;
    
    /** All of the feed versions used in this deployment */
    public Collection<FeedVersion> getFeedVersions () {
        ArrayList<FeedVersion> ret = new ArrayList<FeedVersion>(feedVersions.size());
        
        for (String id : feedVersions) {
            ret.add(FeedVersion.get(id));
        }
        
        return ret;
    }
    
    public void setFeedVersions (Collection<FeedVersion> versions) {
        feedVersions = new ArrayList<String>(versions.size());
        
        for (FeedVersion version : versions) {
            feedVersions.add(version.id);
        }
    }
    
    /** Get a deployment by ID */
    public static Deployment get (String id) {
        return deploymentStore.getById(id);
    }
}
