var map;
var olRoute;
var point;
var points = [];
var machineryLocation;
var allLayers = [];
var iconFeatures = [];
//var infoFormat = 'text/javascript';
var popup;

var featureNS = 'sf';
var formatWFS = new ol.format.WFS();
var formatGML;
var typeSelect = document.getElementById('type');
var selectPointerMove_Highlight;
var selectClick_Delete;

// Base Layers
var source = new ol.source.OSM({
   layers: 'basic'
});

var osm = new ol.layer.Tile({
     source: source,
     name: "osm"
});

olRoute  = new ol.layer.Vector({
   source: new ol.source.Vector(),

});

var view = new ol.View({
        center: ol.proj.transform([24, 38], 'EPSG:4326', 'EPSG:3857'),
        zoom: 6
});

allLayers.push(osm);
allLayers.push(olRoute);

var sourcePointsVector = new ol.source.Vector({
    loader: function(extent) {
        var url;
        if (agency === "GROUP_ALFRESCO_ADMINISTRATORS") {
            url = server_path + ":8080/geoserver/wfs?service=WFS&" + "version=1.1.0&request=GetFeature&typename=_machinery_layer&" + "srsname=EPSG:3857" + "&bbox=" + extent.join(',') + ",EPSG:3857";
        } else {

            var sAgencies = sharedAgencies.split(',');
            var shared = "";

            for (var i in sAgencies) {
                if (shared === "") {
                    shared = "agency%20=%20%27" + sAgencies[i] + "%27";
                } else {
                    shared = shared + "%20OR%20agency%20=%20%27" + sAgencies[i] + "%27";
                }
            }
            shared = shared + "%20Or%20agency%20=%20%27" + agency + "%27";

            url = server_path + ":8080/geoserver/wfs?service=WFS&" + "version=1.1.0&request=GetFeature&typename=_machinery_layer&CQL_FILTER=" + shared + "&srsname=EPSG:3857";
        }
        $.ajax(url, {
                type: 'GET',

            }).done(loadPointsFeatures)
            .fail(function() {
                console("error loading points vector layer");
            })
            .success(function(response) {
                pointsVector.getSource().forEachFeature(function(feature) {
                    $.ajax({
                        url: "/resources/machinery/thumbnail/" + feature.C.machinery_id,
                        success: (function(data) {
                            if (feature.C.agency === agency) {
                                style = [
                                    new ol.style.Style({
                                        image: new ol.style.Icon( /** @type {olx.style.IconOptions} */ ({

                                            anchorXUnits: 'fraction',
                                            anchorYUnits: 'pixels',
                                            src: data
                                        }))
                                    }),
                                    new ol.style.Style({
                                        image: new ol.style.Circle({
                                            radius: 5,
                                            fill: new ol.style.Fill({
                                                color: 'rgba(230,120,30,0.8)'
                                            })
                                        })
                                    })
                                ];
                                feature.setStyle(style);
                            } else {
                                style = [
                                    new ol.style.Style({
                                        image: new ol.style.Icon(({
                                            anchorXUnits: 'fraction',
                                            anchorYUnits: 'pixels',
                                            src: data
                                        }))
                                    })
                                ];


                                feature.setStyle(style);
                            }

                        }),
                        error: (function(result) {
                            // console.log(result);
                        })
                    });
                });
            });
    },
    strategy: ol.loadingstrategy.bbox
});



var pointsVector = new ol.layer.Vector({
    source: sourcePointsVector,
    symbolizer: {pointRadius: 20, fillColor: "red",
                   fillOpacity: 0.7, strokeColor: "black"}
});

controlMousePos = new ol.control.MousePosition({
	coordinateFormat: ol.coordinate.createStringXY(4),
});

popup = document.getElementById('popup_preview');

overlayPopup = new ol.Overlay({
	element: popup
});

