$(function(){
    $('#placesIds').multiSelect()

    function hashCode(str) {
        console.log(str);
        var hash = 0, i, chr, len;
        if (str.length === 0) return hash;
        for (i = 0, len = str.length; i < len; i++) {
            chr   = str.charCodeAt(i);
            hash  = ((hash << 5) - hash) + chr;
            hash |= 0;
        }
        return hash;
    }

    var ctx = $("#ctx")[0].getContext("2d");
    var votingCount = [];
    var colors = [];
    for(var i = 0; i < restaurantNames.length; i++){
        votingCount.push(votingData[i].length);
        var hash = hashCode(restaurantNames[i]);
        var green = hash % 256;
        var blue = Math.floor(hash / 256) % 256;
        var red = Math.floor(hash / 16384) % 256;
        console.log(hash);
        console.log('rgba(' + red + ',' + green + ',' + blue + ',0.5)');
        colors.push('rgba(' + red + ',' + green + ',' + blue + ',0.7)');
    }
    var data = {
        labels: restaurantNames,
        datasets: [
            {
                data: votingCount,
                backgroundColor: colors,
                hoverBackgroundColor: colors,
            }
        ]
    };
    var chart = new Chart(ctx, {
        type: 'pie',
        data: data,
        options: {
			tooltips: {
				callbacks: {
					label: function(v) {
						return votingData[v.index];
					}
				}
			}
		}
    });
});
