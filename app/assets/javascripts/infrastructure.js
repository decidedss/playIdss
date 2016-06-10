// Inspired by
// http://wordpress-dbauszus.rhcloud.com/wfs-t-with-openlayers-3-5/

var numOfLayers;
var infrastructureVectors = [];
var infrastructureLayers = [];
var allLayers = [];
//
var featureNS = 'sf';
var formatWFS = new ol.format.WFS();
var formatGML;
//
var layerSelect = document.getElementById('layer_type');

var selectPointerMove_Highlight;
var selectClick_Delete;

var popup;

var osm = new ol.layer.Tile({
    source: new ol.source.OSM()
});
allLayers.push(osm);


for (i=0; i<numOfLayers; i++){
    infrastructureVectors[i] = [];
    infrastructureLayers[i] = []; // Create an array layer for each infrastructure type
}

function getSourceVector(item) {
    var sourceVector = new ol.source.Vector({
         loader: function (extent) {
             $.ajax(item, {
                 type: 'GET',
             }).success(function(response){
                sourceVector.addFeatures(formatWFS.readFeatures(response));
             });
         },
            strategy: ol.loadingstrategy.bbox
    });

    return sourceVector;
}

var seenData = [];

var addColor = function(evt) {
    var source = evt.target;
    if (source.getState() === 'ready' && (seenData.indexOf(source) <= -1)) {

        seenData.push(source);
        var numFeatures = source.getFeatures().length;

        source.forEachFeature(function (feature) {
            if(feature.C.agency !== agency){
                style = new ol.style.Style({
                    fill: new ol.style.Fill({
                          color: 'rgba(252,232,232,0.3)',
                          weight: 1
                    }),
                    stroke: new ol.style.Stroke({
                        color: '#990000',
                        width: 2
                    }),
                    image: new ol.style.Circle({
                        radius: 5,
                        fill: new ol.style.Fill({
                            color: 'rgba(252,232,232,0.3)',
                            weight: 1
                        }),
                         stroke: new ol.style.Stroke({
                             color: '#990000',
                             width: 2
                         }),
                    })
                });
                feature.setStyle(style);
            }
        });

        return;
    }
};

for (var k in infrastructureLayerIds){
    if(agency === "GROUP_ALFRESCO_ADMINISTRATORS"){
        infrastructureVectors[k] = new ol.source.Vector({
            format: new ol.format.GeoJSON(),
            url: server_path + ':8080/geoserver/wfs?service=WFS&' + 'version=1.1.0&request=GetFeature&typename=sf:'+ infrastructureLayerIds[k] + '&' + 'outputFormat=application/json',
        });

    }else{
        var sAgencies = sharedAgencies.split(',');
        var shared = "";

        for (var i in sAgencies) {
            if(shared === ""){
                shared = "agency%20=%20%27"+sAgencies[i]+"%27";
            }else{
                shared = shared + "%20OR%20agency%20=%20%27"+sAgencies[i]+"%27";
            }
        }
        shared = shared + "%20Or%20agency%20=%20%27"+agency+"%27";

        infrastructureVectors[k] = new ol.source.Vector({
            format: new ol.format.GeoJSON(),
            url: server_path + ":8080/geoserver/wfs?service=WFS&" + "version=1.1.0&request=GetFeature&typename="+ infrastructureLayerIds[k] + "&CQL_FILTER="+shared+"&outputFormat=application/json",
        });

    }

    infrastructureLayers[k] = new ol.layer.Vector({
        source: infrastructureVectors[k]
    });

    allLayers.push(infrastructureLayers[k]);
}

for(var i=0; i<infrastructureVectors.length; i++){
infrastructureLayers[i].getSource().on('change', addColor);
}

controlMousePos = new ol.control.MousePosition({
	coordinateFormat: ol.coordinate.createStringXY(4),
});

popup = document.getElementById('popup_preview');

overlayPopup = new ol.Overlay({
	element: popup
});

