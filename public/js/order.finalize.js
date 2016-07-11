$(function(){
    jQuery.fn.pop = [].pop;
    jQuery.fn.shift = [].shift;
    var orderId = parseInt($('#order-id').text());

    function getData() {
        var rows = $('.table').find('tr');
        var orders = [];

        rows.shift();
        rows.each(function() {
            var id = $(this).find('.id');
            var value = $(this).find('.value');
            var row = {};

            row["id"] = parseInt(id.text());
            if (value.val()) {
                row["cost"] = parseFloat(value.val());
            }

            orders.push(row);
        });
        var discount = parseFloat($('#discount').val());
        var additionalCost = parseFloat($('#additional-cost').val());

        var data = {
            orderId: orderId,
            discount: discount,
            additionalCost: additionalCost,
            orders: orders
        };
        return data;
    }

    function sumOrders() {
        var sum = 0;
        var data = getData();
        for (var i = 0; i < data.orders.length; ++i) {
            if (data.orders[i].cost) {
                sum += parseFloat(data.orders[i].cost);
            } else {
                return null;
            }
        }
        return sum;
    }

    function update() {
        var sum = sumOrders();
        var discount = sum * parseFloat($('#discount').val()) / 100;
        var additionalCost =  parseFloat($('#additional-cost').val());
        var all = sum - discount + additionalCost;
        if (sum) {
            $('#order-sum').text(sumOrders().toFixed(2));
        } else {
            $('#order-sum').text("---");
        }
        if (isNaN(discount)) {
            $('#order-discount').text("---");
        } else {
            $('#order-discount').text(discount.toFixed(2));
        }
        if (isNaN(additionalCost)) {
            $('#order-additional').text("---");
        } else {
            $('#order-additional').text(additionalCost.toFixed(2));
        }
        if (isNaN(all)) {
            $('#order-cost').text("---");
        } else {
            $('#order-cost').text(all.toFixed(2));
        }
        $('#submit').prop("disabled", !sum);
    }

    $('#save').click(function () {
        $('.overlay').show();
        $.ajax({
            url: "/api/order/save",
            method: "POST",
            data: JSON.stringify(getData()),
            contentType: "application/json"
        }).done(function () {
            location.href = "/order/details/" + orderId;
        }).fail(function(jqXHR, textStatus) {
            alert("Request failed: " + textStatus);
        });

    });

    $('#submit').click(function () {
        $('.overlay').show();
        $.ajax({
            url: "/api/order/submit",
            method: "POST",
            data: JSON.stringify(getData()),
            contentType: "application/json"
        }).done(function () {
            location.href = "/order/details/" + orderId;
        }).fail(function(jqXHR, textStatus) {
            alert("Request failed: " + textStatus);
        });
    });

    $('.value').blur(function () {
        update();
    });

    update();
});
