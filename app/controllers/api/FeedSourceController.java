package controllers.api;

import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import controllers.Secured;
import models.FeedCollection;
import models.FeedSource;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;


@Security.Authenticated(Secured.class)
public class FeedSourceController extends Controller {
    private static JsonManager<FeedSource> json = new JsonManager<FeedSource>();
    
    public static Result get (String id) throws JsonProcessingException {
        // TODO: access control
        return ok(json.write(FeedSource.get(id)));
    }
    
    public static Result getAll () {
        User currentUser = User.getUserByUsername(session("username"));
        
        // parse the query parameters
        String fcId = request().getQueryString("feedcollection");
        FeedCollection fc = null;
        if (fcId != null)
            fc = FeedCollection.get(fcId);
        
        // Only admins can get all feeds
        if (fc == null && Boolean.TRUE.equals(currentUser.admin)) {
            return ok(json.write(FeedSource.getAll()));
        }
        else {
            // TODO: access control
            return ok(json.write(fc.getFeedSources()));
        }
    }
    
    // common code between create and update
    private static void applyJsonToFeedSource (FeedSource s, JsonNode params) throws MalformedURLException {
        s.name = params.get("name").asText();
        s.autofetch = params.get("autofetch").asBoolean();
        s.fetchFrequency = params.get("fetchFrequency").asInt();
        s.isPublic = params.get("isPublic").asBoolean();
        // the last fetched/updated cannot be updated from the web interface, only internally
        String url = params.get("url").asText();
        if (url != null && !"null".equals(url))
            s.url = new URL(url);
    }
    
    public static Result update (String id) throws JsonProcessingException, MalformedURLException {
        FeedSource s = FeedSource.get(id);
        User currentUser = User.getUserByUsername(session("username"));

        // admins can update anything; non-admins can only update the feeds they own
        if (Boolean.TRUE.equals(currentUser.admin) || currentUser.equals(s.getUser())) {
            JsonNode params = request().body().asJson();
            applyJsonToFeedSource(s, params);
            s.save();
            return ok(json.write(s));
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
            FeedSource s = new FeedSource();
            s.setUser(currentUser);
            s.setFeedCollection(c);
            
            applyJsonToFeedSource(s, params);
            
            s.save();
            
            return ok(json.write(s));
        }
        else {
            return unauthorized();
        }
    }
}