var map = new ol.Map({
    layers: allLayers,
    target: 'map',
    overlays: [overlayPopup],
    controls: [controlMousePos,
               new ol.control.Zoom(),
               new ol.control.Attribution(),
            ],
    view: new ol.View({
      center: ol.proj.transform([24, 38], 'EPSG:4326', 'EPSG:3857'),
      zoom: 6
    })
});

// Popup showing the position the user clicked
popup = new ol.Overlay({
  element: document.getElementById('popup')
});

map.addOverlay(popup);


var features = new ol.Collection();

// Styling when drawing
var featureOverlay = new ol.layer.Vector({
  source: new ol.source.Vector({features: features}),
  style: new ol.style.Style({
    fill: new ol.style.Fill({
      color: 'rgba(0, 0, 0, 0.3)'
    }),
    stroke: new ol.style.Stroke({
      color: '#ff8100',
      width: 3
    }),
    image: new ol.style.Circle({
      radius: 8,
      fill: new ol.style.Fill({
        color: '#9933FF'
      })
    })
  })
});
featureOverlay.setMap(map);

var modify = new ol.interaction.Modify({
  features: features,
  deleteCondition: function(event) {
    return ol.events.condition.shiftKeyOnly(event) &&
        ol.events.condition.singleClick(event);
  }
});
map.addInteraction(modify);

var selectedLayer = '';

layerSelect.onchange = function(e) {

    var geometryType = layerSelect.value.split(",")[1];
    selectedLayer = layerSelect.value.split(",")[0];
    var agency = layerSelect.value.split(",")[2];
    var language = layerSelect.value.split(",")[3];

    var element = popup.getElement();
    $(element).popover('destroy');

    map.removeInteraction(draw);
    addInteraction();

    formatGML = new ol.format.GML({
        featureNS: featureNS,
        featureType: selectedLayer,
        srsName: 'EPSG:3857'
    });

};

var draw;

var geometryFunction = function(coordinates, geometry) {
    if(layerSelect.value.split(",")[1] === "Polygon"){

        if (!geometry) {
            geometry = new ol.geom.Polygon(null);
        } else {

            var coords = geometry.getCoordinates()[0];
            var diff = coordinates[0].length - coords.length;
            if (diff > 1) {
              coordinates[0].splice(coordinates[0].length - diff, diff - 1);
            }
        }

        geometry.setCoordinates(coordinates);
    }

    return geometry;
};

var keydown = function(evt){

    var charCode = (evt.which) ? evt.which : evt.keyCode;
    if (charCode === 27){ //esc key
        draw.set('escKey');
    }
};
document.addEventListener('keydown', keydown, false);


