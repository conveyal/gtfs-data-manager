package controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Auth0SecuredControlled;
import controllers.Secured;
import jobs.FetchSingleFeedJob;
import models.*;
import models.User.ProjectPermissions;
import play.Play;
import play.api.libs.Files;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.Auth0UserProfile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


//@Security.Authenticated(Secured.class)
public class FeedSourceController extends Auth0SecuredControlled {
    private static JsonManager<FeedSource> json =
            new JsonManager<FeedSource>(FeedSource.class, JsonViews.UserInterface.class);
    
    public static Result get (String id) throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));

        FeedSource fs = FeedSource.get(id);

        if (currentUser.admin || currentUser.id.equals(fs.userId) || currentUser.hasReadAccess(fs.id))
            return ok(json.write(fs));

        else
            return unauthorized();
    }

    public static Result getAll () throws JsonProcessingException {
        //User currentUser = User.getUserByUsername(session("username"));
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        // parse the query parameters
        String fcId = request().getQueryString("feedcollection");
        FeedCollection fc = null;
        if (fcId != null)
            fc = FeedCollection.get(fcId);
 
        Collection<FeedSource> feedSources;
        if (fc == null) {
            feedSources = FeedSource.getAll();
        }
        else {
            feedSources = fc.getFeedSources();
        }

        if(!userProfile.canAdministerProject(fcId)) {
            Set<String> canRead = new HashSet<String>();

            // filter the list, only show the ones this user has permission to access
            List<FeedSource> filtered = new ArrayList<FeedSource>();

            for (FeedSource fs : feedSources) {
                if (canRead.contains(fs.id)) {
                    filtered.add(fs);
                }
            }

            feedSources = filtered;
        }

        return ok(json.write(feedSources)).as("application/json");
    }
    
    // common code between create and update
    private static void applyJsonToFeedSource (FeedSource s, JsonNode params) throws MalformedURLException {
        s.name = params.get("name").asText();
        s.retrievalMethod = FeedSource.FeedRetrievalMethod.valueOf(params.get("retrievalMethod").asText());
        
        if (params.has("editorId"))
            s.editorId = params.get("editorId").asText();

        if (params.has("snapshotVersion"))
            s.snapshotVersion = params.get("snapshotVersion").asText();
        
        s.isPublic = params.get("isPublic").asBoolean();
        s.deployable = params.get("deployable").asBoolean();
        // the last fetched/updated cannot be updated from the web interface, only internally
        String url = params.get("url").asText();
        if (url != null && !"null".equals(url)) {
            if ("".equals(url)) {
                s.url = null;
            }
            else {
                s.url = new URL(url);
                // reset the last fetched date so it can be fetched again
                s.lastFetched = null;
            }
        }
    }
    
    public static Result update (String id) throws JsonProcessingException, MalformedURLException {
        FeedSource s = FeedSource.get(id);
        User currentUser = User.getUserByUsername(session("username"));

        // admins can update anything; non-admins cannot update anything except snapshot
        JsonNode params = request().body().asJson();
        if (currentUser.admin) {
            applyJsonToFeedSource(s, params);
            s.save();
            return ok(json.write(s)).as("application/json");
        }
        else if (currentUser.hasWriteAccess(s.id) || currentUser.id.equals(s.userId)) {
            if (params.has("snapshotVersion")) {
                s.snapshotVersion = params.get("snapshotVersion").asText();
                s.save();
            }
            return ok(json.write(s)).as("application/json");
        }
        return unauthorized();
    }
    
    public static Result create () throws MalformedURLException, JsonProcessingException {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();
        
        // parse the result
        JsonNode params = request().body().asJson();
        
        FeedCollection c = FeedCollection.get(params.get("feedCollection").get("id").asText());
        
        if (userProfile.canAdministerProject(c.id)) {
            FeedSource s = new FeedSource(params.get("name").asText());
            // not setting user because feed sources are automatically assigned a unique user
            s.setFeedCollection(c);
            
            applyJsonToFeedSource(s, params);
            
            s.save();
            
            return ok(json.write(s)).as("application/json");
        }
        else {
            return unauthorized();
        }
    }
    
    public static Result delete (String id) {
        String token = getToken();
        if(token == null) return unauthorized("Could not find authorization token");
        Auth0UserProfile userProfile = verifyUser();
        if(userProfile == null) return unauthorized();

        FeedSource s = FeedSource.get(id);
        if (userProfile.canAdministerProject(s.getFeedCollection().id)) {
            s.delete();
            return ok();
        }
        else {
            return unauthorized();
        }
    }
    
    /**
     * Get the userId and key that will allow a user to edit just this feed, without being an admin.
     * Only admins can retrieve this information.
     */
    public static Result getUserIdAndKey (String id) {
        User currentUser = User.getUserByUsername(session("username"));
        
        if (currentUser.admin) {
            FeedSource s = FeedSource.get(id);
            User u = s.getUser();
            
            ObjectNode result = Json.newObject();
            result.put("userId", u.id);
            result.put("key", u.key);
            return ok(result);
        }
        else {
            return unauthorized();
        }
    }
    
    /**
     * Refetch this feed
     * @throws JsonProcessingException 
     */
    public static Result fetch (String id) throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        FeedSource s = FeedSource.get(id);
        
        // three ways to have permission to do this:
        // 1) be an admin
        // 2) be the autogenerated user associated with this feed
        // 3) have access to this feed through project permissions
        // if all fail, the user cannot do this.
        if (!currentUser.admin && !currentUser.equals(s.getUser()) && !currentUser.hasWriteAccess(s.id))
            return unauthorized();
        
        FetchSingleFeedJob job = new FetchSingleFeedJob(s);
        job.run();
        return ok(FeedVersionController.getJsonManager().write(job.result)).as("application/json");
    }

    public static Result uploadAgencyLogo(String id) {
        FeedSource feedSource = FeedSource.get(id);
        if(feedSource == null) return badRequest();

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart picture = body.getFile("picture");
        if (picture != null) {
            //String contentType = picture.getContentType();
            File formFile = picture.getFile();

            String agencyId = body.asFormUrlEncoded().get("agencyId")[0];

            // create the file in the branding assets directory
            String agencyDir = Play.application().configuration().getString("application.data.branding_internal") + File.separator + agencyId;
            new File(agencyDir).mkdirs();
            File brandingFile = new File(agencyDir + File.separator + "logo.png");
            Files.copyFile(formFile, brandingFile, true);

            // register the branding with the FeedSource
            AgencyBranding agencyBranding = feedSource.getAgencyBranding(agencyId);
            if(agencyBranding == null) { // if branding does not already exist for this agency, create it
                agencyBranding = new AgencyBranding(agencyId);
                feedSource.addAgencyBranding(agencyBranding);
            }
            agencyBranding.hasLogo = true;

            feedSource.save();
            return redirect("/#feed/" + feedSource.id);
        } else {
            flash("error", "Missing file");
            return redirect("/#feed/" + feedSource.id);
        }
    }
}
