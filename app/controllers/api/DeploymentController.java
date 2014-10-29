package controllers.api;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import models.Deployment;
import models.FeedCollection;
import models.FeedVersion;
import models.JsonViews;
import models.User;
import controllers.Admin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Admin.class)
public class DeploymentController extends Controller {
    private static JsonManager<Deployment> json =
            new JsonManager<Deployment>(Deployment.class,JsonViews.UserInterface.class);

    public static Result get (String id) throws JsonProcessingException {
        return ok(json.write(Deployment.get(id))).as("application/json");
    }

    public static Result getAll () throws JsonProcessingException {
        return ok(json.write(Deployment.getAll()));
    }

    public static Result create () throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        JsonNode params = request().body().asJson();

        // find the feed collection
        FeedCollection c = FeedCollection.get(params.get("feedCollection").get("id").asText());

        Deployment d = new Deployment(c);
        d.setUser(currentUser);

        applyJsonToDeployment(d, params);
        
        return ok(json.write(d)).as("application/json");
    }
    
    public static Result update (String id) throws JsonProcessingException {
        Deployment d = Deployment.get(id);
        
        if (d == null)
            return notFound();
        
        JsonNode params = request().body().asJson();
        applyJsonToDeployment(d, params);
        
        return ok(json.write(d)).as("application/json");
    }

    /**
     * Apply JSON params to a deployment. 
     * @param d
     * @param params
     */
    private static void applyJsonToDeployment(Deployment d, JsonNode params) {
        JsonNode versions = params.get("feedVersions");

        if (versions != null) {
            ArrayList<FeedVersion> versionsToInsert = new ArrayList<FeedVersion>(versions.size());
            
            for (JsonNode version : versions) {
                FeedVersion v = FeedVersion.get(version.get("id").asText());

                if (v.getFeedSource().feedCollectionId.equals(d.feedCollectionId)) {
                    versionsToInsert.add(v);
                }
            }
            
            d.setFeedVersions(versionsToInsert);
        }
    }
}