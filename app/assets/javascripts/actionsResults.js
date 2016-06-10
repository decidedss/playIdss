$(document).ready(function() {
    $('#planDropdown').addClass('active');

    $(".js-example-basic-single").select2({
        theme : "classic"
    });

    $('#phenomenon').on('change',function(){
            if( $(this).val()==="other"){
                $("#phenomenon-other").show();
                document.getElementById("phenomenon").required = false;
                document.getElementById("phenomenon-other-child").required = true;
            }
            else{
                $("#phenomenon-other").hide();
                document.getElementById("phenomenon").required = true;
                document.getElementById("phenomenon-other-child").required = false;
            }
    });
});