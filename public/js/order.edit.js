$(function(){
    $('.order-edit-btn').click(function () {
        var row = $(this).parent().parent().parent();
        var id = row.attr('userOrderId');
        var subject = row.children('.user-order-subject')[0].innerHTML;
        var info = row.children('.user-order-info')[0].innerHTML;
        var cost = row.children('.user-order-cost')[0].innerHTML;
        $('input[name=userOrderId]').val(id);
        $('input#editSubject').val(subject);
        $('input#editAdditionalInfo').val(info);
        $('input#editCost').val(parseFloat(cost.replace(",", ".")));
    })
});
