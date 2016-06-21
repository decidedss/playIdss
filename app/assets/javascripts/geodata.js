// Tried to follow but not possible
// http://jmfitton.com/openlayers-3-layer-tree/

var map;
var popup;
var infoFormat = 'application/json' ;
var format = new ol.format.GeoJSON({ featureType: 'Feature'});
var url = geoserver_wms;
var featurePrefix = 'sf';
var layerGroups = [];
var allLayerGroups = [];

function geodata() {

    view = new ol.View({
        center: ol.proj.transform([24, 38], 'EPSG:4326', 'EPSG:3857'),
        zoom: 6
    });

    var baseGroup =  new ol.layer.Group({
           title: 'Base maps',
           layers: [
                new ol.layer.Tile({
                    title: 'OSM',
                    name: 'OSM',
                    type: 'base',
                    visible: true,
                    source: new ol.source.OSM()
                }),
                new ol.layer.Tile({
                   title: 'Water color',
                   type: 'base',
                   visible: false,
                   source: new ol.source.Stamen({
                       layer: 'watercolor'
                   })
                }),
                new ol.layer.Tile({
                    title: 'MapQuest Satellite',
                    type: 'base',
                    visible: false,
                    source: new ol.source.MapQuest({
                        layer: 'sat'
                    })
                }),
           ]
       });

     allLayerGroups.push(baseGroup);

    // Automatically create the structure of the rest of groups
     for (var g in groups) {

        var group = new ol.layer.Group({
            title: groups[g],
            layers: [
            ]
        });
        layerGroups.push(group);
     }

      // Fill in the content of the layers
     for (var i = 0 ; i < data.length; i++) {
            for (var d in data[i]) {
                var values = data[i][d].split("#", 2);
                var tablename = values[0];
                var layertitle = values[1];

                var visibility = false;

                layerGroups[i].getLayers().push(
                    new ol.layer.Tile({
                        title: layertitle,
                        visible: visibility,
                        source: new ol.source.TileWMS({
                            url: geoserver_wms,
                            params: {'LAYERS': featurePrefix + ':' + tablename, 'TILED': true},
                            serverType: 'geoserver',
                        }),
                        id: tablename
                    })
                );
            }
     }

    // RASTER LAYER
//    var rasterSource = new ol.source.TileWMS({
//      url: geoserver_wms,
//      params: {'LAYERS': 'cite' + ':' + 'xrhseis_teliko', 'TILED': true},
//      serverType: 'geoserver',
//    });
//
//      var rasterLayer = new ol.layer.Tile({
//            title: 'Xrhseis ghs',
//            visible: true,
//            source: rasterSource,
//            id: 'xrhseis ghs'
//        });
    ////////////////////////////////////////////////////////

    allLayerGroups = allLayerGroups.concat(layerGroups);
//    allLayerGroups = allLayerGroups.concat(rasterLayer);

    controlMousePos = new ol.control.MousePosition({
        coordinateFormat: ol.coordinate.createStringXY(4),
    });


    map = new ol.Map({
       target: 'map',
       layers: allLayerGroups,
       controls: [controlMousePos,
                  new ol.control.Zoom(),
                  new ol.control.Attribution(),
               ],
       view: view
    });


    // RASTER on click
//    map.on('singleclick', function(evt) {
//        var viewResolution = /** @type {number} */ (view.getResolution());
//        var url = rasterSource.getGetFeatureInfoUrl(
//            evt.coordinate, viewResolution, 'EPSG:3857', {'INFO_FORMAT': infoFormat});
//       if (url) {
//               $.ajax({
//                    url: url,
//                    type: 'GET',
//                    crossDomain: true,
//                    dataType: 'json',
//                    success: function(data){
//                        parseResponse(data);
//                      },
//                      error: function(data){
//                        alert("No data!");
//                      }
//                });
//            }
//      });


    // Popup information
    map.on('click', function (evt1) {

        coordinate = evt1.coordinate;

        var viewResolution = (view.getResolution());
        var url = '';

         // Parse Groups
         layerGroups.forEach(function (lyr) {
            var x = lyr.getLayers();
                x.forEach(function (layer, i, x) {
                if (layer.getVisible() && layer.get('name')!='OSM') {

                // Attempt to find a marker from the planningAppsLayer
                var feature = map.forEachFeatureAtPixel(evt1.pixel, function(feature, layer) {
                    return feature;
                });
                 url = layer.getSource().getGetFeatureInfoUrl(evt1.coordinate, viewResolution, 'EPSG:3857', {
                        'INFO_FORMAT': infoFormat
                    });
                    if (url) {
                       $.ajax({
                            url: url,
                            type: 'GET',
                            crossDomain: true,
                            dataType: 'json',
                            success: function(data){
                                parseResponse(data);
                              },
                              error: function(data){
                                alert("No data!");
                              }
                        });
                    }
                }
             });
         });
    });

    // Popup showing the position the user clicked
    popup = new ol.Overlay({
      element: document.getElementById('popup')
    });
    map.addOverlay(popup);

    // Layer tree
    map.getLayers().forEach(function(layer, i) {
        bindInputs('#layer' + i, layer);
        if (layer instanceof ol.layer.Group) {
            layer.getLayers().forEach(function(sublayer, j) {
                bindInputs('#layer' + i + j, sublayer);
            });
        }
    });

    $('#layertree li > span').click(function() {
      $(this).siblings('fieldset').toggle();
    }).siblings('fieldset').hide();
}


function parseResponse(data) {
    var content = "";
    var hasContent = false;
    var features = format.readFeatures(data);
    var element = popup.getElement();

    if (features.length >= 1 && features[0]) {
        $(element).popover('destroy');
        popup.setPosition(coordinate);
        var feature = features[0];
        var description = "";
        var values = feature.getProperties();

        for (var key in values) {
            hasContent = true;
            if (key !== 'geometry' ){
                if (key == 'agency'){
                    if(mapGroupnameAgency.get(values[key]) !== null){
                            content = content  + "<strong>"+key +  ":</strong> " + mapGroupnameAgency.get(values[key]) + "<br/>";
                    }else{
                            content = content  + "<strong>"+key +  ":</strong> - <br/>";
                    }
                }
                else {
                   if(values[key] !== null){
                     content = content  + "<strong>"+key +  ":</strong> " + values[key] + "<br/>";
                   }else{
                     content = content  + "<strong>"+key +  ":</strong> - <br/>";
                     content = content  + "<strong>"+key +  ":</strong> - <br/>";
                   }

                }
            }
        }

        if (hasContent === true) {
            var dataCoords = $('.ol-mouse-position').html().split(',');
            var coords = [];
            coords[0] = parseFloat(dataCoords[0]);
            coords[1] = parseFloat(dataCoords[1]);

            map.getView().setCenter(coords);

            $(element).popover({
                'placement': 'right',
                'animation': false,
                'html': true,
                'content': '<small>' + content + '</small>',
            });
            $(element).popover('show');
        }
    } else {
        $(element).popover('destroy');
    }
}

function bindInputs(layerid, layer) {
    var visibilityInput = $(layerid + ' input.visible');

    visibilityInput.on('change', function() {
        layer.setVisible(this.checked);
    });
    visibilityInput.prop('checked', layer.getVisible());

    var opacityInput = $(layerid + ' input.opacity');
        opacityInput.on('input change', function() {
        layer.setOpacity(parseFloat(this.value));
    });
    opacityInput.val(String(layer.getOpacity()));
}