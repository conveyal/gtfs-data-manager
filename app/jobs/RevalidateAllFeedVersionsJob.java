package jobs;

import models.FeedVersion;

/**
 * Re-run the validator on every feed version. Useful when something about how the validator has changed.
 * Note that this will chew much time and CPU.
 * @author mattwigway
 *
 */
public class RevalidateAllFeedVersionsJob implements Runnable {

    // TODO: multithread?
    public void run() {
        for (FeedVersion v : FeedVersion.getAll()) {
            v.validate();
            v.save();
        }
    }
}
