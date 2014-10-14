package controllers.api;

import java.io.IOException;
import java.util.Map;

import models.User;
    
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {
    private static JsonManager<User> json = new JsonManager<User>();
    
    public static JsonManager<User> getJsonManager() {
        return json;
    }
    
    public static Result get (String id) throws JsonProcessingException {
        User u = User.getUser(id);
        // TODO: cheesy way to not expose hashed passwords
            u.passwordHash = "";
        return ok(json.write(u));
    }
    
    public static Result update (String id) throws JsonProcessingException {
        Map<String,String[]> params = request().body().asFormUrlEncoded();
        
        User u = User.getUser(id);
        
        // TODO: access control
        
       // if (params.containsKey("password"))
       //     u.setPassword(params.get("password")[0]);
        
        if (params.containsKey("email"))
            u.email = params.get("email")[0];
        
        if (params.containsKey("admin"))
            u.admin = "true".equals(params.get("admin")[0]);
        
        u.save();
        
        u.passwordHash = "";
        
        return ok(json.write(u));
    }
    
    public static Result create () throws JsonParseException, JsonMappingException, IOException {
        Map<String,String[]> params = request().body().asFormUrlEncoded();
        
        User u = new User(params.get("username")[0], params.get("password")[0], params.get("email")[0]);
        
        if (params.containsKey("admin"))
            u.admin = "true".equals(params.get("admin")[0]);
        
        u.save();
        
        u.passwordHash = "";
        
        return ok(json.write(u));
    }
}
