package controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;

import controllers.Secured;
import models.FeedCollection;
import models.FeedSource;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;


//@Security.Authenticated(Secured.class)
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
}
