var page = 0;
var pic_list;
var max_page = 0;
var daypath = {"room_day":""};
var clock;
var isFirst = 1;
var access_token;
function urlencode (str) {  
    str = (str + '').toString();   

    return encodeURIComponent(str).replace(/!/g, '%21').replace(/'/g, '%27').replace(/\(/g, '%28').  
    replace(/\)/g, '%29').replace(/\*/g, '%2A').replace(/%20/g, '+');  
} 
function updateimg() { 
	$(".img").each(function(){
		var param = {"room":"","path_id":""};
		param.room = $(this).attr("room");
		param.path_id = $(this).attr("id");
		//_hmt.push(['_trackEvent', param.room.split("/",4)[3], param.room.split("/",4)[3] + '刷新']);
		$.getJSON("/query/room_newest_pic.json", param, function(data){
			if(	data.hasOwnProperty("path_id")){
				$("#"+data.path_id).show();
				$("#"+data.path_id).empty();
				$("#"+data.path_id).append("<div class='close_box'>X</div><a onclick=\"_hmt.push(['_trackEvent', '" + param.room.split("/",4)[3] + "', '" + param.room.split("/",4)[3] + "/大图'])\" target='_blank' href='/file/get_pic?method=generate&path="+urlencode(data.path)+"&quality=100&width=1600&height=1200'> <img src='/file/get_pic?method=generate&path="+urlencode(data.path)+"&quality=100&width=320&height=240' alt='Ballade' width='320' height='240'>  </a><div class='desc'>"+data.path.split("/",6)[5]+"</div>");
			}
			else{
				if(isFirst == 1){
					$("#"+param.path_id).remove();
				}
			}
		});
	});
	isFirst = 0;
}
$(document).ready(function(){
	$(".hisday").hide();
	$(".hispage").hide();
	$.getJSON("/query/room_list.json", function(data){
		$("#roomsel").append("<option value='live_pic'>实时图片</option>");
		for(var i=0;i<data.list.length;i++){
			$("#roomsel").append("<option value='"+data.list[i].room+"'>"+data.list[i].room.split("/",4)[3]+"</option>");
			$("#pic").append("<div class='img' id=" + data.list[i].path_id + " room='"+data.list[i].room+"' ></div>");
			$("#"+data.list[i].path_id).hide();
		}  
		access_token = data.access_token;
		$("#pic").sortable();
		
		updateimg();
		clock = setInterval("updateimg()",30000);
	});
	$('#roomsel').change(function(){
		if($(this).val() == 'live_pic'){
			$("#day").empty();
			$("#pic").empty();
			$(".pagelist").empty();
			$(".hisday").hide();
			$(".hispage").hide();
			page = 0;
			max_page = 0;
			isFirst = 1;
			$.getJSON("/query/room_list.json", function(data){
				for(var i=0;i<data.list.length;i++){
					$("#pic").append("<div class='img' id=" + data.list[i].path_id + " room='"+data.list[i].room+"' ></div>");
					$("#"+data.list[i].path_id).hide();
				}  
				$("#pic").sortable();
				
				updateimg();
				clock = setInterval("updateimg()",30000);
			});
		}
		else{
			var roompath = {"room":""};
			roompath.room = $(this).val();
			clearInterval(clock);
			
			$.getJSON("/query/room_day_list.json", roompath, function(data){
				$("#day").empty();
				$("#pic").empty();
				$(".pagelist").empty();
				$(".hisday").show();
				page = 0;
				max_page = 0;
				$("#day").append("<option value='null'></option>");
				for(var i=0;i<data.list.length;i++){  
					$("#day").append("<option value='"+data.list[i]+"'>"+data.list[i].split("/",5)[4]+"</option>");
				}  
			});
		}
		return;
	});
	$('#day').change(function(){
		if($(this).val() == 'null'){
			return;
		}
		daypath.room_day = $(this).val();
		
		_hmt.push(['_trackEvent', '' + daypath.room_day.split("/",6)[3] , '' + daypath.room_day.split("/",6)[3] + '/' + daypath.room_day.split("/",6)[4]]);
		
		$.getJSON("/query/room_day_pic_list.json", daypath, function(data){
			if(data.list.length < 1){
				return;
			}
			$(".hispage").show();
			pic_list = data;
			$("#pic").empty();
			$(".pagelist").empty();
			page = 0;
			max_page = (pic_list.list.length-1)/20;

			for(var i=0;i<=max_page;i++){  
				$(".pagelist").append("<option value='"+i+"'>"+i+"</option>");
			}
			
			for(var i=0;i<(pic_list.list.length<20?pic_list.list.length:20);i++){  
				$("#pic").append("<div class='img'><a target='_blank' href='/file/get_pic?method=generate&path="+urlencode(pic_list.list[i])+"&quality=100&width=1600&height=1200'> <img src='/file/get_pic?method=generate&path="+urlencode(pic_list.list[i])+"&quality=100&width=160&height=120' alt='Ballade' width='320' height='240'>  </a><div class='desc'>"+pic_list.list[i].split("/",6)[5]+"</div></div>");
			}  
		});
	});
	$('.pagelist').change(function(){
		page = $(this).val();
		$('.pagelist').val(page);
		$("#pic").empty();
		for(var i=page*20;i<(pic_list.list.length<(page*20+20)?pic_list.list.length:(page*20+20));i++){  
			$("#pic").append("<div class='img'><a target='_blank' href='/file/get_pic?method=generate&path="+urlencode(pic_list.list[i])+"&quality=100&width=1600&height=1200'> <img src='/file/get_pic?method=generate&path="+urlencode(pic_list.list[i])+"&quality=100&width=160&height=120' alt='Ballade' width='320' height='240'>  </a><div class='desc'>"+pic_list.list[i].split("/",6)[5]+"</div></div>");
		} 
	});
	$('.page').click(function(){
		if( $(this).attr("href") == 'next' ){
			if( page < max_page){
				page++;
			}
			else{
				page++;
			}
		}
		else if( $(this).attr("href") == 'front' ){
			if( page < 1){
				return false;
			}
			page--;
		}
		else {
			return false;
		}
		$('.pagelist').val(page);
		$("#pic").empty();
		for(var i=page*20;i<(pic_list.list.length<(page*20+20)?pic_list.list.length:(page*20+20));i++){  
			$("#pic").append("<div class='img'><a target='_blank' href='/file/get_pic?method=generate&path="+urlencode(pic_list.list[i])+"&quality=100&width=1600&height=1200'> <img src='/file/get_pic?method=generate&path="+urlencode(pic_list.list[i])+"&quality=100&width=160&height=120' alt='Ballade' width='320' height='240'>  </a><div class='desc'>"+pic_list.list[i].split("/",6)[5]+"</div></div>");
		}  
		
		return false;
	});
	$(document).on('click','.close_box',function(){
		$(this).parent().fadeTo(300,0,function(){
			  $(this).remove();
		});
	});
});