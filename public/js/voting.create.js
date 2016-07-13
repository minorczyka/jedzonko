$(function(){
    var placeSelect = $("#placesIds");
    var groupSelect = $("#groupId");

    function getPlaces(groupId) {
        $.get("/api/place/list/" + groupId, function (data, status) {
            placeSelect.empty();
            $.each(data, function(index, value) {
                placeSelect.append($("<option></option>")
                    .attr("value", data[index].id).text(data[index].name));
            });
            if ($('#placesIds').attr("multiple")) {
                $('#placesIds').multiSelect()
            }
        });
    }
    getPlaces(groupSelect.val());
    groupSelect.change(function () {
        getPlaces(groupSelect.val());
    });
});
