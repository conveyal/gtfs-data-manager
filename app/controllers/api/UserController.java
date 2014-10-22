package controllers.api;

import java.io.IOException;
import java.util.Map;

import models.JsonViews;
import models.User;
    

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import controllers.Admin;

@Security.Authenticated(Admin.class)
public class UserController extends Controller {
    private static JsonManager<User> json = new JsonManager<User>(JsonViews.UserInterface.class);
    
    public static JsonManager<User> getJsonManager() {
        return json;
    }
    
    public static Result get (String id) throws JsonProcessingException {
        User currentUser = User.getUserByUsername(session("username"));
        User u = User.getUser(id);
        
        if (!(currentUser.equals(u)))
            return unauthorized();
        else
            return ok(json.write(u)).as("application/json");
    }
    
    public static Result update (String id) throws JsonProcessingException {        
        Map<String,String[]> params = request().body().asFormUrlEncoded();
        
        User currentUser = User.getUserByUsername(session("username"));
        User u = User.getUser(id);
        
        if (!(currentUser.equals(u)))
            return unauthorized();
        
       // if (params.containsKey("password"))
       //     u.setPassword(params.get("password")[0]);
        
        if (params.containsKey("email"))
            u.email = params.get("email")[0];
        
        if (params.containsKey("admin"))
            u.admin = "true".equals(params.get("admin")[0]);
        
        u.save();
        
        return ok(json.write(u)).as("application/json");
    }
    
    public static Result create () throws JsonParseException, JsonMappingException, IOException {
        Map<String,String[]> params = request().body().asFormUrlEncoded();
        
        User currentUser = User.getUserByUsername(session("username"));
        
        if (!(Boolean.TRUE.equals(currentUser.admin)))
            return unauthorized();
        
        User u = new User(params.get("username")[0], params.get("password")[0], params.get("email")[0]);
        
        if (params.containsKey("admin"))
            u.admin = "true".equals(params.get("admin")[0]);
        
        u.save();
        
        return ok(json.write(u)).as("application/json");
    }
}
