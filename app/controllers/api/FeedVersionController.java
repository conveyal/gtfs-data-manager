package controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import controllers.Auth0SecuredController;
import controllers.Secured;
import jobs.ProcessSingleFeedJob;
import models.Deployment.SummarizedFeedVersion;
import models.FeedSource;
import models.FeedSource.FeedRetrievalMethod;
import models.FeedVersion;
import models.JsonViews;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import utils.Auth0UserProfile;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

public class FeedVersionController extends Auth0SecuredController {
    public static JsonManager<FeedVersion> json =
            new JsonManager<FeedVersion>(FeedVersion.class, JsonViews.UserInterface.class);
    private static JsonManager<SummarizedFeedVersion> jsonSummarized =
            new JsonManager<SummarizedFeedVersion>(SummarizedFeedVersion.class, JsonViews.UserInterface.class);
    
    /**
     * Grab this feed version.
     * If you pass in ?summarized=true, don't include the full tree of validation results, only the counts.
     */
    public static Result get (String id) throws JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        FeedVersion v = FeedVersion.get(id);
        FeedSource s = v.getFeedSource();
        
        // ways to have permission to do this:
        // 1) be an admin
        // 2) have access to this feed through project permissions
        if (userProfile.canAdministerProject(s.feedCollectionId) || userProfile.canViewFeed(s.feedCollectionId, s.id)) {
            if ("true".equals(request().getQueryString("summarized"))) {
                return ok(jsonSummarized.write(new SummarizedFeedVersion(v))).as("application/json");
            }
            else {
                return ok(json.write(v)).as("application/json");
            }
        }
        else {
            return unauthorized();
        }
    }

    /**
     * Grab this feed version's GTFS.
     */
    public static Result getGtfs (String id) throws JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        FeedVersion v = FeedVersion.get(id);
        FeedSource s = v.getFeedSource();

        if (userProfile.canAdministerProject(s.feedCollectionId) || userProfile.canViewFeed(s.feedCollectionId, s.id)) {
            return ok(v.getFeed());
        }
        else {
            return unauthorized();
        }
    }


    public static Result getAll () throws JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();
        
        // parse the query parameters
        String sId = request().getQueryString("feedsource");
        if (sId == null) {
            return badRequest("Please specify a feedsource");
        }
        
        FeedSource s = FeedSource.get(sId);

        if (userProfile.canAdministerProject(s.feedCollectionId) || userProfile.canViewFeed(s.feedCollectionId, s.id)) {
            return ok(json.write(s.getFeedVersions())).as("application/json");
        }
        else {
            return unauthorized();
        }
    }
    
 
    /**
     * Upload a feed version directly. This is done behind Backbone's back, and as such uses
     * x-multipart-formdata rather than a json blob. This is done because uploading files in a JSON
     * blob is not pretty, and we don't really need to get the Backbone object directly; page re-render isn't
     * a problem.
     * @return
     * @throws JsonProcessingException 
     */
    public static Result create () throws JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();
        
        MultipartFormData body = request().body().asMultipartFormData();
        Map<String, String[]> params = body.asFormUrlEncoded();
        
        FeedSource s = FeedSource.get(params.get("feedSourceId")[0]);

        if (userProfile.canAdministerProject(s.feedCollectionId) || userProfile.canManageFeed(s.feedCollectionId, s.id))
            return unauthorized();
        
        if (FeedRetrievalMethod.FETCHED_AUTOMATICALLY.equals(s.retrievalMethod))
            return badRequest("Feed is autofetched! Cannot upload.");
        
        FeedVersion v = new FeedVersion(s);
        v.setUser(s.getUser());
        
        File toSave = v.newFeed();
        FilePart uploadPart = body.getFile("feed");
        File upload = uploadPart.getFile();
        
        Logger.info("Saving feed {} from upload", s);
        
        FileOutputStream outStream;
        
        try {
            outStream = new FileOutputStream(toSave);
        } catch (FileNotFoundException e) {
            Logger.error("Unable to open {}", toSave);
            return internalServerError("Unable to save feed");
        }
        
        FileInputStream uploadStream;
        try {
            uploadStream = new FileInputStream(upload);
        } catch (FileNotFoundException e) {
            Logger.error("Unable to open input stream from upload {}", upload);
            
            try {
                outStream.close();
            } catch (IOException e1) {}
            
            return internalServerError("Unable to read uploaded feed");
        }
        
        // copy the file
        ReadableByteChannel rbc = Channels.newChannel(uploadStream);
        try {
            outStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            outStream.close();
        } catch (IOException e) {
            Logger.error("Unable to transfer from upload to saved file.");
            return internalServerError("Unable to save uploaded file");
        }
        
        v.hash();
        
        FeedVersion latest = s.getLatest();
        if (latest != null && latest.hash.equals(v.hash)) {
            v.getFeed().delete();
            return redirect("/#feed/" + s.id);
        }

        // note: we don't save it until it's been validated, which happens in the job
        /*Akka.system().scheduler().scheduleOnce(
                Duration.create(50, TimeUnit.MILLISECONDS),
                new ProcessSingleFeedJob(v),
                Akka.system().dispatcher()
                );
        */
        
        // for now run sychronously so the user sees something after the redirect
        // it's pretty fast
        new ProcessSingleFeedJob(v).run();
        
        return redirect("/#feed/" + s.id);
    }

    public static JsonManager<FeedVersion> getJsonManager() {
        return json;
    }
}
