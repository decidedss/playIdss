var table;

$(document).ready(function() {
    table = $('#phenomenonTable').DataTable();
    $('#phenomenonTable tbody').on('click',"input[type='button']",function() {
        edit_delete(this);
    });
});

function edit_delete(but){
    var action = but.id.split(",")[1];
    var id = but.id.split(",")[0];

    if(action === 'edit'){
        $.ajax( {
        url : "/actions/getPhenomenon/" + id,
        success :(function(data){
            for(var k in data){
                $('[id="'+k+'"]').val(data[k]);
            }
        }),
        error: (function(result){ }),
        });
    }else if (action === 'delete'){
        $.ajax( {
            url : "/phenomenonAction/delete/" + id,
            success :(function(data){
                table.row(but.closest('tr')).remove().draw();
            })
        });
    }
}