var map ;

var olRoute;
var point;
var points = [];
var eventLocation;
var lat;
var lon;


function drawMap() {
    // Base Layers
    var source = new ol.source.OSM({
         layers: 'basic'
    });

    var osm = new ol.layer.Tile({
     source: source, //new ol.source.OSM(),
     name: "osm"
     });


    olRoute  = new ol.layer.Vector({
        source: new ol.source.Vector()
    });

    controlMousePos = new ol.control.MousePosition({
        coordinateFormat: ol.coordinate.createStringXY(4),
    });

    var view = new ol.View({
        //center: ol.proj.transform([ 21.67, 40.68], 'EPSG:4326', 'EPSG:3857'),
        center: ol.proj.transform([24, 38], 'EPSG:4326', 'EPSG:3857'),
        zoom: 6
    });

     map = new ol.Map({
        layers: [osm, olRoute],
        target: 'map',
        controls: [controlMousePos,
                   new ol.control.Zoom(),
                   new ol.control.Attribution(),
                ],
        view: view
    });

    // Add Geolocation
    //addGeolocation();

    map.on("click", function(evt) {
         var style = new ol.style.Style({
            image: new ol.style.Circle({
                fill: new ol.style.Fill({
                    color: 'lime'
                }),
                stroke: new ol.style.Stroke({
                    width: 1,
                    color: 'black'
                }),
                radius: 7
            }),
        });


        var coord = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
        olRoute.getSource().clear();
        points.push(coord);
        point = coord;

         var f = new ol.Feature({
           geometry: new ol.geom.Point(ol.proj.transform(coord, 'EPSG:4326', 'EPSG:3857'))
         });

         f.setStyle(style);
         olRoute.getSource().addFeature(f);

         var coord3857 = evt.coordinate;
         var coord4326 = ol.proj.transform(coord3857, 'EPSG:3857', 'EPSG:4326');
          eventLocation=coord4326;

         var lonlat = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
         lon = lonlat[0].toString();
         lat = lonlat[1].toString();
    });

}


