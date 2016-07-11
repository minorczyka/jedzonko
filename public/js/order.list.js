$(function () {
    $("#orders-table").DataTable({
        "paging": true,
        "lengthChange": true,
        "searching": true,
        "ordering": true,
        "info": false,
        "autoWidth": false,
        "pageLength": 25,
        "order": []
    });
});