var map = new ol.Map({
         layers: [
           new ol.layer.Tile({
             source: new ol.source.OSM()
           }),
           pointsVector
         ],
         target: 'map',
         overlays: [overlayPopup],
         controls: [controlMousePos,
                   new ol.control.Zoom(),
                   new ol.control.Attribution(),
                ],
         view: new ol.View({
           center: ol.proj.transform([24, 38], 'EPSG:4326', 'EPSG:3857'),
           zoom: 6
         }),
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

var draw;

typeSelect.onchange = function(e) {
    var element = popup.getElement();
    $(element).popover('destroy');

    addInteraction();

    formatGML = new ol.format.GML({
            featureNS: featureNS,
            featureType: '_machinery_layer',
            srsName: 'EPSG:3857'
     });
};

var keydown = function(evt){
    var charCode = (evt.which) ? evt.which : evt.keyCode;
    if (charCode === 27){ //esc key
        //dispatch event
        draw.set('escKey', Math.random());
    }
};
document.addEventListener('keydown', keydown, false);

function addInteraction() {

    var element = popup.getElement();
    var drawing_feature;

    draw = new ol.interaction.Draw({
            features: features,
            type: (typeSelect.value)
    });

    map.addInteraction(draw);

    var machinery_id = $('#type').find('option:selected').attr('id').split(",")[0];
    var machinery_type = $('#type').find('option:selected').attr('id').split(",")[1];
    var username = $('#type').find('option:selected').attr('id').split(",")[2];
    var agency =  $('#type').find('option:selected').attr('id').split(",")[3];

    var removeLastFeature = function () {
        if (drawing_feature) {
            var geom = drawing_feature.getGeometry();
            geom.setCoordinates([0,0], [0,0]);
            sourcePointsVector.clear();
        }
    };

    draw.on('drawstart', function(evt){
        removeLastFeature();
        drawing_feature = evt.feature;

    });

    draw.on('change:escKey', function(evt){
        var geom = drawing_feature.getGeometry();
        geom.setCoordinates([0,0], [0,0]);
        popup.setPosition(undefined);
    });

    draw.on('drawend', function(evt) {

        var coords = evt.feature.getGeometry().getCoordinates();

        $(element).popover('destroy');
        popup.setPosition(coords);

        var popoverContent =
        '<div id="popup-closer" class="ol-popup-closer add"> <i class="glyphicon glyphicon-remove"></i></div>'+
        '<div id="popover-content" class="popover-content-add">' +
        '<div class="form-group">' +
        '<small>'+ brandTitle +'</small>' +
        '<input type="text" class="form-control" name="brand" id="brand" placeholder="'+ egBrand +'" required>'+
        '<h6>'+ licensePlateTitle +'</h6>' +
        '<input type="text" class="form-control" name="licence_plate" id="licence_plate" placeholder="'+ egLicensePlate +'" required>'+
        '<h6>'+ horsepowerTitle +'</h6>'+
        '<input type="text" class="form-control"  name="bhp" id="bhp" placeholder="'+ egHorsepower +'">'+
        '<h6>'+ seatTitle +'</h6>'+
        '<input type="text" class="form-control" id="seats" name="seats" id="seats" placeholder="'+ egSeat +'">'+
        '<h6>'+ equipmentTitle +'</h6>'+
        '<input type="text" class="form-control" id="equipment" name="equipment" id="equipment" placeholder="'+ equipmentTitle +'">'+
        '<h6>'+ cargoTitle +'</h6>'+
        '<input type="text" class="form-control" id="cargo_type" name="cargo_type" id="cargo_type" placeholder="'+ cargoTitle +'">'+
        '<h6>'+ capacityTitle +'</h6>'+
        '<input type="text" class="form-control" id="capacity_m3" name="capacity_m3" id="capacity_m3" placeholder="'+ capacityTitle +'">'+
        '<h6>'+ driverTitle +'</h6>'+
        '<input type="text" class="form-control" id="driver" name="driver" id="driver" placeholder="'+ driverTitle +'">'+
        '<h6>'+ disasterTitle +'</h6>'+
        '<input type="text" class="form-control" id="disaster_type" name="disaster_type" id="disaster_type" placeholder="'+ egDisaster +'">'+
        '<h6>'+ equipmentConditionTitle +'</h6>'+
        '<input type="text" class="form-control" id="machinery_status" name="machinery_status" id="machinery_status" placeholder="'+ egEquipmentCondition +'">'+
        '<h6>'+ tiresConditionTitle +'</h6>'+
        '<input type="text" class="form-control" id="tires_status" name="tires_status" id="tires_status" placeholder="'+ egTiresCondition +'">'+
        '<h6>'+ machineryAvailabilityTitle +'</h6>'+
        '<input type="radio" id="availability_true" name="availability" value="true" checked> '+ yesTitle +'<br/>'+
        '<input type="radio" id="availability_false" name="availability" value="false"> '+ noTitle +''+
        '<h6>'+ notesTitle +'</h6>'+
        '<input type="text" class="form-control" id="notes" name="notes" id="notes" placeholder="'+ egNotes +'"><br/>'+
        '<button class="btn btn-warning btn-xs" id="saveGeom" data-toggle="tooltip" data-placement="top" title="'+ saveGeometryMetadata +'">' +
        '<span class="glyphicon glyphicon-floppy-disk"></span></button>'+
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


        $('#popup-closer').on('click', function() {
            var geom = drawing_feature.getGeometry();
            geom.setCoordinates([0,0], [0,0]);
            popup.setPosition(undefined);
        });

        document.getElementById("saveGeom").onclick = function() {

            var brand="";
            var licence_plate="";
            var bhp="";
            var seats="";
            var equipment="";
            var cargo_type="";
            var capacity_m3="";
            var driver="";
            var disaster_type="";
            var machinery_status="";
            var tires_status="";
            var notes="";

            if (document.getElementById('brand').value!=="") { brand = document.getElementById('brand').value; }
            if (document.getElementById('licence_plate').value!=="") { licence_plate = document.getElementById('licence_plate').value; }
            if (document.getElementById('bhp').value!=="") { bhp = document.getElementById('bhp').value; }
            if (document.getElementById('seats').value!=="") { seats = document.getElementById('seats').value; }
            if (document.getElementById('equipment').value!=="") { equipment = document.getElementById('equipment').value; }
            if (document.getElementById('cargo_type').value!=="") { cargo_type = document.getElementById('cargo_type').value; }
            if (document.getElementById('capacity_m3').value!=="") { capacity_m3 = document.getElementById('capacity_m3').value; }
            if (document.getElementById('driver').value!=="") { driver = document.getElementById('driver').value; }
            if (document.getElementById('disaster_type').value!=="") { disaster_type = document.getElementById('disaster_type').value; }
            if (document.getElementById('machinery_status').value!=="") { machinery_status = document.getElementById('machinery_status').value; }
            if (document.getElementById('tires_status').value!=="") { tires_status = document.getElementById('tires_status').value; }
            if (document.getElementById('notes').value!=="") { notes = document.getElementById('notes').value; }

            var availabilities = document.getElementsByName('availability');
            var availability_value;
            for(var i = 0; i < availabilities.length; i++){
                if(availabilities[i].checked){
                    availability_value = availabilities[i].value;
                }
            }

            transactWFS('insert', evt.feature, '', machinery_id, machinery_type, username, agency, brand, licence_plate, bhp, seats, equipment, cargo_type, capacity_m3, driver, disaster_type, machinery_status, tires_status, notes, availability_value);
            $(element).popover('destroy');
        };
    });
}


function transactWFS(p,feature, geometry, machinery_id, machinery_type, username, agency, brand, licence_plate, bhp, seats, equipment, cargo_type, capacity_m3, driver, disaster_type, machinery_status, tires_status, notes, availability){
    var s;
    var str;
    var formatGML = new ol.format.GML({
                featureNS: featureNS,
                featureType: '_machinery_layer',
                srsName: 'EPSG:3857'
    });

    switch(p) {
        case 'insert': // *****************************************************************************
            var d = new Date();
            var currentDate = d.toString();
            var transformedGeom;

            if(typeSelect.value=='Point'){
                transformedGeom = new ol.geom.Point(feature.getGeometry().getCoordinates());
            }

            transformedGeom.transform('EPSG:3857', 'EPSG:4326');
            feature.set('machinery_id', machinery_id);
            feature.set('machinery_type', machinery_type);
            feature.set('insert_date', currentDate);
            feature.set('geom', transformedGeom);
            feature.set('username', username.trim());
            feature.set('agency', agency.replace(/^[ ]+|[ ]+$/g,''));
            feature.set('brand', brand);
            feature.set('licence_plate', licence_plate);
            feature.set('bhp', bhp);
            feature.set('seats', seats);
            feature.set('equipment', equipment);
            feature.set('cargo_type', cargo_type);
            feature.set('capacity_m3', capacity_m3);
            feature.set('driver', driver);
            feature.set('disaster_type', disaster_type);
            feature.set('machinery_status', machinery_status);
            feature.set('tires_status', tires_status);
            feature.set('notes', notes);
            feature.set('availability', availability);

            node = formatWFS.writeTransaction([feature],null,null,formatGML);
            s = new XMLSerializer();
            str = s.serializeToString(node);
            break;
        case 'update': // *****************************************************************************
                var db_table = feature.getId().substring(0, feature.getId().indexOf("."));
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
        case 'delete': // *****************************************************************************
            db_table = feature.getId().substring(0, feature.getId().indexOf("."));
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
        }
    });
}