function addInteraction() {
    var element = popup.getElement();
    var drawing_feature;

    if(layerSelect.value.split(",")[1] === "Polygon"){
        draw = new ol.interaction.Draw({
            features: features,
            type: (layerSelect.value.split(",")[1]),
            geometryFunction: geometryFunction
        });
    }else if(layerSelect.value.split(",")[1] === "Point"){
        draw = new ol.interaction.Draw({
            features: features,
            type: (layerSelect.value.split(",")[1])
        });
    }

    map.addInteraction(draw);

    var removeLastFeature = function () {
            if (drawing_feature) {
                var geom = drawing_feature.getGeometry();
                geom.setCoordinates([0,0], [0,0]);
                popup.setPosition(undefined);
            }
    };

    draw.on('drawstart', function(evt){
        removeLastFeature();
        drawing_feature = evt.feature;
    });

    draw.on('change:escKey', function(evt){
        var geom = drawing_feature.getGeometry();

        if (geom.getType() === "Polygon") {
            geom.setCoordinates([geom.getCoordinates()[0].slice(0, -2)]);
        }else if(geom.getType() === "Point"){
            geom.setCoordinates([0,0], [0,0]);
        }

        popup.setPosition(undefined);
    });

    // when a new feature has been drawn...
    draw.on('drawend', function(evt) {

        var coords = evt.feature.getGeometry().getCoordinates();

        if(coords[0][0] === undefined)
            map.getView().setCenter(coords);
        else
            map.getView().setCenter(coords[0][0]);

        // Point
        if (layerSelect.value.split(",")[1] == 'Point'){
            $(element).popover('destroy');
            popup.setPosition(coords);
        }
        // LineString
        else if (layerSelect.value.split(",")[1] === 'LineString'){
            $(element).popover('destroy');
            popup.setPosition(coords[0]);
        }
        // Polygon
        else if (layerSelect.value.split(",")[1] === 'Polygon'){
            $(element).popover('destroy');
            popup.setPosition(coords[0][0]);
        }

        var attr = '';
        if(mapAttributes.get(selectedLayer) !== ''){
                attr = "( " + mapAttributes.get(selectedLayer).replace(/\|\|/gi, ",") + " )";
        }

        var popoverContent =
        '<div id="popover-content">' +
        '<div class="form-group">' +
        '<h6>'+ locationTitle +'</h6>' +
        '<input type="text" id="siteArea" class="form-control"><br/>' +
        '<h6>'+ attributeTitle +'</h6>' +
        '<small>' + attr + '</small>' +
        '<input type="text" id="attributesArea" class="form-control"><br/>' +
        '<button class="btn btn-warning btn-xs" id="saveGeom" data-toggle="tooltip" data-placement="top" title="Save geometry and metadata">' +
        '<span class="glyphicon glyphicon-floppy-disk"></span></button>'+
        '</div>' +
        '<div>';

        $(element).popover({
            'placement': 'top',
            'animation': false,
            'html': true,
            'content': '<small>' + popoverContent + '</small>',
        });
        $(element).popover('show');

        document.getElementById("saveGeom").onclick = function() {
            var textAreaVal="";
            var site="";
            var attributes="";
            var category="";

            if (document.getElementById("siteArea").value!=="") { site = document.getElementById("siteArea").value; }
            if (document.getElementById("attributesArea").value!=="") { attributes = document.getElementById("attributesArea").value; }

            transactWFS('insert', evt.feature, '', '', site, attributes, textAreaVal, category);
            $(element).popover('destroy');
        };
    });
}

