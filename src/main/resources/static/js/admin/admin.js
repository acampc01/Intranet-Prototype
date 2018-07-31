$(document).ready(function () {

	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");

	$("a#editU").on('click', function(event){
		event.preventDefault();
		var inp = $(this).find("input").val();
		$("#uTc").val(inp);
		$.ajax({
			type: "GET",
			url: "/user/data/".concat(inp),
			beforeSend: function(xhr) {
				xhr.setRequestHeader(header, token);
			},
			success: function( data ) {
				$.each(data, function(e, i){
					$("#userName").val(i.name);
					$("#userLastName").val(i.lname);
					$("#userEmail").val(i.email);
					$("#userActive").text(i.active);
					$("#userCreation").text(i.creation);
					$("#userRole").text(i.role);
						
					if(i.update !== null)
						$("#userConnect").text(i.update);
					else
						$("#userConnect").text('-');
										
					$("#editModal").modal('toggle');
				});
			},
			error: function (e) {}
		});
	});

	$("#uedit").click(function (event) {
       	event.preventDefault();
		if( $("#userName").val() !== "" && $("#userLastName").val() !== "" && $("#userEmail").val() !== "" ){
			var datos = [$("#userName").val(),$("#userLastName").val(),$("#userEmail").val()]
			$.ajax({
				type: "POST",
				url: "/user/edit/".concat( $("#uTc").val() ),
				data: {
					datos
				},
				beforeSend: function(xhr) {
					xhr.setRequestHeader(header, token);
				},
				success: function (data) {
					$('#editModal').modal('toggle');
					alertify.success('User Edited!');
					setTimeout(function(){
						location.reload();
				    }, 1000);
				},
				error: function (e) {
					$('#editModal').modal('toggle');
					alertify.error('Error Editing!');
				}
			});
		}
	});
	
	$("#userEmail").change(function() {
		var email = $("#userEmail")[0];
		
		if(email.value.split("@")[1] !== "drotium.com"){
			email.setCustomValidity("Email should be a drotium one.");
		} else {
			email.setCustomValidity('');
		}
	});
	
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