var select = new ol.interaction.Select({
	style: new ol.style.Style({
		stroke: new ol.style.Stroke({
			color: '#FF2828'
		})
	})
});

$('#btnDelete').on('click', function() {
    map.removeInteraction(draw);
    document.getElementById('type').value = null;

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
                        sourcePointsVector.removeFeature(feature);
                        transactWFS('delete', feature);

                        map.removeInteraction(selectPointerMove_Highlight);
                        collection.clear();
                        interaction_added = false;
                        //location.reload();
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

$('#btnEdit').on('click', function(e) {

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
            transformedGeom = new ol.geom.Point(f.getGeometry().getCoordinates());
            transformedGeom.transform('EPSG:3857', 'EPSG:4326');

            if (dirty[f.getId()]){
              delete dirty[f.getId()];
              featureProperties = f.getProperties();
              var clone = new ol.Feature(featureProperties);
              clone.setId(f.getId());
              transactWFS('update',clone,transformedGeom);
            }
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

          var notAvailable;
          var availabilityTitle;

          if(lang === 'el'){
            notAvailable='μ/δ';
            if(props.availability === 'true'){availabilityTitle='ναι';}
            if(props.availability === 'false'){availabilityTitle='όχι';}
          }
          else if(lang === 'en'){
            notAvailable='n/a';
            if(props.availability === 'true'){availabilityTitle='yes';}
            if(props.availability === 'false'){availabilityTitle='no';}
          }

          var agencyGroupname = '';
          if (!props.agency){ agencyGroupname = notAvailable; }
          else{
            agencyGroupname = mapGroupnameAgency.get(props.agency);
            if(agencyGroupname === undefined) agencyGroupname = notAvailable;
          }

          if (!props.machinery_type){ props.machinery_type = notAvailable; }
          if (!props.agency){ props.agency = notAvailable; }
          if (!props.brand){ props.brand = notAvailable; }
          if (!props.licence_plate){ props.licence_plate = notAvailable; }
          if (!props.bhp){ props.bhp = notAvailable; }
          if (!props.seats){ props.seats = notAvailable; }
          if (!props.equipment){ props.equipment = notAvailable; }
          if (!props.cargo_type){ props.cargo_type = notAvailable; }
          if (!props.capacity_m3){ props.capacity_m3 = notAvailable; }
          if (!props.driver){ props.driver = notAvailable; }
          if (!props.disaster_type){ props.disaster_type = notAvailable; }
          if (!props.machinery_status){ props.machinery_status = notAvailable; }
          if (!props.tires_status){ props.tires_status = notAvailable; }

          if (!props.availability){ props.availability = notAvailable; }
          else {
            if(props.availability === 'true'){props.availability = 'Yes';}
            else{props.availability = 'No';}
          }

          if (!props.notes){ props.notes = notAvailable; }


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
                  '<strong>'+ machineryTypeTitle +'</strong> <span id="popup-machinery_type">'+props.machinery_type+'</span><br/>'+
                  '<strong>'+ agencyTitle +':</strong> <span id="popup-agency">'+agencyGroupname+'</span><br/>'+
                  '<span class="preview_title">'+ coordinatesTitle +':</span> <span id="popup-coordinates">'+coordinatesUpdated+'</span>'+
                  '<hr>'+
                  '<strong>'+ brandTitle +':</strong> <span id="popup-brand">'+props.brand+'</span><br/>'+
                  '<strong>'+ licensePlateTitle +':</strong> <span id="popup-licence_plate">'+props.licence_plate+'</span><br/>'+
                  '<strong>'+ horsepowerTitle +':</strong> <span id="popup-bhp">'+props.bhp+'</span><br/>'+
                  '<strong>'+ seatTitle +':</strong> <span id="popup-seats">'+props.seats+'</span><br/>'+
                  '<strong>'+ equipmentTitle +':</strong> <span id="popup-equipment">'+props.equipment+'</span><br/>'+
                  '<strong>'+ cargoTitle +':</strong> <span id="popup-cargo_type">'+props.cargo_type+'</span><br/>'+
                  '<strong>'+ capacityTitle +':</strong> <span id="popup-capacity_m3">'+props.capacity_m3+'</span><br/>'+
                  '<strong>'+ driverTitle +':</strong> <span id="popup-driver">'+props.driver+'</span><br/>'+
                  '<strong>'+ disasterTitle +':</strong> <span id="popup-disaster_type">'+props.disaster_type+'</span><br/>'+
                  '<strong>'+ equipmentConditionTitle +':</strong> <span id="popup-machinery_status">'+props.machinery_status+'</span><br/>'+
                  '<strong>'+ tiresConditionTitle +':</strong> <span id="popup-tires_status">'+props.tires_status+'</span><br/>'+
                  '<strong>'+ machineryAvailabilityTitle +':</strong> <span id="popup-availability">'+availabilityTitle+'</span><br/>'+
                  '<strong>'+ notesTitle +':</strong> <span id="popup-notes">'+props.notes+'</span><br/>'+
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
            overlayPopup.setPosition(undefined);
            $(element).popover('destroy');
          });

      });
});


