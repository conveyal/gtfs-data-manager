import controllers.api.FeedCollectionController;
import models.FeedCollection;
import play.*;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Collection;

public class Global extends GlobalSettings {

    // For CORS
    private class ActionWrapper extends Action.Simple {
        public ActionWrapper(Action<?> action) {
            this.delegate = action;
        }

        @Override
        public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
            Promise<Result> result = this.delegate.call(ctx);
            Http.Response response = ctx.response();
            response.setHeader("Access-Control-Allow-Origin", "*");
            return result;
        }
    }

    @Override
    public Action<?> onRequest(Http.Request request,
                               java.lang.reflect.Method actionMethod) {
        return new ActionWrapper(super.onRequest(request, actionMethod));
    }
    @Override
    public void onStart(Application app) {
        Logger.info("Checking for auto fetch tasks...");
        Collection<FeedCollection> collections = FeedCollection.getAll();
        for (FeedCollection fc : collections){
            if (fc.autoFetchFeeds != null && FeedCollectionController.autoFetchMap.get(fc.id) == null){
                if (fc.autoFetchFeeds) {
                    FeedCollectionController.autoFetchMap.put(fc.id, FeedCollectionController.scheduleAutoFeedFetch(fc.id, fc.autoFetchHour, fc.autoFetchMinute, 1, fc.defaultTimeZone));
                }
            }
        }
    }
}