function transactWFS(p,feature, geometry, description, site, attributes, textAreaVal, category){

    var s;
    var str;

    switch(p) {
        case 'insert': // *****************************************************************************
            var d = new Date();
            var currentDate = d.toString();
            var transformedGeom;

            if(layerSelect.value.split(",")[1]=='Point'){
                transformedGeom = new ol.geom.Point(feature.getGeometry().getCoordinates());
            } else if(layerSelect.value.split(",")[1]=='LineString'){
                transformedGeom = new ol.geom.LineString(feature.getGeometry().getCoordinates());
            } else if(layerSelect.value.split(",")[1]=='Polygon'){
                transformedGeom = new ol.geom.Polygon(feature.getGeometry().getCoordinates());
            }
            transformedGeom.transform('EPSG:3857', 'EPSG:4326');

            feature.set('geom',  transformedGeom);
            feature.set('insert_date', currentDate);
            feature.set('disaster_category', category);
            feature.set('site', site);
            feature.set('attributes', attributes);
            feature.set('agency', layerSelect.value.split(",")[2]);
            //
            node = formatWFS.writeTransaction([feature],null,null,formatGML);
            s = new XMLSerializer();
            str = s.serializeToString(node);
            break;
        case 'updatePoint': // *****************************************************************************
             db_table = feature.getId().substring(0, feature.getId().indexOf("."));
             var coordinatesUpdated = geometry.o[0] + "," + geometry.o[1];

             str =  "<wfs:Transaction service='WFS' version='1.1.0'\n" +
                        "xmlns:cdf='http://www.opengis.net/cite/data'\n" +
                        "xmlns:ogc='http://www.opengis.net/ogc'\n" +
                        "xmlns:wfs='http://www.opengis.net/wfs'\n" +
                        "xmlns:gml='http://www.opengis.net/gml'\n"+
                        "xmlns:topp='http://www.openplans.org/topp'>\n"+
                        "<wfs:Update typeName='"+ featureNS +":" + db_table + "'>\n"+
                            "<wfs:Property>\n"+
                                "<wfs:Name>geom</wfs:Name>\n"+
                                "<wfs:Value>\n"+
                                "<gml:Point srsDimension='2' srsName='EPSG:4326'>\n"+
                                "<gml:coordinates decimal='.' cs=',' ts=' '>"+coordinatesUpdated+"</gml:coordinates>\n"+
                                "</gml:Point>\n"+
                                "</wfs:Value>\n"+
                            "</wfs:Property>\n"+
                            "<ogc:Filter>\n" +
                                "<FeatureId fid='" + feature.getId() + "'/>\n"+
                            "</ogc:Filter>\n"+
                        "</wfs:Update>\n" +
                    "</wfs:Transaction>\n" ;
             break;
        case 'updatePolygon': // **********************************************************************
                     db_table = feature.getId().substring(0, feature.getId().indexOf("."));
                     coordinatesUpdated = "";

                     var cnt = 1;
                     for(var i in geometry.o){
                        // separate couples
                        if (cnt % 2){ //if odd
                            coordinatesUpdated = coordinatesUpdated + " " + geometry.o[i];
                        }
                        else {
                            coordinatesUpdated = coordinatesUpdated + "," + geometry.o[i];
                        }
                        cnt = cnt + 1;
                     }

                     coordinatesUpdated = coordinatesUpdated.substring(1);

                     str =  "<wfs:Transaction service='WFS' version='1.1.0'\n" +
                                "xmlns:cdf='http://www.opengis.net/cite/data'\n" +
                                "xmlns:ogc='http://www.opengis.net/ogc'\n" +
                                "xmlns:wfs='http://www.opengis.net/wfs'\n" +
                                "xmlns:gml='http://www.opengis.net/gml'\n"+
                                "xmlns:topp='http://www.openplans.org/topp'>\n"+
                                "<wfs:Update typeName='"+ featureNS +":" + db_table + "'>\n"+
                                    "<wfs:Property>\n"+
                                        "<wfs:Name>geom</wfs:Name>\n"+
                                        "<wfs:Value>\n"+
                                        "<gml:Polygon srsName='EPSG:4326'>\n"+
                                            "<gml:exterior>\n"+
                                                "<gml:LinearRing>\n"+
                                                    "<gml:coordinates decimal='.' cs=',' ts=' '>"+coordinatesUpdated+"</gml:coordinates>\n"+
                                                "</gml:LinearRing>\n"+
                                            "</gml:exterior>\n"+
                                        "</gml:Polygon>\n"+
                                        "</wfs:Value>\n"+
                                    "</wfs:Property>\n"+
                                    "<ogc:Filter>\n" +
                                        "<FeatureId fid='" + feature.getId() + "'/>\n"+
                                    "</ogc:Filter>\n"+
                                "</wfs:Update>\n" +
                            "</wfs:Transaction>\n" ;
                     break;
        case 'updateLine': // **********************************************************************
                             db_table = feature.getId().substring(0, feature.getId().indexOf("."));
                             coordinatesUpdated = "";

                             cnt = 1;
                             for(var k in geometry.o){
                                // separate couples
                                if (cnt % 2){ //if odd
                                    coordinatesUpdated = coordinatesUpdated + " " + geometry.o[k];
                                }
                                else {
                                    coordinatesUpdated = coordinatesUpdated + "," + geometry.o[k];
                                }
                                cnt = cnt + 1;
                             }

                             coordinatesUpdated = coordinatesUpdated.substring(1);

                             str =  "<wfs:Transaction service='WFS' version='1.1.0'\n" +
                                        "xmlns:cdf='http://www.opengis.net/cite/data'\n" +
                                        "xmlns:ogc='http://www.opengis.net/ogc'\n" +
                                        "xmlns:wfs='http://www.opengis.net/wfs'\n" +
                                        "xmlns:gml='http://www.opengis.net/gml'\n"+
                                        "xmlns:topp='http://www.openplans.org/topp'>\n"+
                                        "<wfs:Update typeName='"+ featureNS +":" + db_table + "'>\n"+
                                            "<wfs:Property>\n"+
                                                "<wfs:Name>geom</wfs:Name>\n"+
                                                "<wfs:Value>\n"+
                                                "<gml:LineString srsName='EPSG:4326'>\n"+
                                                      "<gml:coordinates decimal='.' cs=',' ts=' '>"+coordinatesUpdated+"</gml:coordinates>\n"+
                                                "</gml:LineString>\n"+
                                                "</wfs:Value>\n"+
                                            "</wfs:Property>\n"+
                                            "<ogc:Filter>\n" +
                                                "<FeatureId fid='" + feature.getId() + "'/>\n"+
                                            "</ogc:Filter>\n"+
                                        "</wfs:Update>\n" +
                                    "</wfs:Transaction>\n" ;
                        break;
        case 'updateInfo': // **********************************************************************
                     db_table = feature.getId().substring(0, feature.getId().indexOf("."));

                     str =  "<wfs:Transaction service='WFS' version='1.1.0'\n" +
                                "xmlns:cdf='http://www.opengis.net/cite/data'\n" +
                                "xmlns:ogc='http://www.opengis.net/ogc'\n" +
                                "xmlns:wfs='http://www.opengis.net/wfs'\n" +
                                "xmlns:gml='http://www.opengis.net/gml'\n"+
                                "xmlns:topp='http://www.openplans.org/topp'>\n"+
                                "<wfs:Update typeName='"+ featureNS +":" + db_table + "'>\n"+
                                    "<wfs:Property>\n"+
                                        "<wfs:Name>site</wfs:Name>\n"+
                                        "<wfs:Value>"+site+"</wfs:Value>\n"+
                                    "</wfs:Property>\n"+
                                    "<wfs:Property>\n"+
                                        "<wfs:Name>attributes</wfs:Name>\n"+
                                        "<wfs:Value>"+attributes+"</wfs:Value>\n"+
                                    "</wfs:Property>\n"+
                                    "<ogc:Filter>\n" +
                                        "<FeatureId fid='" + feature.getId() + "'/>\n"+
                                    "</ogc:Filter>\n"+
                                "</wfs:Update>\n" +
                            "</wfs:Transaction>\n" ;
                     break;
        case 'delete': // *****************************************************************************
            var db_table = feature.getId().substring(0, feature.getId().indexOf("."));
            str =
                  "<wfs:Transaction service='WFS' version='1.0.0'\n" +
                       "xmlns:cdf='http://www.opengis.net/cite/data'\n" +
                       "xmlns:ogc='http://www.opengis.net/ogc'\n" +
                         "xmlns:wfs='http://www.opengis.net/wfs'\n" +
                          "xmlns:topp='http://www.openplans.org/topp'>\n"+
                             "<wfs:Delete typeName='"+ featureNS +":" + db_table + "'>\n"+
                               "<ogc:Filter>\n" +
                                "<FeatureId fid='" + feature.getId() + "'/>\n"+
                               "</ogc:Filter>\n"+
                             "</wfs:Delete>\n" +
                      "</wfs:Transaction>\n" ;
            break;
    }

    $.ajax({
        type: "POST",
        url: geoserver_wfs,
        dataType: 'xml',
        processData: false,
        contentType: "text/xml",
        data: str,
        success: function(data) {
            location.reload();
        },
        error: function(jqXHR, textStatus, errorThrown) {
//            console.log(textStatus);
        }

    });
}

