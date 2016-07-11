$(function () {
    $('.charges-prepaid').click(function () {
        var icon = $(this).children();
        if (icon.hasClass('fa-check-circle')) {
            icon.removeClass('fa-check-circle');
            icon.addClass('fa-check-circle-o');
        } else {
            var parent = $(this).parent();
            var chargeId = $(this).attr('charge-id');
            $(this).remove();
            parent.append('<i class="fa fa-spinner fa-pulse fa-lg"></i>')
            $.get("/api/payment/prepaid/" + chargeId, function (data) {
                if (data["message"] === "ok") {
                    parent.children().remove();
                    var status = parent.prev();
                    var badge = status.children('span');
                    var payed = $('#payed-text').text();
                    badge.removeClass('bg-red');
                    badge.addClass('bg-green');
                    badge.text(payed);
                } else {
                    parent.children().remove();
                    parent.append('<i class="fa fa-times-circle fa-lg text-danger"></i>')
                }
            });
        }
    });

    $("#charges-table").DataTable({
        "paging": true,
        "lengthChange": true,
        "searching": true,
        "ordering": true,
        "info": false,
        "autoWidth": false,
        "pageLength": 25
    });
});
