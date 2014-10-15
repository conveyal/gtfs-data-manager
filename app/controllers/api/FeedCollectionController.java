package controllers.api;

import java.io.IOException;
import java.util.Map;

import models.FeedCollection;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import controllers.Secured;

@Security.Authenticated(Secured.class)
public class FeedCollectionController extends Controller {
    private static JsonManager<FeedCollection> json = new JsonManager<FeedCollection>();
    
    public static Result getAll () {
        // TODO: Only show FeedCollections this user has permission to access
        User u = User.getUserByUsername(session("username"));
        if (u.admin)
            return ok(json.write(FeedCollection.getAll()));
        else
            return unauthorized();
    }
    
    public static Result get (String id) throws JsonProcessingException {
        FeedCollection c = FeedCollection.get(id);
        
        return ok(json.write(c));
    }
    
    public static Result update (String id) throws JsonProcessingException {
        FeedCollection c = FeedCollection.get(id);
        
        User currentUser = User.getUserByUsername(session("username"));
        
        if (!Boolean.TRUE.equals(currentUser.admin))
            return unauthorized();
        
        JsonNode params = request().body().asJson();
                
        // Only allow admins or feed collection owners to rename FeedCollections
        // TODO: feed collection admins
        
        if (Boolean.TRUE.equals(currentUser.admin) || currentUser.equals(c.getUser())) {
            JsonNode name = params.get("name");
            if (name != null) {
                c.name = name.asText();
            }
        }
        
        // only allow admins to change feed collection owners
        if (Boolean.TRUE.equals(currentUser.admin)) {
            JsonNode uname = params.get("user");
        
            // TODO: test
            User u = null;
            if (uname != null && uname.has("username"))
                u = User.getUserByUsername(uname.get("username").asText());
        
            if (u != null)
                c.setUser(u);
        }
        
        c.save();
        
        return ok(json.write(c));
    }
    
    public static Result create () throws JsonParseException, JsonMappingException, IOException {
        User currentUser = User.getUserByUsername(session("username"));
        
        if (!Boolean.TRUE.equals(currentUser.admin))
            return unauthorized();
        
        JsonNode params = request().body().asJson();
        
        FeedCollection c = new FeedCollection();
        
        // TODO: fail gracefully
        c.name = params.get("name").asText();
        JsonNode uname = params.get("user/username");
        
        User u = null;
        if (uname != null)
            u = User.getUserByUsername(uname.asText());
        
        if (u == null)
            u = currentUser;
            
        c.setUser(u);
        
        c.save();
        
        return ok(json.write(c));
    }
}
