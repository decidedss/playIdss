var map ;

var olRoute;
var point;
var points = [];
var eventLocation;

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

    var view = new ol.View({
//        center: [0, 0],
        center: ol.proj.transform([24, 38], 'EPSG:4326', 'EPSG:3857'),
        zoom: 6
    });

    var features = [];


        var iconFeature = new ol.Feature({
            geometry: new ol.geom.Point([23.71, 37.97]),
            name: 'test',
            population: 4000,
            rainfall: 500
        });

        var iconStyle = new ol.style.Style({
          image: new ol.style.Icon( ({
            anchor: [0.5, 46],
            anchorXUnits: 'fraction',
            anchorYUnits: 'pixels',
            opacity: 0.75,
            src: 'https://cdn2.iconfinder.com/data/icons/despicable-me-2-minions/128/despicable-me-2-Minion-icon-7.png'
          }))
        });

        iconFeature.setStyle(iconStyle);

        features.push(iconFeature);


    var vectorSource = new ol.source.Vector({
          features: features
    });

    var vectorLayer = new ol.layer.Vector({
          source: vectorSource
    });

    map = new ol.Map({
            layers: [osm, olRoute, vectorLayer],
            target: 'map',
            view: view
    });

    var element = document.getElementById('popup');

    var popup = new ol.Overlay({
      element: element,
      positioning: 'bottom-center',
      stopEvent: false
    });
    map.addOverlay(popup);

    // Add Geolocation
    //addGeolocation();

    map.on('click', function(evt) {
        var feature = map.forEachFeatureAtPixel(evt.pixel,
          function(feature, layer) {
            return feature;
          });
        if (feature) {
            popup.setPosition(evt.coordinate);
            $(element).popover({
              'placement': 'top',
              'html': true,
              'content': feature.get('name')
            });
            var data = [];
            var labels = [];

            $.ajax( {

                url : "/forecast/get/" + "THESSALONIKI KENTRO-DIMOS",
                success :(function(data){
                console.log("THESSALONIKI KENTRO-DIMOS");
                  //  alert(data)
                }),
                error: (function(result){
                    console.log(result);
                    })
            });

//            @if(session.get("location") != null){
//                        @for((key, value) <- precipitation.get(session.get("location"))){
//                            labels.push(@key);
//                            data.push(@value);
//                  }
//            }

            var lineData = {
            labels: labels,
                datasets: [
                {
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: data
                }
            ]};

            var ctx = document.getElementById("myChart").getContext("2d");
            var myLineChart = new Chart(ctx).Line(lineData, {
                responsive: true
            });
            }
         });

    var cursorHoverStyle = "pointer";
    var target = map.getTarget();
    var jTarget = typeof target === "string" ? $("#"+target) : $(target);

    map.on("pointermove", function (event) {
        var mouseCoordInMapPixels = [event.originalEvent.offsetX, event.originalEvent.offsetY];

        //detect feature at mouse coords
        var hit = map.forEachFeatureAtPixel(mouseCoordInMapPixels, function (feature, layer) {
            return true;
        });

        if (hit) {
            jTarget.css("cursor", cursorHoverStyle);
        } else {
            jTarget.css("cursor", "");
        }
    });

}