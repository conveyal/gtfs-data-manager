package jobs;

import models.FeedSource;

public class FetchSingleFeedJob implements Runnable {

    private FeedSource feedSource;

    public FetchSingleFeedJob (FeedSource feedSource) {
        this.feedSource = feedSource;
    }
    
    @Override
    public void run() {
        // TODO: fetch automatically vs. manually vs. in-house
        feedSource.fetch();
    }

}
