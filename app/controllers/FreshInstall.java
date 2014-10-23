package controllers;

import models.User;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * Only allow the method to be called on a fresh install of the data manager.
 */
public class FreshInstall extends Action.Simple {
    public Promise<Result> call(Context ctx) throws Throwable {
        if (User.usersExist()) {
            // so this is rather unfortunate: we need to return Promise<Result> even though the computation is
            // essentially instantaneous.
            Promise<Result> result = Promise.promise(
                new Function0<Result>() {
                    public Result apply () {
                        return badRequest("This command is only available on a fresh install");
                    }
                }
                    
            );
            
            return result;
        }
            
        
        return delegate.call(ctx);
    }
}