$('form').on('submit', function() {
    return false;
});

$('#btnDelete').on('click', function() {
    // 1) Lock & Disable select geometry type
    map.removeInteraction(draw);

    // Hover highlight
    selectPointerMove_Highlight = new ol.interaction.Select({
        condition: ol.events.condition.pointerMove
    });
    map.addInteraction(selectPointerMove_Highlight);
    var interaction_added = true;

    selectClick_Delete = new ol.interaction.Select({
        condition: ol.events.condition.click
        });
    map.addInteraction(selectClick_Delete);

    selectClick_Delete.on('select', function(e) {
        var collection = e.target.getFeatures();
        var feature = collection.item(0);

        if(feature.getProperties().agency === agency){
            if (feature) {
                if (feature.getId()) {
                    var db_table = feature.getId().substring(0, feature.getId().indexOf("."));
                    var index = infrastructureLayerIds.indexOf(db_table);

                   // infrastructureVectors[index].removeFeature(feature);
                    transactWFS('delete', feature);

                    map.removeInteraction(selectPointerMove_Highlight);
                    collection.clear();
                    interaction_added = false;
                }
            }
        }
    });

    map.on('pointermove', function(){
        if(interaction_added) return;
        map.addInteraction(selectPointerMove_Highlight);
        interaction_added = true;
    });
});

