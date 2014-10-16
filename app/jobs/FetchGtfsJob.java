package jobs;

import models.FeedSource;
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
            if (!source.autofetch)
                continue;

            source.fetch();
        }

        Logger.info("Done fetching GTFS feeds");
    }
}
