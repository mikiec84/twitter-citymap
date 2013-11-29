package de.kiezatlas.plugins.twitter;

import com.sun.jersey.api.view.Viewable;
import de.deepamehta.core.Association;
import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.ResultSet;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.*;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.ClientState;
import de.deepamehta.core.service.event.AllPluginsActiveListener;
import de.deepamehta.core.service.PluginService;
import de.deepamehta.core.service.annotation.ConsumesService;
import de.deepamehta.core.storage.spi.DeepaMehtaTransaction;
import de.deepamehta.plugins.accesscontrol.model.ACLEntry;
import de.deepamehta.plugins.accesscontrol.model.AccessControlList;
import de.deepamehta.plugins.accesscontrol.model.Operation;
import de.deepamehta.plugins.accesscontrol.model.UserRole;
import de.deepamehta.plugins.accesscontrol.service.AccessControlService;
import de.deepamehta.plugins.facets.service.FacetsService;

import de.deepamehta.plugins.geomaps.service.GeomapsService;
import de.deepamehta.plugins.topicmaps.TopicmapViewmodel;
import de.deepamehta.plugins.topicmaps.service.TopicmapsService;
import de.deepamehta.plugins.webactivator.WebActivatorPlugin;
import java.io.InputStream;
import org.deepamehta.plugins.twitter.service.TwitterService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class TwitterMapService extends PluginActivator implements AllPluginsActiveListener {

    private static Logger log = Logger.getLogger(TwitterMapService.class.getName());

    private AccessControlService aclService = null;
    private GeomapsService geomapsService = null;
    private TwitterService twitterService = null;
    private TopicmapsService topicmapService = null;
    private FacetsService facetService = null;

    private final String AGGREGATION_DEF_EDGE_TYPE = "dm4.core.aggregation_def";
    private final String COMPOSITION_DEF_EDGE_TYPE = "dm4.core.composition_def";
    private final String ASSOCIATION_EDGE_TYPE = "dm4.core.association";

    private final String ROLE_PARENT_TYPE_URI = "dm4.core.parent_type";
    private final String ROLE_DEFAULT_TYPE = "dm4.core.default";
    private final String ROLE_CHILD_TYPE_URI = "dm4.core.child_type";

    private final String TWITTERMAP_NAME = "Kiezatlas Twitter Citymap";
    private final String TWITTER_TOPICMAP_URI = "de.kiezatlas.topicmap.twitter";
    //
    private final String TWITTER_GEOMAP_URI = "de.kiezatlas.twitter.twitter"; // unused

    private final String GEO_COORDINATE_TOPIC_URI = "dm4.geomaps.geo_coordinate";
    private final String GEO_LONGITUDE_TYPE_URI = "dm4.geomaps.longitude";
    private final String GEO_LATITUDE_TYPE_URI = "dm4.geomaps.latitude";

    private TwitterWorker twitterWorker = null;
    private final long TWITTER_SEARCH_TOPIC_ID = 4814;
    private final int QUERY_INTERVALL = 150000;



    @Override
    public void init() {
        // initTemplateEngine();
    }

    @Override
    public void allPluginsActive() {
        log.info("Twitter Map Service ACKs ALL_PLUGINS_ACTIVE ");
    }

    @Override
    public void postInstall() {
        //
        if (topicmapService != null) {
            Topic topicmap = dms.getTopic("uri", new SimpleValue(TWITTER_TOPICMAP_URI), false);
            if (topicmap == null) {
                // 1) create topicmap
                Topic twittermap = topicmapService.createTopicmap(TWITTERMAP_NAME, TWITTER_TOPICMAP_URI,
                        "dm4.webclient.default_topicmap_renderer", null);
                log.info("Twitter Citymap successfully created.. with URI \"" + twittermap.getUri() + "\"");
                // 2) Set default ACL for our new geomap
                initiateAdministrativeACL(twittermap);
                log.info("\n Twitter Citymap successfully set up with ACLs..");
            } else {
                log.warning("\n Did not create Twitter Citymap since it was already created.. \n");
            }
        } else {
            log.warning("\n Topicmap service was not there as postInstall-Event was fired \n");
        }
    }

    @GET
    @Path("/twitter/map/initialize/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String startThread(@PathParam("id") long searchTopicId) {
        startSearchPollingThread(searchTopicId);
        return "Started.";
    }

    @GET
    @Path("/twitter/map/fetch/more/{searchTopicId}")
    @Produces(MediaType.TEXT_HTML)
    public String fetchMoreGeoTweets(@PathParam("searchTopicId") long searchId) {

        Topic searchTopic = dms.getTopic(searchId, true);
        String name = searchTopic.getSimpleValue().toString();
        if (twitterService != null) {
            try {
                searchTopic = twitterService.searchMoreTweets(searchTopic.getId(), true,  null);
                processTweetSearch(searchTopic);
            } catch (Exception e) {
                if (e.getMessage().indexOf("204") != -1) name = "WARNING: There is no next page.";
            }
        } else {
            throw new WebApplicationException(new Throwable("TwitterResearch Service unavailable"), 500);
        }
        // viewData("term", name);
        // viewData("searchTopicId", searchTopic.getId());
        // return view("search-report");
        return "Fetched more.";
    }

    @GET
    @Path("/twitter/map/fetch/latest/{searchTopicId}")
    @Produces(MediaType.TEXT_HTML)
    public String fetchLatestGeoTweets(@PathParam("searchTopicId") long searchId) {

        Topic searchTopic = dms.getTopic(searchId, true);
        if (twitterService != null) {
            searchTopic = twitterService.searchMoreTweets(searchTopic.getId(), false,  null);
            processTweetSearch(searchTopic);
        } else {
            throw new WebApplicationException(new Throwable("TwitterResearch Service unavailable"), 500);
        }
        // viewData("term", searchTopic.getSimpleValue());
        // viewData("searchTopicId", searchTopic.getId());
        // return view("search-report");
        return "Fetched latest.";
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public InputStream getStartView(@HeaderParam("Cookie") ClientState clientState) {
        return invokeStartView();
    }

    // ------------------------------------------------------------------------------------------------ Private Methods

    private InputStream invokeStartView() {
        try {
            return dms.getPlugin("de.kiezatlas.twitter-citymap").getResourceAsStream("web/script/index.html");
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }

    private void processTweetSearch(Topic searchTopic) {
        CompositeValueModel results = searchTopic.getCompositeValue().getModel();
        if (!results.has("org.deepamehta.twitter.tweet")) {
            log.info("No tweets to process!");
            return;
        }
        List<TopicModel> tweets = results.getTopics("org.deepamehta.twitter.tweet");
        log.fine("Twitter Search Topic got " + tweets.size() + " tweets as result (ID => " + searchTopic.getId() + ")");
        addTweetsToTopicmap(tweets, searchTopic);
    }

    private void addTweetsToTopicmap(List<TopicModel> tweets, Topic searchTopic) {
        // Topic topicmap = dms.getTopic("uri", new SimpleValue(TWITTER_TOPICMAP_URI), false);
        ResultSet<RelatedTopic> topicmaps = searchTopic.getRelatedTopics("dm4.core.association", "dm4.core.default",
                "dm4.core.default", "dm4.topicmaps.topicmap", false, false, 0);
        if (topicmaps == null || topicmaps.getSize() == 0) {
            log.warning("We could not fetch our beloved topicmap by URI.. exiting - DOING NOTHING");
            return;
        }
        Topic topicmap = topicmaps.getItems().iterator().next();
        for (TopicModel tweet : tweets) {
            Topic full_tweet = dms.getTopic(tweet.getId(), true);
            // we just allow #kiezatlas-tweets to show up in `this` map
            // if (full_tweet.getSimpleValue().toString().indexOf("#kiezatlas") == -1) return;
            CompositeValueModel tweet_model = full_tweet.getModel().getCompositeValueModel();
            if (tweet_model.has("dm4.geomaps.geo_coordinate")) {
                addTopicToTopicmap(topicmap, full_tweet);
            } else {
                log.fine("Tweet \""+tweet.getSimpleValue()+"\" " + tweets.indexOf(tweet) + " does not have coordinates set .. ");
            }
        }
    }

    private void addTopicToTopicmap(Topic topicmap, Topic topic) {
        // 1) Check if topic is already part of that topicmap // because of an Ambiguityexception is thrown
        Association assoc = dms.getAssociation("dm4.topicmaps.topic_mapcontext", topicmap.getId(), topic.getId(),
                "dm4.core.default", "dm4.topicmaps.topicmap_topic", false);
        if (assoc == null) {
            // topic does not exist in topicmap (yet)
            topicmapService.addTopicToTopicmap(topicmap.getId(), topic.getId(), null);
            topicmapService.setTopicVisibility(topicmap.getId(), topic.getId(), true);
            topicmapService.setTopicPosition(topicmap.getId(), topic.getId(), 0, 0);
        } else {
            log.fine("SKipping Tweet \""+topic.getSimpleValue()+"\" (already part of our topicmap)");
        }
    }

    private void initiateAdministrativeACL(Topic topic) {
        if (aclService != null) {
            aclService.setCreator(topic, "admin");
            aclService.setOwner(topic, "admin");
            AccessControlList aclist = new AccessControlList()
                    .addEntry(new ACLEntry(Operation.WRITE, UserRole.CREATOR));
            aclService.setACL(topic, aclist);
        } else {
            log.warning("FAILED to set up default ACLs on our new geomap (ACLService unavailable yet).");
        }
    }

    @Override
    @ConsumesService({
        "de.deepamehta.plugins.accesscontrol.service.AccessControlService",
        "de.deepamehta.plugins.geomaps.service.GeomapsService",
        "org.deepamehta.plugins.twitter.service.TwitterService",
        "de.deepamehta.plugins.topicmaps.service.TopicmapsService",
        "de.deepamehta.plugins.facets.service.FacetsService"
    })
    public void serviceArrived(PluginService service) {
        if (service instanceof GeomapsService) {
            geomapsService = (GeomapsService) service;
        } else if (service instanceof TwitterService) {
            twitterService = (TwitterService) service;
        } else if (service instanceof AccessControlService) {
            aclService = (AccessControlService) service;
        } else if (service instanceof TopicmapsService) {
            topicmapService = (TopicmapsService) service;
        } else if (service instanceof FacetsService) {
            facetService = (FacetsService) service;
        }
    }

    @Override
    public void serviceGone(PluginService service) {
        if (service == geomapsService) {
            geomapsService = null;
        } else if (service == twitterService) {
            twitterService = null;
            stopSearchPollingThread();
        } else if (service instanceof AccessControlService) {
            aclService = null;
        } else if (service instanceof TopicmapsService) {
            topicmapService = null;
        } else if (service instanceof FacetsService) {
            facetService = null;
        }
    }

    public void stop() {
        stopSearchPollingThread();
    }

    private void stopSearchPollingThread() {
        twitterWorker.setThreadDead();
        twitterWorker = null;
        log.warning("Stopping Plugin / Service Gone -- TwitterWorker-Thread is destroyed --");
    }

    private void startSearchPollingThread(long searchTopicId) {
        if (twitterWorker == null) {
            twitterWorker = new TwitterWorker(this, searchTopicId, QUERY_INTERVALL);
            twitterWorker.setDaemon(true);
            twitterWorker.start();
        }
    }

}
