package de.kiezatlas.plugins.twitter.migrations;

import de.deepamehta.core.CompositeValue;
import java.util.logging.Logger;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.Topic;
import de.deepamehta.core.TopicType;
import de.deepamehta.core.model.*;

public class Migration1 extends Migration {

    private Logger logger = Logger.getLogger(getClass().getName());

    private String WS_DEFAULT_URI = "de.workspaces.deepamehta";

    private String COMPOSITE_TYPE_URI = "dm4.core.composite";
    private String TEXT_TYPE_URI = "dm4.core.text";
    private String HTML_TYPE_URI = "dm4.core.html";

    private String AGGREGATION_DEF_EDGE_TYPE = "dm4.core.aggregation_def";
    private String COMPOSITION_DEF_EDGE_TYPE = "dm4.core.composition_def";
    private String ASSOCIATION_EDGE_TYPE = "dm4.core.association";

    private String ROLE_PARENT_TYPE_URI = "dm4.core.parent_type";
    private String ROLE_DEFAULT_TYPE = "dm4.core.default";
    private String ROLE_CHILD_TYPE_URI = "dm4.core.child_type";

    // private String TWEET_TYPE_URI = "org.deepamehta.twitter.tweet";
    // private String TWEET_FACET_TYPE_URI = "de.kiezatlas.twitter.tweet_facet";

    // private String KIEZ_SITE_TYPE_URI = "dm4.kiezatlas.website";
    // private String KIEZ_SITE_NAME_TYPE_URI = "dm4.kiezatlas.website_title";
    // private String TWITTER_SITE_URI = "de.kiezatlas.site.twitter";
    // private String GEO_OBJECT_TYPE_URI = "dm4.kiezatlas.geo_object";
    // private final String SITE_NAME = "Kiezatlas Twitter Site";

    private String TWITTER_GEOMAP_URI = "de.kiezatlas.geomap.twitter";
    private final String GEOMAP_NAME = "Kiezatlas Ten'versary Citymap";


