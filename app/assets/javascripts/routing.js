var map ;

var olRoute;

var points = [];

function routing() {

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

    var view = new ol.View({
//        center: ol.proj.transform([ 21.68, 40.69], 'EPSG:4326', 'EPSG:3857'),
        center: ol.proj.transform([24, 38], 'EPSG:4326', 'EPSG:3857'),
        zoom: 6
    });


     map = new ol.Map({
        layers: [osm,olRoute ],
        target: 'map',
        view: view
    });

    map.on("click", function(evt) {
         var style = new ol.style.Style({

                        image: new ol.style.Circle({
                            fill: new ol.style.Fill({
                                color: 'lime'
                            }),
                            stroke: new ol.style.Stroke({
                                width: 1,
                                color: 'blue'
                            }),
                            radius: 6
                        }),
                    });


        var coord = ol.proj.transform(evt.coordinate, 'EPSG:3857', 'EPSG:4326');
        points.push(coord);

         var f = new ol.Feature({
           geometry: new ol.geom.Point(ol.proj.transform(coord, 'EPSG:4326', 'EPSG:3857'))
         });

         f.setStyle(style);
//         console.log(f);
         olRoute.getSource().addFeature(f);

         if (points.length>1){
            $('#clearBtn').prop('disabled', false);
            $('#routingBtn').prop('disabled', false);
         }
    });

}

function getTime(time){
    // Hours, minutes and seconds
    var hrs = ~~(time / 3600);
    var mins = ~~((time % 3600) / 60);
    var secs = time % 60;

    // Output like "1:01" or "4:03:59" or "123:03:59"
    ret = "";

    if (hrs > 0)
        ret += "" + hrs + ":" + (mins < 10 ? "0" : "");

    ret += "" + mins + ":" + (secs < 10 ? "0" : "");
    ret += "" + secs;
    return ret;
}

// When clicking "Get directions" button
function handleRouting()
{
    if (points.length <2){
        alert('Please select two or more points');
        return;
    }
    else {
        $('#instructionsPanel').show();
    }

    var locations="";
    $.each(points, function(i,item)
    {
       locations += "loc="+ item[1]+","+ item[0]+"&";
    });


    // Clear points & instructions from map
    clearRouting();
    points= [];

    //console.log(locations);
    //console.log(Instructions[1]);

    var url = osrm_server + "viaroute?"+locations+"instructions=true";
    console.log(url);


    $.ajax({
        url: url,
        type: 'GET',
        crossDomain: true,
//      dataType: 'json',
        success: function(data) {

            $('#instructionsPanel').show();
            $('#clearBtn').prop('disabled', false);

            var time = getTime(data.route_summary.total_time);
            var list = "";

            if (data.route_summary.start_point && data.route_summary.end_point){
                list = list +  from +  ": " + data.route_summary.start_point + "<br/>" +  to +": " + data.route_summary.end_point + "<hr>";
            }

            list = list + "<h4>" + totalTime + ":<strong> " + time + "</strong></h4>";
            list = list + "<h4>"+ totalDistance+ ":<strong> " + data.route_summary.total_distance + "m</strong></h4>";

            list = list + "<br/><ol>";
            $.each( data.route_instructions, function( i, item ) {
                list = list + "<li>"+Instructions[parseInt(item[0])] + " " +item[1]+ "  " + item[5]+"</li>";
            });

            $("#Results").html(list);

            var format = new ol.format.Polyline({factor: 1e6});
            //console.log(data.route_geometry);
            var route = format.readGeometry(data.route_geometry, {
                dataProjection: 'EPSG:4326',
                featureProjection: 'EPSG:3857'
            });

            var sf = new ol.style.Style({
                stroke: new ol.style.Stroke({
                    width: 6,
                    color: 'red'
                })
            });

            var feature = new ol.Feature({geometry: route});
                feature.setStyle(sf);
               // feature.setId(id);
                olRoute.getSource().addFeature(feature);
            },
            error: function() {
              alert('Error');
            }
    });
}

// When clicking "Clear" button
function clearRouting()
{
    $('#clearBtn').prop('disabled', true);
    $('#routingBtn').prop('disabled', true);
    document.getElementById("instructionsPanel").style.display = 'none';

    $("#Results").html("");
    olRoute.getSource().clear();
}