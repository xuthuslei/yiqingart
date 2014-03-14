function update_table() {  
	$.getJSON("/query/admin_data.json", function(data){
		update_group_list(data.work_group_config);
		update_place_list(data.work_place_cfg);
		//update_phone_cfg(data);
		update_phone_state(data);
	});
}
function update_group_list(data) {  
	$("#work_group_table").find("[id]").remove();
	
	for(var i=0;i<data.length;i++){
		var groupid = "group"+data[i].workgroupid;
		$("#work_group_table").append("<tr id='"+groupid+"' idx="+ data[i].workgroupid +"></tr>");
		
		$("#"+groupid).append("<td class='group_name'><input type='text' value='"+data[i].group_name+"' /></td>");
	
			
		$("#"+groupid).append("<td class='group_time'></td><td class='group_time_p'><select class='group_freq'></select></td><td><input type='checkbox' class='grp_w_1' /></td><td><input type='checkbox' class='grp_w_2' /></td><td><input type='checkbox' class='grp_w_3' /></td><td><input type='checkbox' class='grp_w_4' /></td><td><input type='checkbox' class='grp_w_5' /></td><td><input type='checkbox' class='grp_w_6' /></td><td><input type='checkbox' class='grp_w_7' /></td><td><select class='group_imgsize'></select></td><td><select class='group_target'></select></td><td  class='group_opt'></td>");
		
		
		var temp_str = '<input type="text" class="grp_bh" value="'+data[i].beginHour+'" />:<input type="text" class="grp_bm" value="'+data[i].beginMinute+'" />-<input type="text" class="grp_eh" value="'+data[i].endHour+'" />:<input type="text" class="grp_em" value="'+data[i].endMinute+'" />';
		$("#"+groupid).children(".group_time").append(temp_str);
		
		temp_str = '<option value="15" >15√Î÷”</option><option value="30" >30√Î÷”</option><option value="60" >1∑÷÷”</option><option value="120" >2∑÷÷”</option><option value="300" >5∑÷÷”</option>';
		$("#"+groupid).find(".group_freq").append(temp_str);
		$("#"+groupid).find(".group_freq").val(data[i].shotFreq);
		
		temp_str = '<option value="500" >500</option><option value="1000" >1000</option><option value="1500" >1500</option><option value="2000" >2000</option>';
		$("#"+groupid).find(".group_imgsize").append(temp_str);
		$("#"+groupid).find(".group_imgsize").val(data[i].imgsize);
		
		temp_str = '<option value="0" >Õ¯≈Ã</option><option value="1" >Õ¯’æ</option>';
		$("#"+groupid).find(".group_target").append(temp_str);
		$("#"+groupid).find(".group_target").val(data[i].target);
	
		if(1==data[i].week1) $("#"+groupid).find(".grp_w_1").attr("checked", true);
		if(1==data[i].week2) $("#"+groupid).find(".grp_w_2").attr("checked", true);
		if(1==data[i].week3) $("#"+groupid).find(".grp_w_3").attr("checked", true);
		if(1==data[i].week4) $("#"+groupid).find(".grp_w_4").attr("checked", true);
		if(1==data[i].week5) $("#"+groupid).find(".grp_w_5").attr("checked", true);
		if(1==data[i].week6) $("#"+groupid).find(".grp_w_6").attr("checked", true);
		if(1==data[i].week7) $("#"+groupid).find(".grp_w_7").attr("checked", true);
		$("#"+groupid).children(".group_opt").append("<a class='grp_opt' href='save' >±£¥Ê</a>|<a class='grp_opt' href='del' >…æ≥˝</a>");
	}  
	groupid = "group_new";
	$("#work_group_table").append("<tr id='"+groupid+"'></tr>");
	$("#"+groupid).hide();
	$("#"+groupid).append("<td class='group_name'><input type='text' /></td>");
	$("#"+groupid).append("<td class='group_time'></td><td><select class='group_freq'></select></td><td><input type='checkbox' class='grp_w_1' /></td><td><input type='checkbox' class='grp_w_2' /></td><td><input type='checkbox' class='grp_w_3' /></td><td><input type='checkbox' class='grp_w_4' /></td><td><input type='checkbox' class='grp_w_5' /></td><td><input type='checkbox' class='grp_w_6' /></td><td><input type='checkbox' class='grp_w_7' /></td><td><select class='group_imgsize'></select></td><td><select class='group_target'></select></td><td  class='group_opt'></td>");
	
	
	var temp_str = '<input class="grp_bh" type="text" />:<input class="grp_bm" type="text" />-<input class="grp_eh" type="text" />:<input class="grp_em" type="text" />';
	$("#"+groupid).children(".group_time").append(temp_str);
	
	temp_str = '<option value="15" >15√Î÷”</option><option value="30" >30√Î÷”</option><option value="60" >1∑÷÷”</option><option value="120" >2∑÷÷”</option><option value="300" >5∑÷÷”</option>';
	$("#"+groupid).find(".group_freq").append(temp_str);
	
	temp_str = '<option value="500" >500</option><option value="1000" >1000</option><option value="1500" >1500</option><option value="2000" >2000</option>';
	$("#"+groupid).find(".group_imgsize").append(temp_str);
	$("#"+groupid).find(".group_imgsize").val(2000);
	
	temp_str = '<option value="0" >Õ¯≈Ã</option><option value="1" >Õ¯’æ</option>';
	$("#"+groupid).find(".group_target").append(temp_str);
	$("#"+groupid).find(".group_target").val(1);
	
	$("#"+groupid).children(".group_opt").append("<a class='grp_opt' href='new' >±£¥Ê</a>");
}
function update_place_list(data) {  
	$("#work_place_table").find("[id]").remove();
	
	for(var i=0;i<data.length;i++){
		var placeid = "place"+data[i].workplaceid;
		$("#work_place_table").append("<tr id='"+placeid+"' idx="+ data[i].workplaceid +"></tr>");
		
		$("#"+placeid).append("<td><input type='text'  class='place_name' value='"+data[i].place_name+"' /></td>");
		$("#"+placeid).append("<td><a class='place_opt' href='save' >±£¥Ê</a>|<a class='place_opt' href='del' >…æ≥˝</a></td>");
	}  
	placeid = "place_new";
	$("#work_place_table").append("<tr id='"+placeid+"'></tr>");
	$("#"+placeid).hide();
	$("#"+placeid).append("<td><input  class='place_name' type='text'/></td>");
	$("#"+placeid).append("<td><a class='place_opt' href='new' >±£¥Ê</a></td>");
}
//function update_phone_cfg(data) {  
//	$("#phone_cfg_table").find("[id]").remove();
//	var place_list = "";
//	var group_list = "";
//	var phone_list = "";
//	for(var i=0;i<data.work_place_cfg.length;i++){
//		place_list = place_list + "<option value='"+data.work_place_cfg[i].workplaceid+"' >"+data.work_place_cfg[i].place_name+"</option>";
//	}
//	for(var i=0;i<data.work_group_config.length;i++){
//		group_list = group_list + "<option value='"+data.work_group_config[i].workgroupid+"' >"+data.work_group_config[i].group_name+"</option>";
//	}
//	
//	for(var i=0;i<data.phone_cfg.length;i++){
//		var phoneid = "phone"+data.phone_cfg[i].phone_id;
//		$("#phone_cfg_table").append("<tr id='"+phoneid+"' idx="+ data.phone_cfg[i].phone_id +"></tr>");
//		$("#"+phoneid).append("<td>"+data.phone_cfg[i].phone_id+"</td>");
//		$("#"+phoneid).append("<td><select class='group_id'>"+group_list+"</select></td>");
//		$("#"+phoneid).append("<td><select class='place_id'>"+place_list+"</select></td>");
//		$("#"+phoneid).find(".group_id").val(data.phone_cfg[i].workgroupid);
//		$("#"+phoneid).find(".place_id").val(data.phone_cfg[i].workplaceid);
//		$("#"+phoneid).append("<td><a class='phone_opt' href='save' >±£¥Ê</a>|<a class='phone_opt' href='del' >…æ≥˝</a></td>");
//	}  
//	
//	for(var i=1;i<=20;i++){
//		if($("#phone"+i).length < 1){
//			phone_list = phone_list + "<option value='"+i+"' >"+i+"</option>";
//		}
//	}
//	phoneid = "phone_new";
//	$("#phone_cfg_table").append("<tr id='"+phoneid+"'></tr>");
//	$("#"+phoneid).hide();
//	$("#"+phoneid).append("<td><select class='phone_id'>"+phone_list+"</select></td>");
//	$("#"+phoneid).append("<td><select class='group_id'>"+group_list+"</select></td>");
//	$("#"+phoneid).append("<td><select class='place_id'>"+place_list+"</select></td>");
//	$("#"+phoneid).append("<td><a class='phone_opt' href='new' >±£¥Ê</a></td>");
//}
function get_time_str(time){
	var d = new Date();
	d.setTime(time);
	return d.toLocaleString();
} 
function update_phone_state(data) {  
	$("#phone_state_table").find("[id]").remove();
	var place_list = "<option value='0' >Œ¥≈‰÷√</option>";
	var group_list = "<option value='0' >Œ¥≈‰÷√</option>";
	//var phone_list = "";
	for(var i=0;i<data.work_place_cfg.length;i++){
		place_list = place_list + "<option value='"+data.work_place_cfg[i].workplaceid+"' >"+data.work_place_cfg[i].place_name+"</option>";
	}
	for(var i=0;i<data.work_group_config.length;i++){
		group_list = group_list + "<option value='"+data.work_group_config[i].workgroupid+"' >"+data.work_group_config[i].group_name+"</option>";
	}
	
	for(var i=0;i<data.access_log.length;i++){
		var phoneid = "phone"+data.access_log[i].phone_id;
		var hasConfig = 0;
		var config_version = 0;
		$("#phone_state_table").append("<tr id='"+phoneid+"' idx="+ data.access_log[i].phone_id +" title='"+data.access_log[i].msg+"'></tr>");
		$("#"+phoneid).append("<td>"+data.access_log[i].phone_id+"</td>");
		$("#"+phoneid).append("<td><select class='place_id'>"+place_list+"</select></td>");
		$("#"+phoneid).append("<td><select class='group_id'>"+group_list+"</select></td>");
		for(var j=0;j<data.phone_cfg.length;j++){
			if(data.phone_cfg[j].phone_id == data.access_log[i].phone_id){
				$("#"+phoneid).find(".group_id").val(data.phone_cfg[j].workgroupid);
				$("#"+phoneid).find(".place_id").val(data.phone_cfg[j].workplaceid);
				config_version = data.phone_cfg[j].config_version;
				hasConfig = 1;
			}
		}
		
		$("#"+phoneid).append("<td>"+get_time_str(data.access_log[i].time)+"</td>");
		//$("#"+phoneid).append("<td>"+get_time_str(data.access_log[i].config_version)+"</td>");
		$("#"+phoneid).append("<td>"+data.access_log[i].version+"</td>");
		$("#"+phoneid).append("<td>"+data.access_log[i].sendbytes+"</td>");
		$("#"+phoneid).append("<td>"+data.access_log[i].startcount+"</td>");
		$("#"+phoneid).append("<td><a class='phone_opt' href='save' >±£¥Ê</a>|<a class='phone_opt' href='del' >…æ≥˝</a></td>");
		//$("#"+phoneid).tooltip({ content: data.access_log[i].msg });
		
		var d = new Date();
		if(0==hasConfig){
			$("#"+phoneid).addClass("phone_notcfg");
		}
		else if( d.getTime() > (Number(data.access_log[i].time) + 700000) ){
			$("#"+phoneid).addClass("phone_notonline");
		}
		else if( data.access_log[i].config_version != config_version){
			$("#"+phoneid).addClass("phone_oldcfg");
		}
		else{
			$("#"+phoneid).addClass("phone_normal");
		}
	}  
}
$(document).ready(function(){
	update_table();
	$(document).tooltip();
	
	$(document).on('click','.grp_opt',function(){
		var opt = $(this).attr("href");
		if( opt == "more"){
			$("#group_new").show();
			return false;
		}
		
		var idx = $(this).parent().parent().attr("idx");
		if( opt == "del"){
			var params = {"workgroupid":idx};
			$.getJSON("/delete/work_group.json", params, function(data){
				update_table();
			});
			return false;
		}
		
		var groupid;
		var params = {"shotFreq":0, "beginHour":0, "beginMinute":0, "endHour":0, "endMinute":0, "week1":0, "week2":0, "week3":0, "week4":0, "week5":0, "week6":0, "week7":0};
		
		if( opt == "save"){
			groupid = "group"+idx;
		}
		else{
			groupid = "group_new";
		}
			
		params.shotFreq = $("#"+groupid).find(".group_freq").val();
		params.beginHour = $("#"+groupid).find(".grp_bh").val();
		params.beginMinute = $("#"+groupid).find(".grp_bm").val();
		params.endHour = $("#"+groupid).find(".grp_eh").val();
		params.endMinute = $("#"+groupid).find(".grp_em").val();
		params.imgsize = $("#"+groupid).find(".group_imgsize").val();
		params.target = $("#"+groupid).find(".group_target").val();
		
		if( $("#"+groupid).find(".grp_w_1").is(':checked'))params.week1 = 1;
		if( $("#"+groupid).find(".grp_w_2").is(':checked'))params.week2 = 1;
		if( $("#"+groupid).find(".grp_w_3").is(':checked'))params.week3 = 1;
		if( $("#"+groupid).find(".grp_w_4").is(':checked'))params.week4 = 1;
		if( $("#"+groupid).find(".grp_w_5").is(':checked'))params.week5 = 1;
		if( $("#"+groupid).find(".grp_w_6").is(':checked'))params.week6 = 1;
		if( $("#"+groupid).find(".grp_w_7").is(':checked'))params.week7 = 1;
		
		if( opt == "save"){
			params.workgroupid = idx;
			params.group_name = $("#"+groupid).children(".group_name").children().val();
		
			
			$.getJSON("/modify/work_group.json", params, function(data){
				update_table();
			});
		}
		else{
			params.group_name = $("#"+groupid).children(".group_name").children().val();
			$.getJSON("/insert/work_group.json", params, function(data){
				update_table();
			});
		}
		
		return false;
	});
	$(document).on('click','.place_opt',function(){
		var opt = $(this).attr("href");
		if( opt == "more"){
			$("#place_new").show();
			return false;
		}
		
		var idx = $(this).parent().parent().attr("idx");
		if( opt == "del"){
			var params = {"workplaceid":idx};
			$.getJSON("/delete/WORK_PLACE.json", params, function(data){
				update_table();
			});
			return false;
		}
		
		var placeid;
		var params = {"place_name":""};
		
		if( opt == "save"){
			placeid = "place"+idx;
		}
		else{
			placeid = "place_new";
		}
			
		if( opt == "save"){
			params.workplaceid = idx;
			
			params.place_name = $("#"+placeid).find(".place_name").val();
	
			$.getJSON("/modify/WORK_PLACE.json", params, function(data){
				update_table();
			});
		}
		else{
			params.place_name = $("#"+placeid).find(".place_name").val();
			$.getJSON("/insert/WORK_PLACE.json", params, function(data){
				update_table();
			});
		}
		
		return false;
	});
	$(document).on('click','.phone_opt',function(){
		var opt = $(this).attr("href");
//		if( opt == "more"){
//			$("#phone_new").show();
//			return false;
//		}
		
		var idx = $(this).parent().parent().attr("idx");
		if( opt == "del"){
			var params = {"phone_id":idx};
			$.getJSON("/delete/PHONE_CFG.json", params, function(data){
				$.getJSON("/delete/ACCESS_LOG.json", params, function(data){
					update_table();
				});
			});
			return false;
		}
		if( opt == "reset"){
			$.getJSON("/modify/PHONE_RESET.json",  function(data){
				update_table();
			});
			return false;
		}
		
		var phoneid;
		var params = {"phone_id":""};
		
		if( opt == "save"){
			phoneid = "phone"+idx;
		}
		else{
			phoneid = "phone_new";
		}
			
		if( opt == "save"){
			params.workplaceid = $("#"+phoneid).find(".place_id").val();
			params.workgroupid = $("#"+phoneid).find(".group_id").val();
			params.phone_id = idx;
	
			$.getJSON("/modify/PHONE_CFG.json", params, function(data){
				update_table();
			});
		}
		else{
			params.workplaceid = $("#"+phoneid).find(".place_id").val();
			params.workgroupid = $("#"+phoneid).find(".group_id").val();
			params.phone_id =  $("#"+phoneid).find(".phone_id").val();
			$.getJSON("/insert/PHONE_CFG.json", params, function(data){
				update_table();
			});
		}
		
		return false;
	});
	setInterval("update_table()",30000);
});