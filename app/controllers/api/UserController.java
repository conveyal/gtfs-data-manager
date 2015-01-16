package controllers.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.JsonViews;
import models.User;
    





import models.User.ProjectPermissions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import controllers.Admin;
import controllers.Secured;

@Security.Authenticated(Secured.class)
public class UserController extends Controller {
    private static JsonManager<User> json =
            new JsonManager<User>(User.class, JsonViews.UserInterface.class);
    
    public static JsonManager<User> getJsonManager() {
        return json;
    }
    
    /** Get all of the users 
     * @throws JsonProcessingException */
    public static Result getAll () throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));

        if (Boolean.TRUE.equals(currentUser.admin))
            return ok(json.write(User.getAll()));
        else
            return unauthorized();
    }
    
    public static Result get (String id) throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        User u = User.getUser(id);
        
        if (!(currentUser.equals(u) || Boolean.TRUE.equals(currentUser.admin)))
            return unauthorized();
        else
            return ok(json.write(u)).as("application/json");
    }
    
    /**
     * Apply the JSON to the user. This should only be run if the current user is an admin,
     * as it changes things that only admins should be allowed to change.
     */
    private static void applyJsonToUser (User u, JsonNode params) {
        if (params.has("admin"))
            u.admin = params.get("admin").asBoolean();
        
        if (params.has("active"))
            u.active = params.get("active").asBoolean();
        
        if (params.has("projectPermissions")) {
            // build out the project permissions list
            JsonNode pp = params.get("projectPermissions");
            
            if (pp == null || pp instanceof NullNode) {
                u.projectPermissions = null;
            }
            else {
                List<ProjectPermissions> perms = new ArrayList<ProjectPermissions>(pp.size());
                
                for (int i = 0; i < pp.size(); i++) {
                    JsonNode j = pp.get(i);
                    ProjectPermissions p = new ProjectPermissions();
                    p.project_id = j.get("project_id").asText();
                    p.read = j.get("read").asBoolean();
                    p.write = j.get("write").asBoolean();
                    p.admin = j.get("admin").asBoolean();
                    
                    perms.add(p);
                }
                
                u.projectPermissions = perms;
            }
        }
    }
    
    public static Result update (String id) throws JsonProcessingException {        
        JsonNode params = request().body().asJson();
        
        User currentUser = User.getUserByUsername(session("username"));
        User u = User.getUser(id);
        
        if (!(currentUser.equals(u) || Boolean.TRUE.equals(currentUser.admin)))
            return unauthorized();
        
        if (params.has("password"))
            // validation is handled in the model
            u.setPassword(params.get("password").asText());
        
        if (params.has("email"))
            u.email = params.get("email").asText();
        
        if (Boolean.TRUE.equals(currentUser.admin)) {
            applyJsonToUser(u, params);
        }
        if (params.has("admin"))
            u.admin = params.get("admin").asBoolean();
        
        u.save();
        
        return ok(json.write(u)).as("application/json");
    }
    
    public static Result create () throws JsonParseException, JsonMappingException, IOException {
        JsonNode params = request().body().asJson();
        
        User currentUser = User.getUserByUsername(session("username"));
        
        if (!(Boolean.TRUE.equals(currentUser.admin)))
            return unauthorized();
        
        User u = new User(params.get("username").asText(), params.get("password").asText(), params.get("email").asText());
        
        applyJsonToUser(u, params);
        
        u.save();
        
        return ok(json.write(u)).as("application/json");
    }
}