function loadPointsFeatures(response) {
    sourcePointsVector.addFeatures(formatWFS.readFeatures(response));
}

$('#manageDropdown').addClass('active');

$(".js-example-basic-single").select2({
    theme : "classic"
});

var agency = document.getElementById("agency").value;
var sharedAgencies = document.getElementById("sharedAgencies").value;
var groupnameAgency = document.getElementById("groupnameAgency").value;

var brandTitle = document.getElementById("brandTitle").value;
var licensePlateTitle = document.getElementById("licensePlateTitle").value;
var horsepowerTitle = document.getElementById("horsepowerTitle").value;
var seatTitle = document.getElementById("seatTitle").value;
var equipmentTitle = document.getElementById("equipmentTitle").value;
var cargoTitle = document.getElementById("cargoTitle").value;
var capacityTitle = document.getElementById("capacityTitle").value;
var driverTitle = document.getElementById("driverTitle").value;
var disasterTitle = document.getElementById("disasterTitle").value;
var equipmentConditionTitle = document.getElementById("equipmentConditionTitle").value;
var tiresConditionTitle = document.getElementById("tiresConditionTitle").value;
var notesTitle = document.getElementById("notesTitle").value;
var machineryAvailabilityTitle = document.getElementById("machineryAvailabilityTitle").value;
var yesTitle = document.getElementById("yesTitle").value;
var noTitle = document.getElementById("noTitle").value;
var agencyTitle = document.getElementById("agencyTitle").value;
var machineryTypeTitle = document.getElementById("machineryTypeTitle").value;
var coordinatesTitle = document.getElementById("coordinatesTitle").value;
var lang = document.getElementsByClassName("lang")[0].value;

var egBrand = document.getElementById("egBrand").value;
var egLicensePlate = document.getElementById("egLicensePlate").value;
var egHorsepower = document.getElementById("egHorsepower").value;
var egSeat = document.getElementById("egSeat").value;
var egEquipmentCondition = document.getElementById("egEquipmentCondition").value;
var egTiresCondition = document.getElementById("egTiresCondition").value;
var egDisaster = document.getElementById("egDisaster").value;
var egNotes = document.getElementById("egNotes").value;
var saveGeometryMetadata = document.getElementById("saveGeometryMetadata").value;

groupnameAgency = groupnameAgency.replace("{", "");
groupnameAgency = groupnameAgency.replace("}", "");

var mapGroupnameAgency = new Map();
var datas = groupnameAgency.split(", ");


for(var i in datas){
    var key = datas[i].split("=")[0];
    var value = datas[i].split("=")[1];
    mapGroupnameAgency.set(key,value);
}