$('#btnSelect').on('click', function() {

      var selectFeat = new ol.interaction.Select();
      map.addInteraction(selectFeat);
      var selectedFeat = selectFeat.getFeatures();

      var element = overlayPopup.getElement();

      selectedFeat.on('add', function(e) {
          $(element).popover('destroy');
          props = e.element.getProperties();
          props.layer = e.element.ha.split(".")[0].replace(/_/g, " ").trim();
          props.layerTitle = idTitle[e.element.ha.split(".")[0]];
          props.layerTitleEn = idTitleEn[e.element.ha.split(".")[0]];

          if (!props.insert_date){ props.insert_date = 'n/a'; }
          if (!props.site){ props.site = 'n/a'; }
          if (!props.attributes){ props.attributes = 'n/a'; }
          if (!props.layer){ props.layer = 'n/a'; }
          if (!props.layerTitle){ props.layerTitle = 'n/a'; }
          if (!props.layerTitleEn){ props.layerTitleEn = 'n/a'; }

          var agencyGroupname = '';
          if (!props.agency){ agencyGroupname = 'n/a'; }
          else{
            agencyGroupname = mapGroupnameAgency.get(props.agency);
            if(agencyGroupname === undefined) agencyGroupname = 'n/a';
          }

          var title='';
          if(document.getElementsByClassName("lang")[0].defaultValue === 'en'){ title = props.layerTitleEn; }
          if(document.getElementsByClassName("lang")[0].defaultValue === 'el'){ title = props.layerTitle; }

          var data = $('.ol-mouse-position').html().split(',');
          var coords = [];
          coords[0] = parseFloat(data[0]);
          coords[1] = parseFloat(data[1]);
          transformedGeom = new ol.geom.Point(coords);
          transformedGeom.transform('EPSG:3857', 'EPSG:4326');
          var coordinatesUpdated = transformedGeom.o[0] + ", " + transformedGeom.o[1];

          if (data){$('#popup-coordinates').html(coordinatesUpdated);}else{$('#popup-coordinates').html('n/a');}

          overlayPopup.setPosition(data);

          var popoverContent =
                  '<div id="popup-closer" class="ol-popup-closer"> <i class="glyphicon glyphicon-remove"></i></div>'+
                  '<div id="popover-content" class="popover-content-preview">' +
                  '<div class="form-group">' +
                  '<small>'+
                  '<strong>Title:</strong> <span id="popup-layer">'+title+'</span><br/>'+
                  '<strong>Agency:</strong> <span id="popup-agency">'+agencyGroupname+'</span><br/>'+
                  '<strong>Date:</strong> <span id="popup-insert_date">'+props.insert_date.split("GMT+0300")[0]+'</span><br/>'+
                  '<br/>'+
                  '<strong>Location:</strong> <span id="popup-site">'+props.site+'</span><br/>'+
                  '<strong>Attributes:</strong> <span id="popup-attributes">'+props.attributes+'</span><br/>'+
                  '</small>' +
                  '</div>' +
                  '<div>';

          map.getView().setCenter(coords);

          $(element).popover({
               'placement': 'center-left',
               'animation': false,
               'html': true,
               'content': '<small>' + popoverContent + '</small>',
          });
          $(element).popover('show');

          if(props.agency != agency){
            document.getElementsByClassName('popover-content')[0].style.background = '#fff0f0';
          }else{
            document.getElementsByClassName('popover-content')[0].style.background = '#fff';
          }

          $('#popup-closer').on('click', function() {
            $(element).popover('hide');
            $(element).popover('destroy');
          });
      });
});

