var table;
    $(document).ready(function() {
         table = $('#example').DataTable();
         if (table.rows().data().length < 1){
                $('#timesheetBtn').hide();
         }

        $('#example tbody').on('click',"input[type='button']",function() {
           edit_delete(this);
        });

        $("input").prop('required',true);
        $("select").prop('required',true);

        $('#planDropdown').addClass('active');

        $.fn.datepicker.defaults.format = "yyyy-mm-dd";

        $('#startdate').datepicker().on('changeDate',function(e) {
            $('#startdate').datepicker('hide');
        });

        $('#enddate').datepicker().on('changeDate',function(e) {
            $('#enddate').datepicker('hide');
        });

        $('#startdate-n').datepicker().on('changeDate',function(e) {
            $('#startdate-n').datepicker('hide');
        });

        $('#enddate-n').datepicker().on('changeDate',function(e) {
            $('#enddate-n').datepicker('hide');
        });

        $(".js-example-basic-single").select2({
            theme : "classic"
        });
});

function edit_delete(but){
    var action = but.id.split(",")[2];

    var id = but.id.substring(0, but.id.indexOf(','));
    var name = but.id.substring(but.id.indexOf(',') + 1, but.id.length);

    if(action === 'edit'){
            $.ajax( {
                    url : "/measures/getMeasure/" + id,
                    success :(function(data){
                       for(var k in data){
                            $('[id="'+k+'"]').val(data[k]);
                       }
                    }),
                    error: (function(result){
                    }),
            });
    }else if (action === 'delete'){

    var result;

    $.ajax( {
                url : "/measures/delete/" + id,
                success :(function(data){
                    // result = data.length;
                    table.row(but.closest('tr')).remove().draw();
                    if (table.rows().data().length < 1){
                         $('#timesheetBtn').hide();
                     }

                    $('#delete_alert').html("<strong>Success!</strong> Measure: <strong>" +  but.id.split(",")[1] + "</strong> was deleted");
                    $('#delete_alert').show();
                })
            });
    }
}