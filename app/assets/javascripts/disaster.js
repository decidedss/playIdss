// With WMS tiles you cannot set a custom icon (like a police car)
// This is why I use points from DB to depict them like feature icons

var map;
var eventFeatures = [];
var eventSources = [];
var eventLayers = [];
var eventStyles = [];
var allLayers = [];
var osm;
var view;
//var infoFormat = 'text/javascript' ;
//var format = new ol.format.GeoJSON({ featureType: 'Feature'});
var coordinate;
var popup;

function disasterMap() {

    // Base Layers
    var source = new ol.source.OSM({
         layers: 'basic'
    });

    osm = new ol.layer.Tile({
     source: source,
     name: "osm"
    });

    view = new ol.View({
        center: ol.proj.transform([24, 38], 'EPSG:4326', 'EPSG:3857'),
        zoom: 6
    });

    /////////// <all styles> ///////////////

    // initialize distinct types of event layers
    var type_mapping = [];
    for (i=0; i<numOfEventTypes; i++){
        eventLayers[i] = []; // Create an array layer for each event type
        eventSources[i] = [];
        eventFeatures[i] = [];
        eventStyles[i] = [];
        type_mapping[i] = event_types_code[i];
    }


    for (i=0; i<numOfEventTypes; i++){

        eventStyles[i] = new ol.style.Style({
                             image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                             anchorXUnits: 'fraction',
                             anchorYUnits: 'pixels',
                             src: icons[i]
                           }))
                        });
    }
    /////////// </all styles> ///////////////


    // Generate features grouped by event type
    for (i=0; i<numOfEvents; i++){
        for (t=0; t<numOfEventTypes; t++){
            if (types[i] === event_types_code[t] ) {
               var eventFeature = new ol.Feature({
                  geometry: new ol.geom.Point(ol.proj.transform([longitude[i], latitude[i]], 'EPSG:4326', 'EPSG:3857')),
                  id: types[i],
                  description: description[i],
                  image: image[i],
                  username: username[i],
                  agency: agency[i],
                  insert_date: insert_date[i],
                  isOwn: isOwn[i],
                  ids: ids[i],
                  disaster_details: disaster_details[i],
                  disaster_attributes: disaster_attributes[i],
              });
              eventFeatures[t].push(eventFeature);
            }
        }
    }

    // Generate layers grouped by event type
    for (t=0; t<numOfEventTypes; t++){
        eventSources[t] = new ol.source.Vector({
            features: eventFeatures[t] //add an array of features
        });
       eventLayers[t] = new ol.layer.Vector({
           source: eventSources[t],
           style: eventStyles[t],
       });
     }


    // Base is OSM
    allLayers.push(osm);
//    allLayers.push(new ol.layer.Group({
//       'title': 'Base maps',
//       layers: [
//
//            new ol.layer.Tile({
//                title: 'Water color',
//                type: 'base',
//                visible: false,
//                source: new ol.source.Stamen({
//                    layer: 'watercolor'
//                })
//            }),
//           new ol.layer.Tile({
//               title: 'Aerial Bing Maps',
//               type: 'base',
//               visible: false,
//               source: new ol.source.BingMaps({
//                  key: 'Ahz67mIhVlpI_zEGTXowrmR2yrZHHZXSUgxlgAVAxbVZfv_ydd-aSt3_zbn2_kr2',
//                  imagerySet: 'AerialWithLabels'
//               })
//           }),
//          new ol.layer.Tile({
//              title: 'OSM',
//              type: 'base',
//              visible: true,
//              source: new ol.source.OSM()
//          }),
//       ]
//   }));

    for (i=0; i<numOfEventTypes; i++){
       allLayers = allLayers.concat(eventLayers[i]);
    }

    controlMousePos = new ol.control.MousePosition({
            coordinateFormat: ol.coordinate.createStringXY(4),
    });

    map = new ol.Map({
        layers: allLayers,
        target: 'map',
        controls: [controlMousePos,
              new ol.control.Zoom(),
              new ol.control.Attribution(),
        ],
        view: view
    });