    @Override
    public void run() {

        // 1) We extend a "Geo Object" to contain a "Tweet"
        /** dms.createAssociation(new AssociationModel(AGGREGATION_DEF_EDGE_TYPE,
                new TopicRoleModel(GEO_OBJECT_TYPE_URI, ROLE_PARENT_TYPE_URI),
                new TopicRoleModel(TWEET_TYPE_URI, ROLE_CHILD_TYPE_URI)), null); **/
        //
        // 1) Create TopicType "Tweet Facet" (Composite) for "Tweet"
        /** TopicType tweet_facet = dms.createTopicType(new TopicTypeModel(TWEET_FACET_TYPE_URI, "Tweet Facet", COMPOSITE_TYPE_URI), null);
        TopicType tweet = dms.getTopicType(TWEET_TYPE_URI);
        tweet_facet.addAssocDef(new AssociationDefinitionModel(AGGREGATION_DEF_EDGE_TYPE, TWEET_FACET_TYPE_URI,
                tweet.getUri(), "dm4.core.one", "dm4.core.one"));**/
        /** // 2) Create TopicType "Tweet Content Facet" (Composite) and "Tweet Content" (HTML)
        Topic tweet_content_facet = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_content_facet", "Tweet Content Facet", COMPOSITE_TYPE_URI), null);
        Topic tweet_content = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_content", "Tweet Content", HTML_TYPE_URI), null);
        dms.createAssociation(new AssociationModel(COMPOSITION_DEF_EDGE_TYPE,
                new TopicRoleModel(tweet_content_facet.getId(), ROLE_PARENT_TYPE_URI),
                new TopicRoleModel(tweet_content.getId(), ROLE_CHILD_TYPE_URI)), null);
        // 3) Create TopicType "Twitter Username Facet" (Composite) and "Twitter Username" (Text)
        Topic username_facet = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_username_facet", "Tweet Username Facet", COMPOSITE_TYPE_URI), null);
        Topic username = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_username", "Tweet Username", TEXT_TYPE_URI), null);
        dms.createAssociation(new AssociationModel(COMPOSITION_DEF_EDGE_TYPE,
                new TopicRoleModel(username_facet.getId(), ROLE_PARENT_TYPE_URI),
                new TopicRoleModel(username.getId(), ROLE_CHILD_TYPE_URI)), null);
        // 3) Create TopicType "Twitter Avatar Facet" (Composite) and "Twitter Avatar" (Text)
        Topic avatar_facet = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_avatar_facet", "Twitter Avatar Facet", COMPOSITE_TYPE_URI), null);
        Topic avatar = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_avatar", "Twitter Avatar", TEXT_TYPE_URI), null);
        dms.createAssociation(new AssociationModel(COMPOSITION_DEF_EDGE_TYPE,
                new TopicRoleModel(avatar_facet.getId(), ROLE_PARENT_TYPE_URI),
                new TopicRoleModel(avatar.getId(), ROLE_CHILD_TYPE_URI)), null);
        // 4) Create TopicType "Twitter Media Facet" (Composite) and "Twitter Media URL" (Text)
        Topic media_url_facet = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_media_url_facet", "Twitter Media URL Facet", COMPOSITE_TYPE_URI), null);
        Topic media_url = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_media_url", "Twitter Media URL", TEXT_TYPE_URI), null);
        dms.createAssociation(new AssociationModel(COMPOSITION_DEF_EDGE_TYPE,
                new TopicRoleModel(media_url_facet.getId(), ROLE_PARENT_TYPE_URI),
                new TopicRoleModel(media_url.getId(), ROLE_CHILD_TYPE_URI)), null);
        // 5) Create TopicType "Twitter Timestamp Facet" (Composite) and "Twitter Timestamp" (Text)
        Topic timestamp_facet = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_timestamp_facet", "Twitter Timestamp Facet", COMPOSITE_TYPE_URI), null);
        Topic timestamp = dms.createTopicType(new TopicTypeModel("de.kiezatlas.twitter.tweet_timestamp_url", "Twitter Timestamp", TEXT_TYPE_URI), null);
        dms.createAssociation(new AssociationModel(COMPOSITION_DEF_EDGE_TYPE,
                new TopicRoleModel(timestamp_facet.getId(), ROLE_PARENT_TYPE_URI),
                new TopicRoleModel(timestamp.getId(), ROLE_CHILD_TYPE_URI)), null); **/
        // 6) We create our "Site" and associate it with our "Geomap"
        /** Topic citymap = dms.createTopic(new TopicModel(TWITTER_SITE_URI, KIEZ_SITE_TYPE_URI,
                new SimpleValue(SITE_NAME)), null);
        citymap.setCompositeValue(new CompositeValueModel().put(KIEZ_SITE_NAME_TYPE_URI, SITE_NAME), null, null); **/
        // 7) Marry "Site" and "Tweet Facet"
        /** dms.createAssociation(new AssociationModel(ASSOCIATION_EDGE_TYPE, new TopicRoleModel(citymap.getId(),
                ROLE_DEFAULT_TYPE), new TopicRoleModel(tweet_facet.getId(), ROLE_DEFAULT_TYPE)), null); **/
        // CompositeValue site_model = citymap.getCompositeValue(); // maybe
        //
        /* // 7) Create all associations between our "Site" and all "Facets"
        dms.createAssociation(new AssociationModel(ASSOCIATION_EDGE_TYPE, new TopicRoleModel(citymap.getId(),
                ROLE_DEFAULT_TYPE), new TopicRoleModel(tweet_content_facet.getId(), ROLE_DEFAULT_TYPE)), null);
        dms.createAssociation(new AssociationModel(ASSOCIATION_EDGE_TYPE, new TopicRoleModel(citymap.getId(),
                ROLE_DEFAULT_TYPE), new TopicRoleModel(media_url_facet.getId(), ROLE_DEFAULT_TYPE)), null);
        dms.createAssociation(new AssociationModel(ASSOCIATION_EDGE_TYPE, new TopicRoleModel(citymap.getId(),
                ROLE_DEFAULT_TYPE), new TopicRoleModel(username_facet.getId(), ROLE_DEFAULT_TYPE)), null);
        dms.createAssociation(new AssociationModel(ASSOCIATION_EDGE_TYPE, new TopicRoleModel(citymap.getId(),
                ROLE_DEFAULT_TYPE), new TopicRoleModel(avatar_facet.getId(), ROLE_DEFAULT_TYPE)), null);
        dms.createAssociation(new AssociationModel(ASSOCIATION_EDGE_TYPE, new TopicRoleModel(citymap.getId(),
                ROLE_DEFAULT_TYPE), new TopicRoleModel(tweet_url_facet.getId(), ROLE_DEFAULT_TYPE)), null);
        dms.createAssociation(new AssociationModel(ASSOCIATION_EDGE_TYPE, new TopicRoleModel(citymap.getId(),
                ROLE_DEFAULT_TYPE), new TopicRoleModel(timestamp_facet.getId(), ROLE_DEFAULT_TYPE)), null); **/
        // n-1) Create "Geomap"
        // n+1) Realte "Site" with "Geomap"
        // n+2) Relate "Site" with "Facets"
    }

    // === Workspace ===

    private void assignWorkspace(Topic topic) {
        if (hasWorkspace(topic)) {
            return;
        }
        Topic defaultWorkspace = dms.getTopic("uri", new SimpleValue(WS_DEFAULT_URI), false);
        dms.createAssociation(new AssociationModel("dm4.core.aggregation",
            new TopicRoleModel(topic.getId(), "dm4.core.parent"),
            new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.child")
        ), null);
    }

    private boolean hasWorkspace(Topic topic) {
        return topic.getRelatedTopics("dm4.core.aggregation", "dm4.core.parent", "dm4.core.child",
            "dm4.workspaces.workspace", false, false, 0).getSize() > 0;
    }
}
