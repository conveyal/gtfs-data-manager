package jobs;

import models.FeedSource;
import models.FeedSource.FeedRetrievalMethod;
import play.Logger;

/**
 * Fetch all of the autofetched feeds that need to be fetched.
 * @author mattwigway
 *
 */
public class FetchGtfsJob implements Runnable {
    public void run () {
        Logger.info("Fetching GTFS feeds");

        for (FeedSource source : FeedSource.getAll()) {
            if (!FeedRetrievalMethod.FETCHED_AUTOMATICALLY.equals(source.retrievalMethod))
                continue;

            source.fetch();
        }

        Logger.info("Done fetching GTFS feeds");
    }
}