var dataGeometry;
var selectFeat;
var currentGeom;
var clone;

$('#infrastructureUpdate').submit(function () {
    getData();
});

function getData(){
    var siteN = document.getElementById("site").value;
    if(siteN===""){siteN = document.getElementById("site").getAttribute("placeholder");}
    var attributesN = document.getElementById("attributes").value;
    if(attributesN===""){attributesN = document.getElementById("attributes").getAttribute("placeholder");}

    transactWFS('updateInfo',clone,'','', siteN, attributesN);
}

$('#btnUpdate').on('click', function() {
      selectFeat = new ol.interaction.Select();
      map.addInteraction(selectFeat);
      var element = overlayPopup.getElement();
      var selectedFeat = selectFeat.getFeatures();

      selectedFeat.on('add', function(e) {
        if(e.element.getProperties().agency === agency){
            props = e.element.getProperties();
            props.layer = e.element.ha.split(".")[0].trim();
            if(props.site){ $('#site').attr("placeholder",props.site); }
            else{ $('#site').attr("placeholder","n/a"); }
            if(props.attributes){ $('#attributes').attr("placeholder",props.attributes); }
            else{ $('#attributes').attr("placeholder","n/a"); }

            if(mapAttributes.get(props.layer) !== ''){
                document.getElementById("attributeValue").innerHTML = "<br/>( " + mapAttributes.get(props.layer).replace(/\|\|/gi, ",") + " )";
            }

            document.getElementById("updateForm").style.display="block";
            f = e.element;

            var db_table = e.element.getId().substring(0, e.element.getId().indexOf("."));
            var index = infrastructureLayerIds.indexOf(db_table);
            currentGeom = infrastructureLayerGeomType[index];

            featureProperties = f.getProperties();
            clone = new ol.Feature(featureProperties);
            clone.setId(f.getId());

            document.getElementById("updateForm").scrollIntoView();
        }else{
            document.getElementById("updateForm").style.display="none";
        }
      });
});

$('#btnEdit').on('click', function() {
      var selectFeat = new ol.interaction.Select();
      map.addInteraction(selectFeat);
      var selectedFeat = selectFeat.getFeatures();

      var dirty = {};

      selectedFeat.on('add', function(e) {
          if(e.element.getProperties().agency === agency){

                var modify = new ol.interaction.Modify({
                  features: selectedFeat
                });
                map.addInteraction(modify);

                e.element.on('change', function(e) {
                dirty[e.target.getId()] = true;
                });
          }
      });
      selectedFeat.on('remove', function(e) {
        f = e.element;
        var db_table = e.element.getId().substring(0, e.element.getId().indexOf("."));
        var index = infrastructureLayerIds.indexOf(db_table);
        var currentGeom = infrastructureLayerGeomType[index];

        var transformedGeom;
        if(currentGeom === 'Polygon'){
            transformedGeom = new ol.geom.Polygon(f.getGeometry().getCoordinates());
        }else if(currentGeom === 'LineString'){
            transformedGeom = new ol.geom.LineString(f.getGeometry().getCoordinates());
        }else if(currentGeom === 'Point'){
            transformedGeom = new ol.geom.Point(f.getGeometry().getCoordinates());
        }
        transformedGeom.transform('EPSG:3857', 'EPSG:4326');

        if (dirty[f.getId()]){
          delete dirty[f.getId()];
          featureProperties = f.getProperties();
          var clone = new ol.Feature(featureProperties);
          clone.setId(f.getId());
          if(currentGeom === 'Polygon'){
                transactWFS('updatePolygon',clone,transformedGeom);
          }else if(currentGeom === 'LineString'){
                transactWFS('updateLine',clone,transformedGeom);
          }else if(currentGeom === 'Point'){
                transactWFS('updatePoint',clone,transformedGeom);
          }
        }
      });

});

$(".js-example-basic-single").select2({
    theme : "classic",
});

$(function () {
    $('[data-toggle="tooltip"]').tooltip();
});

$('#manageDropdown').addClass('active');