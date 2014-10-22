package controllers.api;

import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import models.FeedSource;
import models.FeedVersion;
import models.JsonViews;
import models.Model;
import models.Note;
import models.Note.NoteType;
import models.User;
import controllers.Secured;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class NoteController extends Controller {
    private static JsonManager<Note> json =
            new JsonManager<Note>(Note.class, JsonViews.UserInterface.class);
    
    public static Result getAll () throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));

        String typeStr = request().getQueryString("type");
        String objectId = request().getQueryString("objectId");
        
        if (typeStr == null || objectId == null) {
            return badRequest("Please specify objectId and type");
        }
        
        NoteType type;
        try {
            type = NoteType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return badRequest("Please specify a valid type");
        }
        
        Model model;
        
        switch (type) {
        case FEED_SOURCE:
            model = FeedSource.get(objectId);
            break;
        case FEED_VERSION:
            model = FeedVersion.get(objectId);
            break;
        default:
            // this shouldn't ever happen, but Java requires that every case be covered somehow so model can't be used uninitialized
            return badRequest("Unsupported type for notes");
        }
     
        // check if the user has permission
        if (Boolean.TRUE.equals(currentUser.admin) || currentUser.equals(model.getUser())) {
            return ok(json.write(model.getNotes())).as("application/json");
        }
        else {
            return unauthorized();
        }
    }
    
    public static Result create () throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));

        JsonNode params = request().body().asJson();
        
        String typeStr = params.get("type").asText();
        String objectId = params.get("objectId").asText();
        
        if (typeStr == null || objectId == null) {
            return badRequest("Please specify objectId and type");
        }
        
        NoteType type;
        try {
            type = NoteType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return badRequest("Please specify a valid type");
        }
        
        Model model;
        
        switch (type) {
        case FEED_SOURCE:
            model = FeedSource.get(objectId);
            break;
        case FEED_VERSION:
            model = FeedVersion.get(objectId);
            break;
        default:
            // this shouldn't ever happen, but Java requires that every case be covered somehow so model can't be used uninitialized
            return badRequest("Unsupported type for notes");
        }
     
        // check if the user has permission
        if (Boolean.TRUE.equals(currentUser.admin) || currentUser.equals(model.getUser())) {
            Note n = new Note();
            n.note = params.get("note").asText();
            // folks can't make comments as other folks
            n.userId = currentUser.id;
            n.date = new Date();
            model.addNote(n);
            n.save();
            model.save();
            
            return ok(json.write(n)).as("application/json");
        }
        else {
            return unauthorized();
        }
    }
}
