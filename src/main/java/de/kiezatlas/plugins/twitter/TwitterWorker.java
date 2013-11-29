package de.kiezatlas.plugins.twitter;

import de.deepamehta.core.Topic;
import de.deepamehta.core.util.DeepaMehtaUtils;
import java.util.logging.Logger;
import org.deepamehta.plugins.twitter.service.TwitterService;

public class TwitterWorker extends Thread {

    private static Logger log = Logger.getLogger(TwitterMapService.class.getName());

    private boolean threadDone = false;
    private boolean threadDead = false;

    private TwitterMapService twitterMap = null;
    private long searchTopicId = 0;
    private long updateInterval = 0;

    public TwitterWorker(TwitterMapService twitterService, long searchTopicId, long intervall) {
        this.twitterMap = twitterService;
        this.searchTopicId = searchTopicId;
        this.updateInterval = intervall;
    }

    public void run() {
        if (!threadDead) {
            while (!threadDone) {
                log.info("TwitterWorker-Thread of and runnin .. (ID " + searchTopicId);
                // work here
                twitterMap.fetchLatestGeoTweets(searchTopicId);
                done();
            }
        } else {
            threadDone = true;
            log.info("TwitterWorker-Thread is runnin out now .. ");
        }
        // running out
    }

    public void done() {
        if (!threadDead) {
            threadDone = true;
            log.info("TwitterWorker-Thread is going to sleep for " + updateInterval/1000/60 + "min.");
            try {
                // wait for a couple of minutes
                this.sleep(updateInterval);
                // restart this worker
                threadDone = false;
                run();
            } catch (InterruptedException intex) {
                log.info("*** TwitterWorker-Thread was interrupted cause of " + intex.getMessage());
            }
        } else {
            log.info("TwitterWorker-Thread named \""+getName()+"\" is DEAD ---");
        }

    }

    public void setThreadDead() {
        threadDead = true;
    }

    public boolean getThreadState() {
        return threadDead;
    }

}
