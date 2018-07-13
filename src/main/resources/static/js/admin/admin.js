$(document).ready(function () {

	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");

	$("a#refuse").on('click', function(){
		$.ajax({
			type: "DELETE",
			url: "/admin/refuse/".concat($(this).find("input").val()),
			cache: false,
			beforeSend: function(xhr) {
				xhr.setRequestHeader(header, token);
			},
			timeout: 6000,
			success: function (data) {
				alertify.error('User Refused!');
				setTimeout(function(){
					location.reload();
			    }, 1000);
			},
			error: function (e) {}
		});
	});
	
	$("a#accept").on('click', function(){
		$.ajax({
			type: "PUT",
			url: "/admin/accept/".concat($(this).find("input").val()),
			cache: false,
			beforeSend: function(xhr) {
				xhr.setRequestHeader(header, token);
			},
			timeout: 6000,
			success: function (data) {
				alertify.success('User Accepted!');
				setTimeout(function(){
					location.reload();
			    }, 1000);
			},
			error: function (e) {}
		});
	});
	
	$("a#delete").on('click', function(){
		$.ajax({
			type: "DELETE",
			url: "/admin/delete/".concat($(this).find("input").val()),
			cache: false,
			beforeSend: function(xhr) {
				xhr.setRequestHeader(header, token);
			},
			timeout: 6000,
			success: function (data) {
				alertify.success('User Deleted!');
				setTimeout(function(){
					location.reload();
			    }, 1000);
			},
			error: function (e) {}
		});
	});
	
});