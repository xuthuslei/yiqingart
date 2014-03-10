	var access_token = "";
	function getQueryString(name) {
		var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
		var r = window.location.search.substr(1).match(reg);
		if (r != null) return unescape(r[2]); return null;
	}
	function Appendzero(obj)
	{
	 if(obj<10) return "0" +""+ obj;
	 else return obj;
		 
	}

	function getYearMonthDate()
	{
	 var now=new Date();
	 var year=now.getFullYear();
	 var month=now.getMonth()+1;
	 var date=now.getDate();

	 var s=year+"-"+Appendzero(month)+"-"+Appendzero(date);
	 return s;
	}

	function urlencode (str) {  
		str = (str + '').toString();   

		return encodeURIComponent(str).replace(/!/g, '%21').replace(/'/g, '%27').replace(/\(/g, '%28').  
		replace(/\)/g, '%29').replace(/\*/g, '%2A').replace(/%20/g, '+');  
	} 
	function updateimg() { 
		$.ajax({
			type : "get",
			async:false,
			cache:true,
			url : "http://yqartpic.cdn.duapp.com/query/new_pic_list.jsonp",
			dataType : "jsonp",
			jsonp: "callbackparam",//传递给请求处理程序或页面的，用以获得jsonp回调函数名的参数名(默认为:callback)
			jsonpCallback:"jsonpCallback",//自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
			success : function(data){
				for(var i=0;i<data.list.length;i++){  
					var roomid = data.list[i].path_id;
					var room = data.list[i].path.split("/",6)[3];

					if( $("#room"+roomid).length <= 0)
					{
						continue;
					}
					else
					{
						$("#room"+roomid).empty();
						$("#room"+roomid).append("<div class='close_box'>X</div><a onclick=\"_hmt.push(['_trackEvent', '" + room + "', '大图'])\" target='_blank' href='https://pcs.baidu.com/rest/2.0/pcs/thumbnail?access_token="+access_token+"&method=generate&path="+urlencode(data.list[i].path)+"&quality=100&width=1600&height=1200'> <img src='https://pcs.baidu.com/rest/2.0/pcs/thumbnail?access_token="+access_token+"&method=generate&path="+urlencode(data.list[i].path)+"&quality=100&width=320&height=240' alt='Ballade' width='320' height='240'>  </a><div class='desc'>"+data.list[i].path.split("/",6)[5]+"</div>");
					}
				}  
			}
		});

	}
	$(document).ready(function(){
		//var Request = new Object();
		//Request = GetRequest();
		var roompara = getQueryString('room');
		$.ajax({
			type : "get",
			async:false,
			cache:true,
			url : "http://yqartpic.cdn.duapp.com/query/new_pic_list.jsonp",
			dataType : "jsonp",
			jsonp: "callbackparam",//传递给请求处理程序或页面的，用以获得jsonp回调函数名的参数名(默认为:callback)
			jsonpCallback:"jsonpCallback",//自定义的jsonp回调函数名称，默认为jQuery自动生成的随机函数名
			success : function(data){
				access_token = data.access_token;
				for(var i=0;i<data.list.length;i++){  
					$("#pic").append("<div class='img' id='room" + data.list[i].path_id + "' ></div>");
				}  
				$("#pic").sortable();
				
				updateimg();
				setInterval("updateimg()",30000);
			}
		});
	});
	$(document).on('click','.close_box',function(){
		$(this).parent().fadeTo(300,0,function(){
			  $(this).remove();
		});
	});
	var _bdhmProtocol = (("https:" == document.location.protocol) ? " https://" : " http://");
    document.write(unescape("%3Cscript src='" + _bdhmProtocol + "hm.baidu.com/h.js%3F53ef1ad966caadac8e0fe238fd90c5ba' type='text/javascript'%3E%3C/script%3E"));
