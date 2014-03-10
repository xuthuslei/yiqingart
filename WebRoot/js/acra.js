function get_time_str(time){
	var d = new Date();
	d.setTime(time);
	return d.toLocaleString();
} 
function update_table(data) {  
	
		$("#phone_acra_table").find("[id]").remove();
		
		for(var i=0;i<data.length;i++){
			var idx = "acra"+data[i].idx;
			$("#phone_acra_table").append("<tr id='"+idx+"' idx="+ data[i].idx +"></tr>");
			
			$("#"+idx).append("<td >"+get_time_str(data[i].time)+"</td>");
			$("#"+idx).append("<td >"+data[i].phone_id+"</td>");
			$("#"+idx).append("<td >"+data[i].APP_VERSION_NAME+"</td>");
			$("#"+idx).append("<td >"+data[i].SHARED_PREFERENCES+"</td>");
			$("#"+idx).append("<td >"+data[i].STACK_TRACE+"</td>");
			$("#"+idx).append("<td >"+data[i].ANDROID_VERSION+"</td>");
		}  
	
}

$(document).ready(function(){
	$.getJSON("/acra/query", function(data){
		update_table(data);
	});
	$('#phone_id').change(function(){
		if($(this).val() != 0){
			var para = {"phone_id":$(this).val()};
			$.getJSON("/acra/query", para, function(data){
				update_table(data);
			});
		}
		else{
			$.getJSON("/acra/query", function(data){
				update_table(data);
			});
		}
	});
});