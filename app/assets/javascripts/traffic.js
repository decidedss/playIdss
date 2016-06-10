var map ;

function traffic() {

//    var source = new ol.source.OSM({
//             layers: 'basic'
//    });

    var osm = new ol.layer.Tile({
     source: new ol.source.OSM(),
     name: "osm"
    });

    var view = new ol.View({
        center: ol.proj.transform([22.9, 40.6], 'EPSG:4326', 'EPSG:3857'), // Thessaloniki
        zoom: 11
    });

     var high = new ol.style.Style({
            stroke: new ol.style.Stroke({
                width: 5,
                color: 'red'
            })
    });

    var low = new ol.style.Style({
                stroke: new ol.style.Stroke({
                    width: 5,
                    color: '#00c864'
                })
    });

    var medium = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        width: 5,
                        color: '#ffa500'
                    })
    });

    var allLayers = [];
    var trafficVectors = [];
    var trafficLayers = [];

    allLayers.push(osm);

    var lineString;

    $.ajax( {
            url : "/traffic/getCongestion",
            success :(function(congestions){

            $.ajax( {
                url : "/traffic/getCoordinates",
                success :(function(result){
                for (k=0; k<result.length; k++){

                    trafficVectors[k] = [];
                    trafficLayers[k] = [];
                }

                var i = 0;
                Object.keys(result).forEach(function(key) {
                   lineString = new ol.geom.LineString(result[key]);

                    var feature = new ol.Feature({
                        geometry: lineString
                    });

                    feature.getGeometry().transform('EPSG:4326', 'EPSG:3857');

                    if(congestions[key] === 'Low')
                        feature.setStyle(low);
                    else if(congestions[key] === 'Medium')
                        feature.setStyle(medium);
                    else
                        feature.setStyle(high);

                    trafficVectors[i] = new ol.source.Vector({
                        features: [feature]
                    });

                    trafficLayers[i] = new ol.layer.Vector({
                        source: trafficVectors[i]
                    });

                    allLayers.push(trafficLayers[i]);
                    i = i + 1;
                });

                map = new ol.Map({
                    layers: allLayers,
                    target: 'map',
                    view: view
                });
            })
        });
    })
});
}

$('#spinner').addClass('spin');

function stopSpinner() {
    $('#loading').addClass('hide');
    $('#loading').one('webkitTransitionEnd', function() {
        $('#loading').hide();
    });
}

$(document).ajaxStop(function () {
    stopSpinner();
});

setTimeout(stopSpinner, 3000);