//    var layerSwitcher = new ol.control.LayerSwitcher();
//    map.addControl(layerSwitcher);

    // Add Geolocation
    //addGeolocation();


    // Popup showing the position the user clicked
    popup = new ol.Overlay({
      element: document.getElementById('popup')
    });
    map.addOverlay(popup);


    map.on('click', function(evt) {

        var feature = map.forEachFeatureAtPixel(evt.pixel, function(feature, layer) {
            return feature;
        });

        var element = popup.getElement();
        if (feature){

            var data = $('.ol-mouse-position').html().split(',');
            var coords = [];
            coords[0] = parseFloat(data[0]);
            coords[1] = parseFloat(data[1]);

            map.getView().setCenter(coords);

            // console.log("there is a feature");
            $(element).popover('destroy');
            popup.setPosition(evt.coordinate);
            var lonlat = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
            var lon = lonlat[0].toString().substring(0,8);
            var lat = lonlat[1].toString().substring(0,8);
            var image = "";
            if (feature.get('image')){
                   // image = '<a href="'+ base_url + 'getImage?id='  + feature.get('image') +'" target="_blank"><img src="' + base_url + 'getImage?id='  + feature.get('image') + '" width=300px height=200px"></a>';
                   image = '<a href="'+ feature.get('image') +'" target="_blank"><img src="' +  feature.get('image') + '" width=300px height=200px"></a>';
            }
            var descr = (feature.get('description') == 'empty')? '' : feature.get('description');
            var username =  feature.get('username');
            var agency =  feature.get('agency');
            var insert_date =  feature.get('insert_date');
            var isOwn =  feature.get('isOwn');
            var disaster_id = feature.get('ids');
            var disaster_details = feature.get('disaster_details');
            var detailsArray =  disaster_details.split('#');
            var disaster_attributes = feature.get('disaster_attributes');
            //
            var editBtn = '';
            var deleteBtn = '';
            if (isOwn === 'true' && isAdmin === 'false'){
                deleteBtn = '<button class="btn btn-xs btn-danger" title="Delete" id="deleteDisaster" onclick="deleteDisaster('+ disaster_id + ')"><span class="glyphicon glyphicon-trash"></span></button>';
                editBtn  = '<br/><i><strong><button type="button" class="btn btn-link" id="editBtn">' + editBtnMsg + '</button></strong></i>';
            }
            //
            $(element).popover({
                'placement': 'top',
                'animation': false,
                'html': true,
                'content': '<p><h4><font color="green">'  + event_types[feature.get('id')] +  '</font></h4>'  +
                 ' Lat, Lon = <code>' +  lat + ', ' + lon + '</code><br>' + image + '<br/><small>' + disasterComments +': ' +  descr + '</small><br>' + deleteBtn  +
                  '<br/><small><i>' + reportedBy + ' <strong>' + username + '</strong> ('+ agency + ')<br/>@ ' + insert_date +  '</i></small>' + editBtn + '</p>' +
                  '<small><strong>' + dateM + ': </strong>' + detailsArray[0] +
                  '<br/><strong>'+ durationM +': </strong>' + detailsArray[1] +
                  '<br/><strong> ' + areaM + ': </strong>' + detailsArray[2] +
                  '<br/><strong> ' + causeM + ': </strong>' + detailsArray[3] +
                  '<br/><strong>' + characteristicsM +': </strong>' + detailsArray[4] +
                  '<br/><strong>' + injuriesM +': </strong>' + detailsArray[5] +
                  '<br/><strong>' + deathsM +': </strong>' + detailsArray[6] +
                  '<br/><strong>' + impactsM +': </strong>' + detailsArray[7] +
                  '<br/><strong>' + otherM +': </strong>' + detailsArray[8] +
                  '<br/><strong>' + meansM +': </strong>' + detailsArray[9] +
                  '<br/><strong>' + actionListM +': </strong>' + detailsArray[10] +
                  '<br/><strong>' + remarksM +': </strong>' + detailsArray[11] +
                  '<br/><strong>' + suggestionsM +': </strong>' + detailsArray[12] +
                  '</small>',
            });
            $(element).popover('show');
        }
        else {
          $(element).popover('destroy');
        }

        // Edit disaster details button
        $('#editBtn').click(function(){

            $("#id").val(disaster_id);
            //
            $("#disaster_date").val(detailsArray[0]);
            $("#disaster_duration").val(detailsArray[1]);
            $("#disaster_area").val(detailsArray[2]);
            $("#disaster_cause").val(detailsArray[3]);
            $("#disaster_characteristics").val(detailsArray[4]);
            $("#disaster_injuries").val(detailsArray[5]);
            $("#disaster_deaths").val(detailsArray[6]);
            $("#disaster_impacts_infrastructure").val(detailsArray[7]);
            $("#disaster_impacts_other").val(detailsArray[8]);
            $("#disaster_means_forces").val(detailsArray[9]);
            $("#disaster_action_list").val(detailsArray[10]);
            $("#disaster_remarks").val(detailsArray[11]);
            $("#disaster_suggestions").val(detailsArray[12]);
            //

            disaster_attributes = disaster_attributes.replace(/&lt;strong&gt;/g, '<strong>');
            disaster_attributes = disaster_attributes.replace(/&lt;\/strong&gt;/g, '</strong>');
            disaster_attributes = disaster_attributes.replace(/null/g, '');

            var attrArray =  disaster_attributes.split('#');
            $("#attributeValue_disaster_duration").html(attrArray[0]);
            $("#attributeValue_disaster_area").html(attrArray[1]);
            $("#attributeValue_disaster_cause").html(attrArray[2]);
            $("#attributeValue_disaster_characteristics").html(attrArray[3]);
            $("#attributeValue_disaster_impacts_infrastructure").html(attrArray[4]);
            $("#attributeValue_disaster_impacts_other").html(attrArray[5]);
            //
            $('#editForm').show();
            $("#editForm")[0].scrollIntoView();
        });

    });


    // Initially set visibility to false for all layers
    for (i=1; i<allLayers.length; i++){ // osm should stay visible
        allLayers[i].setVisible(true);
    }
    $('input[type=checkbox]').on('change', function() {
        // event layers
        var layer = eventLayers[type_mapping.indexOf($(this).attr('id'))];
        layer.setVisible(!layer.getVisible());
    });

} // END of function eventMap

var checkflag = "true";
 function toggleAllLayers() {
     if (checkflag == "false") {
         $(".layerCheckbox").prop('checked', true);
         checkflag = "true";
         //
         for (i=1; i<allLayers.length; i++){ // start counting from 1, osm should stay visible
             allLayers[i].setVisible(true);
         }
         //
         $('#eventButton').val('Uncheck All');
       } else {
         $(".layerCheckbox").prop('checked', false);
         //
         for (i=1; i<allLayers.length; i++){ // start counting from 1, osm should stay visible
             allLayers[i].setVisible(false);
         }
         //
         checkflag = "false";
         $('#eventButton').val('Check All');
       }
 }


function el(id) {
  return document.getElementById(id);
}


function deleteDisaster(id) {
    $("#myModalDelete").modal();
    $('.modal-body').html(deleteDisasterBody);

    $('#markBtnDel').click(function(){
        $.ajax( {
            url : "/events/delete/" + id ,
            success :(function(result){
                location.reload();
            })
        });
     });
}