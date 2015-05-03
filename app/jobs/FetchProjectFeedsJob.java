package jobs;

import models.FeedCollection;
import models.FeedSource;
import models.FeedVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by demory on 4/29/15.
 */
public class FetchProjectFeedsJob implements Runnable {

    private FeedCollection feedCollection;
    public Map<String, FeedVersion> result;

    public FetchProjectFeedsJob (FeedCollection feedCollection) {
        this.feedCollection = feedCollection;
    }

    @Override
    public void run() {
        result = new HashMap<>();

        for(FeedSource feedSource : feedCollection.getFeedSources()) {
            if (!FeedSource.FeedRetrievalMethod.FETCHED_AUTOMATICALLY.equals(feedSource.retrievalMethod))
                continue;

            FeedVersion feedVersion = feedSource.fetch();
            result.put(feedSource.id, feedVersion);
        }
    }

}
