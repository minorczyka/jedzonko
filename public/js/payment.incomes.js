$(function(){
    $('.income-accept').click(function () {
        var icon = $(this).children();
        if (icon.hasClass('fa-check-circle')) {
            icon.removeClass('fa-check-circle');
            icon.addClass('fa-check-circle-o');
        } else {
            var parent = $(this).parent();
            var incomeId = $(this).attr('income-id');
            $(this).remove();
            parent.append('<i class="fa fa-spinner fa-pulse fa-lg"></i>')
            $.get("/api/payment/accept/" + incomeId, function () {
                parent.children().remove();
                var status = parent.prev();
                var badge = status.children('span');
                var payed = $('#payed-text').text();
                badge.removeClass('bg-red');
                badge.addClass('bg-green');
                badge.text(payed);
            });
        }
    });

    $("#incomes-table").DataTable({
        "paging": true,
        "lengthChange": true,
        "searching": true,
        "ordering": true,
        "info": false,
        "autoWidth": false,
        "pageLength": 25
    });
});
