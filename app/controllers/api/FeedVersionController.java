package controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;

import controllers.Secured;
import models.FeedVersion;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class FeedVersionController extends Controller {
    private static JsonManager<FeedVersion> json = new JsonManager<FeedVersion>();
    
    public static Result get (String id) throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        
        FeedVersion v = FeedVersion.get(id);
        
        if (Boolean.TRUE.equals(currentUser.admin) || currentUser.id.equals(v.userId)) {
            return ok(json.write(v));
        }
        else {
            return unauthorized();
        }
    }
}
