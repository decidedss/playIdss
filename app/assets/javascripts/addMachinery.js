var tableInstance;
        var tableInstanceOther;

        $(document).ready(function() {

            tableInstance = $('#machineryInstance').DataTable();
            tableInstanceOther = $('#machineryInstanceOther').DataTable();

             $('#addMachinery').click(function(){
               document.getElementById("imgPreview").src = "/assets/images/machinery_default.png";
               document.getElementById("imgPreview").width = "128";
               document.getElementById("imgPreview").height = "128";
             });

             $('#machineryInstance tbody').on('click',"input[type='button']",function() {
                edit_delete_instance(this);
             });
        });

        function edit_delete_instance(but){

            var id = but.id.substring(0, but.id.indexOf(','));
            var action = but.id.substring(but.id.indexOf(',') + 1, but.id.length);

            if (action === "edit"){
                $.ajax( {
                    url : "/resources/machinery/getLayer/" + id,
                    success :(function(data){
                       for(var k in data){
                            $('[id="'+k+'"]').val(data[k]);
                       }
                    }),
                    error: (function(result){
                    }),
                });
            }

            if (action === "delete"){
                $.ajax( {
                    url : "/resources/machinery/deleteLayer/" + id,
                    success :(function(data){
                        tableInstance.row(but.closest('tr')).remove().draw();
                    })
                });
            }
}



function readURL(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();

        reader.onload = function (e) {
            $('#imgPreview').attr('src', e.target.result);
            $('#imgPreview_edit').attr('src', e.target.result);
        };
        reader.readAsDataURL(input.files[0]);
    }
}

$("#imgInp").change(function(){
    readURL(this);
});

$("#imgInp_edit").change(function(){
    readURL(this);
});

$('#manageDropdown').addClass('active');