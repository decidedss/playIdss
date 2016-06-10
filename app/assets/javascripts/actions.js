$(document).ready(function() {
    $('#actionsTbl').DataTable();

    $('#actionsTbl tbody .deleteAction').on('click',"input[type='button']",function() {
        deleteCall(this);
        location.reload();
    });

    $('#actionsTbl tbody .editAction').on('click',"input[type='button']",function() {
         editCall(this);
    });

    $(".js-example-basic-single").select2({
        theme : "classic",
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

function GetPhases(type)
{
    var ActionForm;

    if(type === "add"){
                ActionForm = document.forms.AddForm;
    }else if (type === "search" || type === "edit"){
                ActionForm = document.forms.ActionsForm;
    }

    var phaseVal = "";
     var x = 0;

    for (x=0; x < ActionForm.phase.length; x++)
    {
        if (ActionForm.phase[x].selected)
        {
            phaseVal = phaseVal + "," + ActionForm.phase[x].value;
        }
    }

    var addPhase;

    if(type === "add"){
        addPhase=document.getElementsByName("phasesAdd");
    }else if (type === "search" || type === "edit"){
        addPhase=document.getElementsByName("phases");
    }

    addPhase[0].value = phaseVal.substring(1);
}

function deleteCall(but) {
    var id_actions = but.id.substring(0, but.id.indexOf(','));
    var id = but.id.substring(but.id.indexOf(',') + 1, but.id.length);

    $.ajax( {
        url : "/actions/delete/" + id_actions,
        success :(function(data){
            table.row(but.closest('tr')).remove().draw();
        })
    });
}

function editCall(but){

    var id = but.id.substring(but.id.indexOf(',') + 1, but.id.length);

    $.ajax( {
        url : "/actions/getAction/" + id,
        success :(function(data){
            for(var k in data){
                 $('[id="'+k+'"]').val(data[k]);
                 if(k === "phenomenon"){
                    document.getElementById('phenomenon').value=data[k];
                 }
            }
         }),
         error: (function(result){
         }),
    });
}


