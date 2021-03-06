package controllers.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import controllers.Secured;
import jobs.DeployJob;
import models.*;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import scala.concurrent.duration.Duration;
import utils.DeploymentManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Security.Authenticated(Secured.class)
public class DeploymentController extends Controller {
    private static JsonManager<Deployment> json =
            new JsonManager<Deployment>(Deployment.class, JsonViews.UserInterface.class);
    
    private static JsonManager<DeployJob.DeployStatus> statusJson =
            new JsonManager<DeployJob.DeployStatus>(DeployJob.DeployStatus.class, JsonViews.UserInterface.class);

    private static HashMap<String, DeployJob> deploymentJobsByServer = new HashMap<String, DeployJob>();
    
    public static Result get (String id) throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        Deployment d = Deployment.get(id);
        
        if (!currentUser.admin && !currentUser.equals(d.getUser()))
            return unauthorized();
        else
            return ok(json.write(d)).as("application/json");
    }

    /** Download all of the GTFS files in the feed */
    public static Result download (String id) throws IOException {
        User currentUser = User.getUserByUsername(session("username"));
        Deployment d = Deployment.get(id);

        if (!currentUser.admin && !currentUser.equals(d.getUser()))
            return unauthorized();

        File temp = File.createTempFile("deployment", ".zip");
        // just include GTFS, not any of the ancillary information
        d.dump(temp, false, false, false);

        FileInputStream fis = new FileInputStream(temp);

        response().setContentType("application/zip");
        response().setHeader("Content-Disposition", "attachment;filename=" + d.name.replaceAll("[^a-zA-Z0-9]", "") + ".zip");

        // will not actually be deleted until download has completed
        // http://stackoverflow.com/questions/24372279
        temp.delete();

        return ok(fis);
    }

    public static Result getAll () throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        if (!currentUser.admin)
            return unauthorized();
        
        String feedCollectionId = request().getQueryString("feedCollection");
        if (feedCollectionId != null) {
            FeedCollection c = FeedCollection.get(feedCollectionId);
            return ok(json.write(c.getDeployments())).as("application/json");
        }
        else {
            return ok(json.write(Deployment.getAll())).as("application/json");
        }
    }

    public static Result create () throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        JsonNode params = request().body().asJson();

        if (!currentUser.admin)
            return unauthorized();
        
        // find the feed collection
        FeedCollection c = FeedCollection.get(params.get("feedCollection").get("id").asText());

        Deployment d = new Deployment(c);
        d.setUser(currentUser);

        applyJsonToDeployment(d, params);
        
        d.save();
        
        return ok(json.write(d)).as("application/json");
    }
    
    /**
     * Create a deployment for a particular feedsource
     * @throws JsonProcessingException 
     */
    public static Result createFromFeedSource (String feedSourceId) throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        FeedSource s = FeedSource.get(feedSourceId);
        
        // three ways to have permission to do this:
        // 1) be an admin
        // 2) be the autogenerated user associated with this feed
        // 3) have access to this feed through project permissions
        // if all fail, the user cannot do this.
        if (!currentUser.admin && !currentUser.equals(s.getUser()) && !currentUser.hasWriteAccess(s.id))
            return unauthorized();
        
        // never loaded
        if (s.getLatestVersionId() == null)
            return badRequest();  
        
        Deployment d = new Deployment(s);
        d.setUser(currentUser);
        d.save();
        
        return ok(json.write(d)).as("application/json");
    }
    
    @BodyParser.Of(value=BodyParser.Json.class, maxLength=1024*1024)
    public static Result update (String id) throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        Deployment d = Deployment.get(id);
        
        if (!currentUser.admin && !currentUser.equals(d.getUser()))
            return unauthorized();
        
        if (d == null)
            return notFound();
        
        JsonNode params = request().body().asJson();
        applyJsonToDeployment(d, params);
        
        d.save();
        
        return ok(json.write(d)).as("application/json");
    }

    /**
     * Apply JSON params to a deployment. 
     * @param d
     * @param params
     */
    private static void applyJsonToDeployment(Deployment d, JsonNode params) {
        JsonNode versions = params.get("feedVersions");

        if (versions != null && !(versions instanceof NullNode)) {
            ArrayList<FeedVersion> versionsToInsert = new ArrayList<FeedVersion>(versions.size());
            
            for (JsonNode version : versions) {
                FeedVersion v = FeedVersion.get(version.get("id").asText());

                if (v.getFeedSource().feedCollectionId.equals(d.feedCollectionId)) {
                    versionsToInsert.add(v);
                }
            }
            
            d.setFeedVersions(versionsToInsert);
        }
        
        String name = params.get("name").asText();
        
        if (name != null) {
            d.name = name;
        }
    }
    
    /**
     * Create a deployment bundle, and push it to OTP
     * @throws IOException 
     */
    public static Result deploy (String id, String target) throws IOException {
        User currentUser = User.getUserByUsername(session("username"));
        Deployment d = Deployment.get(id);
        
        if (!currentUser.admin && !currentUser.equals(d.getUser()))
            return unauthorized();
        
        if (!currentUser.admin && DeploymentManager.isDeploymentAdmin(target))
            return unauthorized();
        
        // check if we can deploy
        if (deploymentJobsByServer.containsKey(target)) {
            DeployJob currentJob = deploymentJobsByServer.get(target);
            if (currentJob != null && !currentJob.getStatus().completed) {
                // send a 503 service unavailable as it is not possible to deploy to this target right now;
                // someone else is deploying
                return status(503);
            }
        }
        
        List<String> targetUrls = DeploymentManager.getDeploymentUrls(target);
        
        Deployment oldD = Deployment.getDeploymentForServerAndRouterId(target, d.routerId);
        if (oldD != null) {
            oldD.deployedTo = null;
            oldD.save();
        }
        
        d.deployedTo = target;
        d.save();
        
        DeployJob job = new DeployJob(d, targetUrls, DeploymentManager.getPublicUrl(target), DeploymentManager.getS3Bucket(target), DeploymentManager.getS3Credentials(target));
        
        deploymentJobsByServer.put(target, job);
        
        Akka.system().scheduler().scheduleOnce(
                Duration.create(50, TimeUnit.MILLISECONDS),
                job,
                Akka.system().dispatcher()
                );
        
        return ok();
    }
    
    /**
     * The current status of a deployment, polled to update the progress dialog.
     * @throws JsonProcessingException 
     */
    public static Result deploymentStatus (String target) throws JsonProcessingException {
        // this is not access-controlled beyond requiring auth, which is fine
        // there's no good way to know who should be able to see this.
        if (!deploymentJobsByServer.containsKey(target))
            return notFound();
        
        DeployJob j = deploymentJobsByServer.get(target);
        
        if (j == null)
            return notFound();

        return ok(statusJson.write(j.getStatus())).as("application/json");
    }
    
    /**
     * The servers that it is possible to deploy to.
     */
    public static Result targets () {
        User currentUser = User.getUserByUsername(session("username"));
        return ok(Json.toJson(DeploymentManager.getDeploymentNames(currentUser.admin)));
    }
}