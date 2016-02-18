package jobs;


import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import models.FeedCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by landon on 2/17/16.
 */
public class FetchProjectFeedsActor extends UntypedActor {

    public static final Logger LOG = LoggerFactory.getLogger(FetchProjectFeedsActor.class);

    /**
     * Create Props for an actor of this type.
     * @param id Feed collection id to be passed to fetch actor
     * @return a Props for creating this actor, which can then be further configured
     *         (e.g. calling `.withDispatcher()` on it)
     */
    public static Props props(final String id) {
        return Props.create(new Creator<FetchProjectFeedsActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public FetchProjectFeedsActor create() throws Exception {
                return new FetchProjectFeedsActor(id);
            }
        });
    }

    final String id;

    public FetchProjectFeedsActor(String id) {
        this.id = id;
    }

    @Override
    public void onReceive(Object msg) {
        // some behavior here

        FeedCollection c = FeedCollection.get(id);

        LOG.info("Scheduling the feed auto fetch daemon for " + c.id);

        FetchProjectFeedsJob job = new FetchProjectFeedsJob(c);
        job.run();
    }

}