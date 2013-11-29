/**
 * This is some new KiezatlasJS citymap client designed to run on all major mobile screens.
 * @author  Malte Rei&szlig;ig (malte@mikromedia.de), Copyright (c) 2013
 * @license   GPLv3 (http://www.gnu.org/licenses/gpl-3.0.en.html)
 *
 * @requires  jQuery JavaScript Library v1.9.1, Copyright 2013, John Resig
              Dual licensed under the MIT or GPL Version 2 licenses.(http://jquery.org/license)
 * @requires  leaflet.js, Copyright (c) 2010-2012, CloudMade, Vladimir Agafonkin, All rights reserved.
 *            Used in version/with source code at https://github.com/mukil/Leaflet
 * @requires  jQueryMobile Library v1.3.1, Copyright 2013, John Resig
 *            Dual licensed under the MIT or GPL Version 2 licenses.(http://jquery.org/license)
 *
 * Implementation Notes:
 * - load*-Methods set data (and even return data if a handler is given)
 * - render* or show*-Methods depend on jquery, jquerymobile and a specific DOM/Layout/IDs
 * - get/set* Methods operate on the kiezatlas-object itself (and use its reference as a client-side model)
 *
 * @modified  07 August 2013
 */

var kiezatlas = new function() {
    //
    this.mapTopics = undefined;
    this.mapTopic = undefined;
    this.city_map_id = undefined;
    this.defaultMapCenter = undefined;
    this.markersBounds = undefined; // L.LatLngBounds Object
    this.locationCircle = undefined; // L.Circle () if already set once..
    this.mapLayer = undefined;
    this.map = undefined; // L.Map();
    //
    this.markers = undefined; // arrray of L.Marker Objects
    //
    this.LEVEL_OF_DETAIL_ZOOM = 15; // the map focus when a mapinternal infoWindow is rendered
    this.LEVEL_OF_STREET_ZOOM = 14; // the map focus when a mapinternal infoWindow is rendered
    this.LEVEL_OF_KIEZ_ZOOM = 13;
    this.LEVEL_OF_DISTRICT_ZOOM = 12;
    this.LEVEL_OF_CITY_ZOOM = 11;
    //
    this.layer = undefined;
    //
    this.historyApiSupported = window.history.pushState;
    this.panorama = undefined; // currently unused: gui helper flag

    /** initializes an interactive citymap view
     *  fixme: fails if there is just 1 element in the geomap result */
    this.render_mobile_city_map_view = function () {
        // check if, and if not, initialize leaflet
        if (kiezatlas.map == undefined) {
            // console.log(" map is not initialized.. initializing..")
            kiezatlas.set_map(new L.Map('map'))
        }
        // update model
        // kiezatlas.loadCityObjectInfo(kiezatlas.city_map_id)
        kiezatlas.load_geomap_objects(kiezatlas.city_map_id);
        kiezatlas.setup_leaflet_markers(true) // all L.LatLng()s are constructed here
        kiezatlas.render_leaflet_container(true)
        // set tmp page title
        kiezatlas.render_mobile_info_title("#kiezatlas-Stadtplan");
        // ask for users location
        // kiezatlas.ask_users_location()
        jQuery('#present').click(function(e) {
            jQuery('#present').remove()
        })
    }

    this.render_leaflet_container = function (reset) {
        // update gui
        $("#map").height($(window).innerHeight())
        kiezatlas.map.invalidateSize()
        if (reset) {
            kiezatlas.set_leaflet_map_to_current_bounds()
            kiezatlas.load_map_tiles()
            // set new page titles to citymap name
            // fixme: kiezatlas.render_mobile_info_title(kiezatlas.mapTopic.value)
        }
    }

    /**
     this.switch_mobile_city_map = function (mapId) {

        kiezatlas.city_map_id = mapId;
        // kiezatlas.workspaceId = workspaceId;
        if (kiezatlas.markers != undefined) {
            kiezatlas.clear_markers();
        }

        jQuery("img.loading").css("display", "block");
        kiezatlas.load_geomap_objects(mapId, function () {
            // kiezatlas.setup_leaflet_markers();
            // kiezatlas.set_leaflet_map_to_current_bounds();
            // kiezatlas.ask_users_location();
        });

        // ### FIXMEs mvoe GUI related manipulations into guiSetup/renderFunctions
        kiezatlas.closeInfoContainer(); // close info and  show nav
        // initiate current citymap state
        // var newLink = "/?map=" + mapId;
        // kiezatlas.push_history({ "name": "loaded", "parameter": [ mapId, workspaceId ] }, newLink);
        // kiezatlas.hideKiezatlasControl();
    } **/

    this.load_map_tiles = function() {
        var cloudmadeUrl = 'http://{s}.tiles.mapbox.com/v3/kiezatlas.map-feifsq6f/{z}/{x}/{y}.png',
            cloudmadeAttribution = "Tiles &copy; <a href='http://mapbox.com/'>MapBox</a> | " +
              "Data &copy; <a href='http://www.openstreetmap.org/'>OpenStreetMap</a> and contributors, CC-BY-SA",
            cloudmade = new L.TileLayer(cloudmadeUrl, {maxZoom: 18, attribution: cloudmadeAttribution});
        // mapbox: "http://a.tiles.mapbox.com/v3/kiezatlas.map-feifsq6f/${z}/${x}/${y}.png",
        //            attribution: "Tiles &copy; <a href='http://mapbox.com/'>MapBox</a> | " +
        //            "Data &copy; <a href='http://www.openstreetmap.org/'>OpenStreetMap</a> " +
        //            "and contributors, CC-BY-SA",
        // osm: "http://b.tile.openstreetmap.de/tiles/osmde/"
        //        attribution: 'Tile server sponsored by STRATO / <b>Europe only</b> /
        //  <a href="http://www.openstreetmap.de/germanstyle.html">About style</a>',
        // TODO: render nice info message "Map tiles are loding ..."
        /** cloudmade.on('load', function(e) {
          // is just fired when panning the first time out of our viewport, but strangely not on initiali tile-loading
          console.log("tilelayer loaded.. could invaldate #maps size..")
        }); **/
        kiezatlas.map.addLayer(cloudmade);
    }

    this.on_bubble_click = function (e) {
        var topicId = e.id;
        // load geoobject container
        kiezatlas.do_info(topicId);
    }

    this.do_info = function(id) {
        $("#infoo-area").html('<p class="content-body"></p>')
        // fixme: depends on jquerymobile
        $.mobile.changePage( "#infoo", {
            transition: "flow", reverse: false, showLoadMsg: true, changeHash: true
        })
        // fetch the data, and render it
        kiezatlas.load_object_info(id, kiezatlas.render_mobile_info_body)
    }

    this.render_object_title = function (object) {
        var full_topic = kiezatlas.load_object_info(object.id)
        return '<div id="' + object.id+ '" onclick="kiezatlas.on_bubble_click(this)" class="topic-name item">'
            + '<b>' + full_topic.value + '&nbsp;&rsaquo;&rsaquo;&rsaquo;</b></div>';
    }

    this.render_mobile_info_title = function(newTitle) {
        $(".my-title").html(newTitle)
        window.document.title = newTitle
    }

    this.render_mobile_info_body = function (object) {
        var infoHeader = '<div id="info-table">';
            infoHeader += '<h3 class="title">' + object.value + '</h3></div>';
        var address = ""
        if (object.composite.hasOwnProperty("dm4.contacts.address")) {
            address = object.composite["dm4.contacts.address"].value
        }
        var description = ""
        if (object.composite.hasOwnProperty(kiezatlas.object_facet_uri)) {
            description = object.composite[kiezatlas.object_facet_uri].value
        }
        var user = ""
        if (object.composite.hasOwnProperty("org.deepamehta.twitter.user")) {
            var entity = object.composite["org.deepamehta.twitter.user"]
            var username = entity.composite["org.deepamehta.twitter.user_name"].value
            var avatar_url = entity.composite["org.deepamehta.twitter.user_image_url"].value
            user = '<img src="'+avatar_url+'"><br/>'
            user += '<b>' + username + '</b>'
        }
        // var references = ""
        if (object.composite.hasOwnProperty("org.deepamehta.twitter.tweet_entities")) {
            var items = JSON.parse(object.composite["org.deepamehta.twitter.tweet_entities"].value)
            // media check
            if (items.hasOwnProperty('media')) {
                var media_item_one = items['media'][0]
                var media_url = media_item_one[0]["media_url"]
                var media_html = '<br/><img src="'+media_url+'" title="Twitter Media Item><br/>'
                description += media_html
            }
            // url check
            if (items.hasOwnProperty('urls') && items['urls'].length > 0) {
                var url_item_one = items['urls'][0]
                var label = url_item_one['display_url']
                var href = url_item_one['expanded_url']
                var url_html = '<a href="'+href+'" target="_blank" title="Link opens new browser window">'+label+'</a>'
                description += url_html
            }
        }
        var infoItem = '<div id="info-item">'+ user + '<br/><br/>' + description + '</div>';
        //
        jQuery("#infoo-area .content-body").html(infoHeader);
        jQuery("#infoo-area .content-body").append(infoItem);
        // add voting-interaction
        // jQuery("#infoo-area .content-body .like-buttons").append($like).append($dislike);

    }

    this.setup_leaflet_markers = function(isMobile) {
        /** currently not in use
        var KiezAtlasIcon = L.Icon.extend({
          options: {
            iconUrl: 'css/locationPointer.png',
            shadowUrl: null, iconSize: new L.Point(40, 40), shadowSize: null,
            iconAnchor: new L.Point(20, 14), popupAnchor: new L.Point(0, 4)
          }
        });
        var myIcon = new KiezAtlasIcon(); **/
        //
        if (kiezatlas.markers != undefined) {
            kiezatlas.clear_markers();
        }
        kiezatlas.markers = new Array(); // helper to keep the refs to all markes once added..
        //
        for (var i = 0; i < kiezatlas.mapTopics.topics.length; i++) {
            var topic = kiezatlas.mapTopics.topics[i];
            if (topic.type_uri === "org.deepamehta.twitter.tweet") {
                var full_topic = this.load_object_info(topic.id)
                // get info locally
                var topicId = topic.id;
                var marker = undefined;
                var latlng = undefined;
                if (typeof full_topic !== "undefined") {
                    var coordinate = full_topic.composite['dm4.geomaps.geo_coordinate']
                    var lng = coordinate.composite['dm4.geomaps.longitude'].value;
                    var lat = coordinate.composite['dm4.geomaps.latitude'].value;
                    // sanity check..
                    var skip = false;
                    if (lat == 0.0 || lng == 0.0) {
                        skip = true;
                    } else if (lng < -180.0 || lng > 180.0) {
                        skip = true;
                    } else if (lat < -90.0 || lat > 90.0) {
                        skip = true;
                    } else if (isNaN(lat) || isNaN(lng)) {
                        skip = true;
                    }
                    if (!skip) {
                        latlng = new L.LatLng(parseFloat(lat), parseFloat(lng));
                    }
                    //
                    if (latlng != undefined) {
                        var existingMarker = kiezatlas.get_marker_by_lat_lng(latlng);
                        if (existingMarker != null) {
                            marker = existingMarker;
                            // add our current, proprietary topicId to the marker-object
                            marker.options.topicId.push(topicId);
                            var previousContent = marker._popup._content;
                            marker.bindPopup(kiezatlas.render_object_title(full_topic) + previousContent);
                        } else {
                            marker = new L.Marker(latlng, {'clickable': true , 'topicId': [topicId]}); // icon: myIcon
                            marker.bindPopup(kiezatlas.render_object_title(full_topic));
                        }
                        // add marker to map object
                        kiezatlas.map.addLayer(marker);
                        // reference each marker in kiezatlas.markers model
                        kiezatlas.markers.push(marker);
                        //
                        marker.on('click', function (e) {
                            //
                            this._popup.options.autoPan = true;
                            this._popup.options.maxWidth = 160;
                            this._popup.options.closeButton = true;
                            this.openPopup();
                            //
                            // bubbles click handler consumed by on_bubble_click
                            // jQuery(".leaflet-popup-content-wrapper").click(kiezatlas.on_bubble_click);
                        }, marker);
                    }
                }
            }
        }
        // console.log("map.setup => " + kiezatlas.markers.length + " leaflets for "
           // + kiezatlas.mapTopics.topics.length + " loaded topics");
        if (isMobile) $.mobile.loader("hide")
    }



    /** Custom Methods accessing our REST- Services */

    this.add_review_good = function (event) {
        var id = event.target.id.substr(5);
        var url = "/review/good/" + id;
        jQuery.ajax({
            type: "GET", async: true,
                url: url, dataType: 'json',
                beforeSend: function(xhr) {
                xhr.setRequestHeader("Content-Type", "application/json")
            },
            success: function(obj) {
                // update gui
                kiezatlas.render_mobile_info_body(obj)
                // show notification
                var $mapMessage = $("#message-info.notification")
                $mapMessage.html('Deine Stimme wurde gez&auml;hlt')
                $mapMessage.fadeIn(600)
                setTimeout(function(e) {
                    $mapMessage.fadeOut(600)
                }, 3000)
                return null
            },
            error: function(x, s, e) {
                throw new Error('ERROR: detailed information on this point could not be loaded. please try again.' + x)
            }
        })
    }

    this.add_review_soso = function (event) {
        var id = event.target.id.substr(8);
        var url = "/review/soso/" + id;
        jQuery.ajax({
            type: "GET", async: true,
                url: url, dataType: 'json',
                beforeSend: function(xhr) {
                xhr.setRequestHeader("Content-Type", "application/json")
            },
            success: function(obj) {
                // update gui
                kiezatlas.render_mobile_info_body(obj)
                // show notification
                var $mapMessage = $("#message-info.notification")
                $mapMessage.html('Deine Stimme wurde gez&auml;hlt')
                $mapMessage.fadeIn(600)
                setTimeout(function(e) {
                    $mapMessage.fadeOut(600)
                }, 3000)
                return null
            },
            error: function(x, s, e) {
                throw new Error('ERROR: detailed information on this point could not be loaded. please try again.' + x)
            }
        })
    }

    this.load_object_info = function (topicId, render_function) {
        var url = "/core/topic/" + topicId + "?fetch_composite=true";
        var response = undefined
        jQuery.ajax({
            type: "GET", async: false,
                url: url, dataType: 'json',
                beforeSend: function(xhr) {
                xhr.setRequestHeader("Content-Type", "application/json")
            },
            success: function(obj) {
                if (typeof render_function !== 'undefined') {
                    //
                    kiezatlas.set_map_topic(obj)
                    render_function(obj)
                }
                response = obj
            },
            error: function(x, s, e) {
                throw new Error('ERROR: detailed information on this point could not be loaded. please try again.' + x)
            }
        })
        return response
    }

    /** requests and sets all geobjects of the loaded map to kiezatlas.mapTopics **/
    this.load_geomap_objects = function (mapId, handler) {
        var url = "/topicmap/" + mapId; // uses topicmap-service instead of /geomap-service
        // var body = '{"method": "getMapTopics", "params": ["' + mapId+ '" , "' + workspaceId + '"]}';
        jQuery.ajax({
            type: "GET", async: false,
                // data: body,
                url: url, dataType: 'json',
                beforeSend: function(xhr) {
                xhr.setRequestHeader("Content-Type", "application/json")
            },
            success: function(obj) {
                kiezatlas.set_map_topics(obj);
                if (handler != undefined) handler();
            },
            error: function(x, s, e) {
                throw new Error("Error while loading city-map. Message: " + JSON.stringify(x));
            }
        });
    }



    /** Handle browsers geo-location API */

    this.ask_users_location = function (options) {
        // set default options
        if (options == undefined) {
            options =  {"setView" : true, "maxZoom" : kiezatlas.LEVEL_OF_KIEZ_ZOOM};
        }
        // ask browser for location-info
        kiezatlas.map.locate(options);
        // ("img.loading").hide();
    }

    this.on_location_found = function(e) {
        var radius = e.accuracy;
        if (kiezatlas.locationCircle != undefined) {
          kiezatlas.map.removeLayer(kiezatlas.locationCircle);
        }
        var $mapMessage = $("#message.notification")
        // $mapMessage.show("fast")
        $mapMessage.html('Ihr Smartphone hat Sie gerade automatisch lokalisiert.<br/>'
            + 'Dr&uuml;cken Sie hier um den Kartenausschnit zur&uuml;ckzusetzen.')
        $mapMessage.click(kiezatlas.set_leaflet_map_to_current_bounds)
        $mapMessage.fadeIn(1000)
        setTimeout(function(e) {
            $mapMessage.fadeOut(3000)
        }, 3000)
        kiezatlas.locationCircle = new L.Circle(e.latlng, radius, {"stroke": true, "clickable": false, "color":
            "#1d1d1d", "fillOpacity": 0.3, "opacity": 0.3, "weight":10}); // show double sized circle..
        kiezatlas.map.addLayer(kiezatlas.locationCircle, {"clickable" : true});
        kiezatlas.locationCircle.bindPopup("You are within " + radius + " meters from this point");
        kiezatlas.map.setView(e.latlng, kiezatlas.LEVEL_OF_KIEZ_ZOOM);
        // kiezatlas.map.panTo(e.latlng);
    }

    this.on_location_error = function (e) {
        // TODO: doesnt matter
    }



    /** Application controler utility methods */

    this.pop_history = function (state) {
        // simulate the back and forth navigation...
        if (!this.historyApiSupported) {
            return;
        } else {
            // TODO:
            // console.log(state);
        }
    }

    this.push_history = function (state, link) {
        //
        if (!this.historyApiSupported) {
            return;
        }
        // build history entry
        var history_entry = {state: state, url: link};
        // push history entry
        window.history.pushState(history_entry.state, null, history_entry.url);
    }



    /** GUI-Utility Methods */

    /** returns a Leaflet.Marker object (for identifying and building clusters in the ui) */
    this.get_marker_by_lat_lng = function (latLng) {
        //
        for (var i = 0; i < kiezatlas.markers.length; i++) {
            var marker = kiezatlas.markers[i];
            if (marker._latlng.equals(latLng)) {
                return marker;
            }
        }
        //
        return null;
    }

    this.clear_markers = function  () {
        for (var i = 0; i < kiezatlas.markers.length; i++) {
            var m = kiezatlas.markers[i];
            try {
                kiezatlas.map.removeLayer(m);
            } catch (e) {
                console.log("Exception: " + e);
            }
        }
    }

    this.get_current_bounds = function () {
        var bounds = new L.LatLngBounds();
        for (var i = 0; i < kiezatlas.markers.length; i++) {
            var m = kiezatlas.markers[i];
            var lng = m._latlng.lng;
            var lat = m._latlng.lat;
            var skip = false;
            if (lat == 0.0 || lng == 0.0) {
                skip = true;
            } else if (lng < -180.0 || lng > 180.0) {
                skip = true;
            } else if (lat < -90.0 || lat > 90.0) {
                skip = true;
            } else if (isNaN(lat) || isNaN(lng)) {
                skip = true;
            }
            if (!skip) {
                var point = new L.LatLng(parseFloat(lat), parseFloat(lng));
                bounds.extend(point);
            }
        }
        return bounds;
    }

    /** sorting desc by item.value */
    this.alphabetical_sort_desc = function (a, b) {
        // console.log(a)
        var scoreA = a.value
        var scoreB = b.value
        if (scoreA < scoreB) // sort string descending
          return -1
        if (scoreA > scoreB)
          return 1
        return 0 //default return value (no sorting)
    }

    this.window_height = function () {
        if (self.innerHeight) {
            return self.innerHeight;
        }
        if (document.documentElement && document.documentElement.clientHeight) {
            return jQuery.clientHeight;
        }
        if (document.body) {
            return document.body.clientHeight;
        }
        return 0;
    }

    this.window_width = function () {
        if (self.innerWidth) {
            return self.innerWidth;
        }
        if (document.documentElement && document.documentElement.clientWidth) {
            return jQuery.clientWidth;
        }
        if (document.body) {
            return document.body.clientWidth;
        }
        return 0;
    }

    this.create_webpage_link = function (url, label) {
        urlMarkup = '<a href="' + url + '" target="_blank">' + label + '</a>';
            //  + '<img src="css/link_extern.gif" alt="(externer Link)" border="0" height="11" width="12"/>
        // else urlMarkup = '<a href="'+url+'" target="_blank">'+label+'</a>';
        return urlMarkup
    }

    this.create_email_link = function (url, label) {
        urlMarkup = '<a href="mailto:' + url + '" target="_blank">' + label + '</a>';
        return urlMarkup
    }



    /** Java-Style Getters & Setters */

    this.set_map = function(mapObject)  {
        this.map = mapObject;
        //
        this.map.options.touchZoom = true;
        kiezatlas.map.on('locationfound', kiezatlas.on_location_found);
        // kiezatlas.map.on('locationerror', kiezatlas.on_location_error);
    }

    this.set_map_topics = function(topics) {
        this.mapTopics = topics;
    }

    this.set_map_topic = function(topic) {
        this.mapTopic = topic;
    }

    this.set_leaflet_map_to_current_bounds = function () {
        kiezatlas.markersBounds = kiezatlas.get_current_bounds();
        kiezatlas.map.fitBounds(kiezatlas.markersBounds);
    }

}
