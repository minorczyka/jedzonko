$(function(){
    function getCurrentDate() {
        var today = new Date();
        var dd = today.getDate();
        var mm = today.getMonth() + 1; //January is 0!
        var yyyy = today.getFullYear();

        if (dd < 10) {
            dd = '0' + dd
        }

        if (mm < 10) {
            mm = '0' + mm
        }

        today = mm + '-' + dd + '-' + yyyy + ' 10:00';
        return today;
    }
    $('.form_datetime').datetimepicker({
        format: "yyyy-mm-dd HH:ii:00",
        language:  'pl',
        weekStart: 1,
        autoclose: 1,
        todayHighlight: 1,
        startView: 1,
        forceParse: 0,
        startDate: getCurrentDate()
    });

    $('[data-toggle="tooltip"]').tooltip();
});