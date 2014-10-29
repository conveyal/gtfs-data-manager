package controllers.api;

import java.net.MalformedURLException;
import java.net.URL;

import jobs.FetchSingleFeedJob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.Secured;
import models.FeedCollection;
import models.FeedSource;
import models.JsonViews;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;


@Security.Authenticated(Secured.class)
public class FeedSourceController extends Controller {
    private static JsonManager<FeedSource> json =
            new JsonManager<FeedSource>(FeedSource.class, JsonViews.UserInterface.class);
    
    public static Result get (String id) throws JsonProcessingException {
        // TODO: access control
        return ok(json.write(FeedSource.get(id)));
    }
    
    public static Result getAll () throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        
        if (!Boolean.TRUE.equals(currentUser.admin)) {
            return unauthorized();
        }
        
        // parse the query parameters
        String fcId = request().getQueryString("feedcollection");
        FeedCollection fc = null;
        if (fcId != null)
            fc = FeedCollection.get(fcId);
 
        if (fc == null) {
            return ok(json.write(FeedSource.getAll())).as("application/json");
        }
        else {
            return ok(json.write(fc.getFeedSources())).as("application/json");
        }
    }
    
    // common code between create and update
    private static void applyJsonToFeedSource (FeedSource s, JsonNode params) throws MalformedURLException {
        s.name = params.get("name").asText();
        s.retrievalMethod = FeedSource.FeedRetrievalMethod.valueOf(params.get("retrievalMethod").asText());
        s.isPublic = params.get("isPublic").asBoolean();
        // the last fetched/updated cannot be updated from the web interface, only internally
        String url = params.get("url").asText();
        if (url != null && !"null".equals(url))
            s.url = new URL(url);
    }
    
    public static Result update (String id) throws JsonProcessingException, MalformedURLException {
        FeedSource s = FeedSource.get(id);
        User currentUser = User.getUserByUsername(session("username"));

        // admins can update anything; non-admins cannot update anything (imagine the havoc if an agency changed their retrieval method,
        // or set a non-public feed to public)
        if (Boolean.TRUE.equals(currentUser.admin)) {
            JsonNode params = request().body().asJson();
            applyJsonToFeedSource(s, params);
            s.save();
            return ok(json.write(s)).as("application/json");
        }
        
        return unauthorized();
    }
    
    public static Result create () throws MalformedURLException, JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        
        // parse the result
        JsonNode params = request().body().asJson();
        
        FeedCollection c = FeedCollection.get(params.get("feedCollection").get("id").asText());
        
        // TODO: access control
        if (Boolean.TRUE.equals(currentUser.admin) || currentUser.equals(c.getUser())) {
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
    
    /**
     * Get the userId and key that will allow a user to edit just this feed, without being an admin.
     * Only admins can retrieve this information.
     */
    public static Result getUserIdAndKey (String id) {
        User currentUser = User.getUserByUsername(session("username"));
        
        if (Boolean.TRUE.equals(currentUser.admin)) {
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
        
        if (Boolean.TRUE.equals(currentUser.admin)) {
            FeedSource s = FeedSource.get(id);
            FetchSingleFeedJob job = new FetchSingleFeedJob(s);
            job.run();
            return ok(FeedVersionController.getJsonManager().write(job.result)).as("application/json");
        }
        else {
            return unauthorized();
        }
    }
}
