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

import controllers.Secured;

@Security.Authenticated(Secured.class)
public class FeedCollectionController extends Controller {
    private static JsonManager<FeedCollection> json = new JsonManager<FeedCollection>();
    
    public static Result get (String id) throws JsonProcessingException {
        FeedCollection c = FeedCollection.get(id);
        
        return ok(json.write(c));
    }
    
    public static Result update (String id) throws JsonProcessingException {
        Map<String,String[]> params = request().body().asFormUrlEncoded();

        FeedCollection c = FeedCollection.get(id);
        
        c.setName(params.get("name")[0]);
        c.setUser(User.getUser(params.get("userId")[0]));
        
        c.save();
        
        return ok(json.write(c));
    }
    
    public static Result create () throws JsonParseException, JsonMappingException, IOException {
        Map<String,String[]> params = request().body().asFormUrlEncoded();
        
        FeedCollection c = new FeedCollection();
        
        // TODO: logged-in user
        // TODO: fail gracefully
        c.setName(params.get("name")[0]);
        c.setUser(User.getUser(params.get("userId")[0]));
        
        c.save();
        
        return ok(json.write(c));
    }
}
