function addGeolocation(){
    var geolocation = new ol.Geolocation({
        projection: ol.proj.get('EPSG:3857'),
        tracking: true
    });
    //
    // handle geolocation error
    geolocation.on('error', function(error) {
        var center = ol.proj.transform([ 21.67, 40.68], 'EPSG:4326', 'EPSG:3857');
        map.getView().setCenter(center);
    });
    //
    geolocation.setTracking(true);
    map.getView().setCenter(geolocation.getPosition());
    //
    geolocation.on('change', function(evt) {
        map.getView().setCenter(geolocation.getPosition());
    });
}