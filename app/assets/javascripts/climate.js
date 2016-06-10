var map ;
var StyleMap;

function drawMapCurrent() {
    var map ;
    var olRoute;
    var point;
    var points = [];
    var eventLocation;

    // Base Layers
    var source = new ol.source.OSM({
             layers: 'basic'
    });

    var osm = new ol.layer.Tile({
     source: source,
     name: "osm"
     });

    olRoute  = new ol.layer.Vector({
        source: new ol.source.Vector()
    });

    var thessaloniki = [22.9308600, 40.6436200];
    var thessalonikiMercator = ol.proj.fromLonLat(thessaloniki);

    var view = new ol.View({
        center: thessalonikiMercator,
        zoom: 7
    });

    var features = [];


     $.ajax( {
                url : "/climate/getCoordinatesCurrentHistoric",
                success :(function(result){

                  for(var place in result){
                      for(var y in result[place]){
                         var iconFeature = new ol.Feature({
                                    geometry: new ol.geom.Point(ol.proj.transform([parseFloat(result[place][y]), parseFloat(y)], 'EPSG:4326', 'EPSG:3857')),
                                    name: place.split("_")[0],
                                    title: place.split("_")[1]

                                });

                                var iconStyle = new ol.style.Style({
                                  image: new ol.style.Icon( ({
                                    anchor: [0.5, 46],
                                    anchorXUnits: 'fraction',
                                    anchorYUnits: 'pixels',
                                    opacity: 0.75,
                                    src: 'http://www.myiconfinder.com/uploads/iconsets/32-32-5f9f834af5818fa985de0f1ed06ee2a5-pin.png'
                                  }))
                                });

                                iconFeature.setStyle(iconStyle);
                                features.push(iconFeature);
                      }
                  }

                  var vectorSource = new ol.source.Vector({
          features: features
    });

    var vectorLayer = new ol.layer.Vector({
          source: vectorSource
    });

    map = new ol.Map({
            layers: [osm, olRoute, vectorLayer],
            target: 'mapLast2days',
            view: view
    });

    var element = document.getElementById('popupLast2days');

    var popup = new ol.Overlay({
      element: element,
      positioning: 'bottom-center',
      stopEvent: false
    });
    map.addOverlay(popup);

    document.getElementById("chartsResultsLast2days").style.visibility = "hidden";

    map.on('click', function(evt) {
        var feature = map.forEachFeatureAtPixel(evt.pixel,
          function(feature, layer) {
            return feature;
          });
        if (feature) {
            document.getElementById("chartsResultsLast2days").style.visibility = "visible";

            popup.setPosition(evt.coordinate);
            $(element).popover({
              'placement': 'top',
              'html': true,
              'content': feature.get('name')
            });

            $(".head h4").html(feature.get('title'));

            Chart.types.Line.extend({
                name: "LineAlt",
                draw: function () {
                    Chart.types.Line.prototype.draw.apply(this, arguments);

                    var ctx = this.chart.ctx;
                    ctx.save();
                    // text alignment and color
                    ctx.textAlign = "center";
                    ctx.textBaseline = "bottom";
                    ctx.fillStyle = this.options.scaleFontColor;
                    // position
                    var x = this.scale.xScalePaddingLeft * 0.4;
                    var y = this.chart.height / 2;
                    // change origin
                    ctx.translate(x, y);
                    // rotate text
                    ctx.rotate(-90 * Math.PI / 180);
                    ctx.fillText(this.datasets[0].label, 0, 0);
                    ctx.restore();
                }
            });

//            <!---------TEMPERATURE--------->
            var dataTemp = [];
            var labelsTemp = [];

            $.ajax( {
                url : "/climate/getTempCurrent/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsTemp.push(x);
                        dataTemp.push(result[x]);
                  }
                  var lineData = {
                  labels: labelsTemp,
                  datasets: [
                  {
                    label: "Celsius (°C)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataTemp
                  }
                ]};

                var ctx = document.getElementById("myChartCurrentTemp").getContext("2d");
                new Chart(ctx).LineAlt(lineData, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                    tooltipEvents: [],
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END TEMPERATURE-------->

//          <!---------RAIN--------->
            var dataRain = [];
            var labelsRain = [];

            $.ajax( {
                url : "/climate/getRainCurrent/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsRain.push(x);
                        dataRain.push(result[x]);
                  }
                  var lineData = {
                  labels: labelsRain,
                  datasets: [
                  {
                    label: "Millimeter (mm)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataRain
                  }
                ]};

                var ctx = document.getElementById("myChartCurrentRain").getContext("2d");
                new Chart(ctx).LineAlt(lineData, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                     tooltipEvents: [],
                     scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//          <!-------RAIN-------->

//          <!---------WIND SPEED--------->
            var dataWind = [];
            var labelsWind = [];

            $.ajax( {
                url : "/climate/getWindSpeedCurrent/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsWind.push(x);
                        dataWind.push(result[x]);
                  }

                  var lineData = {
                  labels: labelsWind,
                  datasets: [
                  {
                    label: "km/h",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataWind
                  }
                ]};

                var ctx = document.getElementById("myChartCurrentWindSpeed").getContext("2d");
                new Chart(ctx).LineAlt(lineData, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                     tooltipEvents: [],
                     scaleLabel: "          <%=value%>"
                });

                document.getElementById("myChartCurrentTemp").style.visibility = "visible";
                document.getElementById("myChartCurrentRain").style.visibility = "visible";
                document.getElementById("myChartCurrentWindSpeed").style.visibility = "visible";

                $(".head-last2days h3").hide();
                $(".children h3").show();

                if(dataWind.length === 0){
                    $(".head-last2days h3").show();
                    $(".children h3").hide();

                    document.getElementById("myChartCurrentTemp").style.visibility = "hidden";
                    document.getElementById("myChartCurrentRain").style.visibility = "hidden";
                    document.getElementById("myChartCurrentWindSpeed").style.visibility = "hidden";
                }

                }),
                error: (function(result){
                    console.log(result);
                    })

            });
//          <!-------WIND SPEED-------->
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
                  }),
                error: (function(result){
                    console.log(result);
                    })
            });
}

function drawMapForecast() {
    var map ;
    var olRoute;
    var point;
    var points = [];
    var eventLocation;

    // Base Layers
    var source = new ol.source.OSM({
             layers: 'basic'
    });

    var osm = new ol.layer.Tile({
     source: source,
     name: "osm"
     });

    olRoute  = new ol.layer.Vector({
        source: new ol.source.Vector()
    });

    var thessaloniki = [22.9308600, 40.6436200];
    var thessalonikiMercator = ol.proj.fromLonLat(thessaloniki);

    var view = new ol.View({
        center: thessalonikiMercator,
        zoom: 7
    });

    var features = [];
    var iconFeature;
    var iconStyle;

     $.ajax( {
                url : "/climate/getCoordinatesForecast",
                success :(function(result){

                  for(var place in result){
                      for(var y in result[place]){

                        var iconFeature = new ol.Feature({
                            geometry: new ol.geom.Point(ol.proj.transform([parseFloat(result[place][y]), parseFloat(y)], 'EPSG:4326', 'EPSG:3857')),
                            name: place.split("_")[0],
                            title: place.split("_")[1]
                        });

                        var iconStyle = new ol.style.Style({
                          image: new ol.style.Icon( ({
                            anchor: [0.5, 46],
                            anchorXUnits: 'fraction',
                            anchorYUnits: 'pixels',
                            opacity: 0.75,
                            src: 'http://www.myiconfinder.com/uploads/iconsets/32-32-5f9f834af5818fa985de0f1ed06ee2a5-pin.png'
                          }))
                        });

                        iconFeature.setStyle(iconStyle);
                        features.push(iconFeature);
                      }
                  }

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

                var element = document.getElementById('popupForecast');

                var popup = new ol.Overlay({
                  element: element,
                  positioning: 'bottom-center',
                  stopEvent: false
                });
                map.addOverlay(popup);

                document.getElementById("chartsResults").style.visibility = "hidden";

                map.on('click', function(evt) {
            var feature = map.forEachFeatureAtPixel(evt.pixel,
              function(feature, layer) {
                return feature;
              });
            if (feature) {
            document.getElementById("chartsResults").style.visibility = "visible";

            popup.setPosition(evt.coordinate);
            $(element).popover({
              'placement': 'top',
              'html': true,
              'content': feature.get('name')
            });

            $(".head h4").html(feature.get('title'));

            Chart.types.Line.extend({
                name: "LineAlt",
                draw: function () {
                    Chart.types.Line.prototype.draw.apply(this, arguments);

                    var ctx = this.chart.ctx;
                    ctx.save();
                    // text alignment and color
                    ctx.textAlign = "center";
                    ctx.textBaseline = "bottom";
                    ctx.fillStyle = this.options.scaleFontColor;
                    // position
                    var x = this.scale.xScalePaddingLeft * 0.4;
                    var y = this.chart.height / 2;
                    // change origin
                    ctx.translate(x, y);
                    // rotate text
                    ctx.rotate(-90 * Math.PI / 180);
                    ctx.fillText(this.datasets[0].label, 0, 0);
                    ctx.restore();
                }
            });

//            <!---------CAPACITATION--------->
            var data = [];
            var labels = [];

            $.ajax( {
                url : "/climate/getCapacitation/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labels.push(x);
                        data.push(result[x]);

                  }
                  var lineData = {
                  labels: labels,
                  datasets: [
                  {
                    label: "Percentage (%)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: data
                  }
                ]};

                var ctx = document.getElementById("myChartCapacitation").getContext("2d");
                var myLineChart = new Chart(ctx).LineAlt(lineData, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                    tooltipEvents: [],
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END CAPACITATION-------->

//            <!---------PRECIPITATION--------->
            var dataPrec = [];
            var labelsPrec = [];

            $.ajax( {
                url : "/climate/getPrecipitation/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsPrec.push(x);
                        dataPrec.push(parseFloat(result[x]) * 1000);

                  }
                  var lineDataPrec = {
                  labels: labelsPrec,
                  datasets: [
                  {
                    label: "Millimeter (mm)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataPrec
                  }
                ]};

                var ctx = document.getElementById("myChartPrecipitation").getContext("2d");
                new Chart(ctx).LineAlt(lineDataPrec, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                    tooltipEvents: [],
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END PRECIPITATION------->

//            <!---------TEMPERATURE--------->
            var dataTemp = [];
            var labelsTemp = [];

            $.ajax( {
                url : "/climate/getTemperature/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsTemp.push(x);
                        dataTemp.push(result[x]);

                  }

                  var lineDataTemp = {
                  labels: labelsTemp,
                  datasets: [
                  {
                    label: "Celsius (°C)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataTemp
                  }
                ]};

                var ctx = document.getElementById("myChartTemp").getContext("2d");
                new Chart(ctx).LineAlt(lineDataTemp, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                    tooltipEvents: [],
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END TEMPERATURE------->

//            <!---------WIND (DEGREES)--------->
            var dataWindDegree = [];
            var labelsWindDegree = [];

            $.ajax( {
                url : "/climate/getWindDegrees/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsWindDegree.push(x);
                        dataWindDegree.push(result[x]);

                  }

                  var lineDataWindDegree = {
                  labels: labelsWindDegree,
                  datasets: [
                  {
                    label: "Degrees (°)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataWindDegree
                  }
                ]};

                var ctx = document.getElementById("myChartWindDegree").getContext("2d");
                new Chart(ctx).LineAlt(lineDataWindDegree, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                    tooltipEvents: [],
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END WIND (DEGREES)------->

//            <!---------WIND (SPEED)--------->
            var dataWindSpeed = [];
            var labelsWindSpeed = [];

            $.ajax( {
                url : "/climate/getWindSpeed/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsWindSpeed.push(x);
                        dataWindSpeed.push(result[x]);

                  }

                  var lineDataWindSpeed = {
                  labels: labelsWindSpeed,
                  datasets: [
                  {
                    label: "Average wind speed (kt)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataWindSpeed
                  }
                ]};

                var ctx = document.getElementById("myChartWindSpeed").getContext("2d");
                new Chart(ctx).LineAlt(lineDataWindSpeed, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                    tooltipEvents: [],
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })


            });

            document.getElementById("myChartCapacitation").style.visibility = "visible";
            document.getElementById("myChartPrecipitation").style.visibility = "visible";
            document.getElementById("myChartTemp").style.visibility = "visible";
            document.getElementById("myChartWindDegree").style.visibility = "visible";
            document.getElementById("myChartWindSpeed").style.visibility = "visible";
//            <!-------END WIND (DEGREES)------->

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


                }),
                error: (function(result){
                    console.log(result);
                })
     });
}

function drawMapHistoricPerDay() {
    var map ;
    var olRoute;
    var point;
    var points = [];
    var eventLocation;

    // Base Layers
    var source = new ol.source.OSM({
             layers: 'basic'
    });

    var osm = new ol.layer.Tile({
     source: source,
     name: "osm"
     });

    olRoute  = new ol.layer.Vector({
        source: new ol.source.Vector()
    });

    var thessaloniki = [22.9308600, 40.6436200];
    var thessalonikiMercator = ol.proj.fromLonLat(thessaloniki);

    var view = new ol.View({
        center: thessalonikiMercator,
        zoom: 7
    });

    var features = [];


     $.ajax( {
                url : "/climate/getCoordinatesCurrentHistoric",
                success :(function(result){

                  for(var place in result){
                      for(var y in result[place]){
                         var iconFeature = new ol.Feature({
                                    geometry: new ol.geom.Point(ol.proj.transform([parseFloat(result[place][y]), parseFloat(y)], 'EPSG:4326', 'EPSG:3857')),
                                    name: place.split("_")[0],
                                    title: place.split("_")[1]
                                });

                                var iconStyle = new ol.style.Style({
                                  image: new ol.style.Icon( ({
                                    anchor: [0.5, 46],
                                    anchorXUnits: 'fraction',
                                    anchorYUnits: 'pixels',
                                    opacity: 0.75,
                                    src: 'http://www.myiconfinder.com/uploads/iconsets/32-32-5f9f834af5818fa985de0f1ed06ee2a5-pin.png'
                                  }))
                                });

                                iconFeature.setStyle(iconStyle);
                                features.push(iconFeature);
                      }
                  }

                  var vectorSource = new ol.source.Vector({
          features: features
    });

    var vectorLayer = new ol.layer.Vector({
          source: vectorSource
    });

    map = new ol.Map({
            layers: [osm, olRoute, vectorLayer],
            target: 'mapHistoricDay',
            view: view
    });

    var element = document.getElementById('popupHistoricDay');

    var popup = new ol.Overlay({
      element: element,
      positioning: 'bottom-center',
      stopEvent: false
    });
    map.addOverlay(popup);

    document.getElementById("chartsResultsHistoricDay").style.visibility = "hidden";

    map.on('click', function(evt) {
        var feature = map.forEachFeatureAtPixel(evt.pixel,
          function(feature, layer) {
            return feature;
          });
        if (feature) {
            document.getElementById("chartsResultsHistoricDay").style.visibility = "visible";

            popup.setPosition(evt.coordinate);
            $(element).popover({
              'placement': 'top',
              'html': true,
              'content': feature.get('name')
            });

            $(".head h4").html(feature.get('title'));

            Chart.types.Line.extend({
                name: "LineAlt",
                draw: function () {
                    Chart.types.Line.prototype.draw.apply(this, arguments);

                    var ctx = this.chart.ctx;
                    ctx.save();
                    // text alignment and color
                    ctx.textAlign = "center";
                    ctx.textBaseline = "bottom";
                    ctx.fillStyle = this.options.scaleFontColor;
                    // position
                    var x = this.scale.xScalePaddingLeft * 0.4;
                    var y = this.chart.height / 2;
                    // change origin
                    ctx.translate(x, y);
                    // rotate text
                    ctx.rotate(-90 * Math.PI / 180);
                    ctx.fillText(this.datasets[0].label, 0, 0);
                    ctx.restore();
                }
            });

//            <!---------TEMPERATURE--------->
            var dataMeanTemp = [];
            var dataHighTemp = [];
            var dataLowTemp = [];
            var labels = [];

            $.ajax( {
                url : "/climate/getTempHistoric/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labels.push(x);
                        dataMeanTemp.push(result[x].split("\t")[0]);
                        dataHighTemp.push(result[x].split("\t")[1]);
                        dataLowTemp.push(result[x].split("\t")[2]);
                  }


                  var lineData = {
                  labels: labels,
                  datasets: [
                  {
                    label: "Celsius (°C)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataMeanTemp
                  },
                  {
                    label: "Celsius (°C)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataHighTemp
                  },
                  {
                    label: "Celsius (°C)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataLowTemp
                  }
                ]};

                var ctx = document.getElementById("myChartTempHistDay").getContext("2d");
                var myLineChart = new Chart(ctx).LineAlt(lineData, {
                    responsive: false,
                    showTooltips: false,
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END TEMPERATURE-------->

//            <!---------RAIN--------->
            var dataRain = [];
            var labelsRain = [];

            $.ajax( {
                url : "/climate/getRainHistoric/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsRain.push(x);
                        dataRain.push(result[x]);
                  }

                  var lineDataTemp = {
                  labels: labelsRain,
                  datasets: [
                  {
                    label: "Millimeter (mm)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataRain
                  }
                ]};

                var ctx = document.getElementById("myChartRainHistDay").getContext("2d");
                new Chart(ctx).LineAlt(lineDataTemp, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                    tooltipEvents: [],
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END RAIN------->


//             <!---------WIND--------->
            var dataMeanWind = [];
            var dataHighWind = [];
            var labelsWind = [];

            $.ajax( {
                url : "/climate/getWindHistoric/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsWind.push(x);
                        dataMeanWind.push(result[x].split("\t")[0]);
                        dataHighWind.push(result[x].split("\t")[1]);
                  }
                  var lineData = {
                  labels: labelsWind,
                  datasets: [
                  {
                    label: "Average wind speed (km/h)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataMeanWind
                  },
                  {
                    label: "Average wind speed (km/h)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataHighWind
                  }
                ]};

                var ctx = document.getElementById("myChartWindHistDay").getContext("2d");
                var myLineChart = new Chart(ctx).LineAlt(lineData, {
                    responsive: false,
                    showTooltips: false,
                    scaleLabel: "          <%=value%>"
                });

                document.getElementById("myChartTempHistDay").style.visibility = "visible";
                document.getElementById("myChartRainHistDay").style.visibility = "visible";
                document.getElementById("myChartWindHistDay").style.visibility = "visible";

                $(".head-histday h3").hide();
                $(".children h3").show();

                if(dataMeanWind.length === 0){
                    if(lang === "en"){ $(".head-histday h3").html("No available data"); }
                    else if (lang === "el"){ $(".head-histday h3").html("Δεν υπάρχουν διαθέσιμα δεδομένα"); }

                    document.getElementById("myChartTempHistDay").style.visibility = "hidden";
                    document.getElementById("myChartRainHistDay").style.visibility = "hidden";
                    document.getElementById("myChartWindHistDay").style.visibility = "hidden";

                    $(".children h3").hide();
                    $(".head-histday h3").show();
                    $(".y-label").html("");
                }

                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END WIND-------->

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



                  }),
                error: (function(result){
                    console.log(result);
                    })
            });
}

function drawMapHistoric() {
    var map ;
    var olRoute;
    var point;
    var points = [];
    var eventLocation;

    // Base Layers
    var source = new ol.source.OSM({
             layers: 'basic'
    });

    var osm = new ol.layer.Tile({
     source: source,
     name: "osm"
     });

    olRoute  = new ol.layer.Vector({
        source: new ol.source.Vector()
    });

    var thessaloniki = [22.9308600, 40.6436200];
    var thessalonikiMercator = ol.proj.fromLonLat(thessaloniki);

    var view = new ol.View({
        center: thessalonikiMercator,
        zoom: 7
    });

    var features = [];


     $.ajax( {
                url : "/climate/getCoordinatesCurrentHistoric",
                success :(function(result){

                  for(var place in result){
                      for(var y in result[place]){
                         var iconFeature = new ol.Feature({
                                    geometry: new ol.geom.Point(ol.proj.transform([parseFloat(result[place][y]), parseFloat(y)], 'EPSG:4326', 'EPSG:3857')),
                                    name: place.split("_")[0],
                                    title: place.split("_")[1]
                                });

                                var iconStyle = new ol.style.Style({
                                  image: new ol.style.Icon( ({
                                    anchor: [0.5, 46],
                                    anchorXUnits: 'fraction',
                                    anchorYUnits: 'pixels',
                                    opacity: 0.75,
                                    src: 'http://www.myiconfinder.com/uploads/iconsets/32-32-5f9f834af5818fa985de0f1ed06ee2a5-pin.png'
                                  }))
                                });

                                iconFeature.setStyle(iconStyle);
                                features.push(iconFeature);
                      }
                  }

                  var vectorSource = new ol.source.Vector({
          features: features
    });

    var vectorLayer = new ol.layer.Vector({
          source: vectorSource
    });

    map = new ol.Map({
            layers: [osm, olRoute, vectorLayer],
            target: 'mapHistoric',
            view: view
    });

    var element = document.getElementById('popupHistoric');

    var popup = new ol.Overlay({
      element: element,
      positioning: 'bottom-center',
      stopEvent: false
    });
    map.addOverlay(popup);

    document.getElementById("chartsResultsHistoric").style.visibility = "hidden";

    map.on('click', function(evt) {
        var feature = map.forEachFeatureAtPixel(evt.pixel,
          function(feature, layer) {
            return feature;
          });
        if (feature) {
            document.getElementById("chartsResultsHistoric").style.visibility = "visible";

            popup.setPosition(evt.coordinate);
            $(element).popover({
              'placement': 'top',
              'html': true,
              'content': feature.get('name')
            });

            $(".head h4").html(feature.get('title'));

            Chart.types.Line.extend({
                name: "LineAlt",
                draw: function () {
                    Chart.types.Line.prototype.draw.apply(this, arguments);

                    var ctx = this.chart.ctx;
                    ctx.save();
                    // text alignment and color
                    ctx.textAlign = "center";
                    ctx.textBaseline = "bottom";
                    ctx.fillStyle = this.options.scaleFontColor;
                    // position
                    var x = this.scale.xScalePaddingLeft * 0.4;
                    var y = this.chart.height / 2;
                    // change origin
                    ctx.translate(x, y);
                    // rotate text
                    ctx.rotate(-90 * Math.PI / 180);
                    ctx.fillText(this.datasets[0].label, 0, 0);
                    ctx.restore();
                }
            });


//            <!---------TEMPERATURE--------->
            var dataMeanTemp = [];
            var dataHighTemp = [];
            var dataLowTemp = [];
            var labels = [];

            $.ajax( {
                url : "/climate/getTempCurrentYear/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labels.push(x);
                        dataMeanTemp.push(result[x].split("\t")[0]);
                        dataHighTemp.push(result[x].split("\t")[1]);
                        dataLowTemp.push(result[x].split("\t")[2]);
                  }
                  var lineData = {
                  labels: labels,
                  datasets: [
                  {
                    label: "Temperature (°C)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataMeanTemp
                  },
                  {
                    label: "Celsius (°C)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataHighTemp
                  },
                  {
                    label: "Celsius (°C)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataLowTemp
                  }
                ]};

                var ctx = document.getElementById("myChartTempHist").getContext("2d");
                var myLineChart = new Chart(ctx).LineAlt(lineData, {
                    responsive: false,
                    showTooltips: false,
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END TEMPERATURE-------->


//          <!---------PRECIPITATION--------->
            var dataPrecipitation = [];
            var labelsPrecipitation = [];

            $.ajax( {
                url : "/climate/getPrecipitationCurrentYear/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsPrecipitation.push(x);
                        dataPrecipitation.push(result[x]);
                  }

                  var lineDataTemp = {
                  labels: labelsPrecipitation,
                  datasets: [
                  {
                    label: "Total precipitation (mm)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataPrecipitation
                  }
                ]};

                var ctx = document.getElementById("myChartRainHist").getContext("2d");
                new Chart(ctx).LineAlt(lineDataTemp, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                    tooltipEvents: [],
                    scaleLabel: "          <%=value%>"
                });
                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END PRECIPITATION------->


//             <!---------WIND--------->
            var dataWind = [];
            var labelsWind = [];

            $.ajax( {
                url : "/climate/getWindSpeedCurrentYear/" + feature.get('name'),
                success :(function(result){

                  for(var x in result){
                        labelsWind.push(x);
                        dataWind.push(result[x]);
                  }
                  var lineData = {
                  labels: labelsWind,
                  datasets: [
                  {
                    label: "Average wind speed (km/h)",
                    fillColor: "rgba(220,220,220,0.2)",
                    strokeColor: "rgba(220,220,220,1)",
                    pointColor: "rgba(220,220,220,1)",
                    pointStrokeColor: "#fff",
                    pointHighlightFill: "#fff",
                    pointHighlightStroke: "rgba(220,220,220,1)",
                    data: dataWind
                  }
                ]};

                var ctx = document.getElementById("myChartWindHist").getContext("2d");
                new Chart(ctx).LineAlt(lineData, {
                    responsive: false,
                    tooltipTemplate: "<%= value %>",
                    showTooltips: true,
                    onAnimationComplete: function()
                    {
                        this.showTooltip(this.datasets[0].points, true);
                    },
                     tooltipEvents: [],
                     scaleLabel: "          <%=value%>"
                });

                 document.getElementById("myChartTempHist").style.visibility = "visible";
                 document.getElementById("myChartRainHist").style.visibility = "visible";
                 document.getElementById("myChartWindHist").style.visibility = "visible";

                $(".head-historic h3").hide();
                $(".children h3").show();

                if(dataWind.length === 0){
                    if(lang === "en"){ $(".head-historic h3").html("No available data"); }
                    else if (lang === "el"){ $(".head-historic h3").html("Δεν υπάρχουν διαθέσιμα δεδομένα"); }

                    document.getElementById("myChartTempHist").style.visibility = "hidden";
                    document.getElementById("myChartRainHist").style.visibility = "hidden";
                    document.getElementById("myChartWindHist").style.visibility = "hidden";

                    $(".children h3").html("");
                    $(".head-historic h3").show();
                    $(".y-label").html("");
                }

                }),
                error: (function(result){
                    console.log(result);
                    })
            });
//            <!-------END WIND-------->

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
                  }),
                error: (function(result){
                    console.log(result);
                    })
            });
}

$(document).ready(function() {
    drawMapCurrent();
    $('.nav-tabs').on('shown.bs.tab',function(){
        document.getElementById("chartsResultsHistoric").style.visibility = "hidden";
        document.getElementById("myChartTempHist").style.visibility = "hidden";
        document.getElementById("myChartRainHist").style.visibility = "hidden";
        document.getElementById("myChartWindHist").style.visibility = "hidden";

        document.getElementById("chartsResultsHistoricDay").style.visibility = "hidden";
        document.getElementById("myChartTempHistDay").style.visibility = "hidden";
        document.getElementById("myChartRainHistDay").style.visibility = "hidden";
        document.getElementById("myChartWindHistDay").style.visibility = "hidden";

        document.getElementById("chartsResults").style.visibility = "hidden";
        document.getElementById("myChartCapacitation").style.visibility = "hidden";
        document.getElementById("myChartPrecipitation").style.visibility = "hidden";
        document.getElementById("myChartTemp").style.visibility = "hidden";
        document.getElementById("myChartWindDegree").style.visibility = "hidden";
        document.getElementById("myChartWindSpeed").style.visibility = "hidden";

        document.getElementById("chartsResultsLast2days").style.visibility = "hidden";
        document.getElementById("myChartCurrentTemp").style.visibility = "hidden";
        document.getElementById("myChartCurrentRain").style.visibility = "hidden";
        document.getElementById("myChartCurrentWindSpeed").style.visibility = "hidden";

        $('.ol-viewport').remove();
        drawMapCurrent();
        drawMapForecast();
        drawMapHistoric();
        drawMapHistoricPerDay();
    });
});

$('#viewDropdown').addClass('active');
var lang = document.getElementsByClassName("lang")[0].value;