package controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import jobs.ProcessSingleFeedJob;

import com.fasterxml.jackson.core.JsonProcessingException;

import controllers.Secured;
import models.FeedSource;
import models.FeedVersion;
import models.User;
import play.Logger;
import play.libs.Akka;
import play.mvc.Http.MultipartFormData;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Security;
import scala.concurrent.duration.Duration;

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
    
    /**
     * Upload a feed version directly. This is done behind Backbone's back, and as such uses
     * x-multipart-formdata rather than a json blob. This is done because uploading files in a JSON
     * blob is not pretty, and we don't really need to get the Backbone object directly; page re-render isn't
     * a problem.
     * @return
     * @throws JsonProcessingException 
     */
    public static Result create () throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        
        MultipartFormData body = request().body().asMultipartFormData();
        Map<String, String[]> params = body.asFormUrlEncoded();
        
        FeedSource s = FeedSource.get(params.get("feedSourceId")[0]);
        
        if (!Boolean.TRUE.equals(currentUser.admin) && !currentUser.equals(s.getUser()))
            return unauthorized();
        
        if (s.autofetch)
            return badRequest("Feed is autofetched! Cannot upload.");
        
        FeedVersion v = new FeedVersion(s);
        v.setUser(currentUser);
        
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
            return internalServerError("Unable to read uploaded feed");
        }
        
        // copy the file
        ReadableByteChannel rbc = Channels.newChannel(uploadStream);
        try {
            outStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            Logger.error("Unable to transfer from upload to saved file.");
            return internalServerError("Unable to save uploaded file");
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
}
