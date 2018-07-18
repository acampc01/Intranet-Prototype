$(document).ready(function () {
	
		var token = $("meta[name='_csrf']").attr("content");
		var header = $("meta[name='_csrf_header']").attr("content");

		$("#searchFiles").autocomplete({
			source: function( request, response ) {
				$.ajax({ 
					type: "POST",
					url: "/user/autocomplete",
					data: $("#searchFiles").val(),
					contentType: "text/plain",
					beforeSend: function(xhr) {
						xhr.setRequestHeader(header, token);
					},
					timeout: 6000,
					success: function (data) {
						$("#search-list").empty();
						if(Object.keys(data).length != 0){
							$.each(data, function(index, item) {
								var append = "<a href='/user/files/" + item[1] + "' class='list-group-item list-group-item-action flex-column align-items-start'><div class='d-flex w-100 justify-content-between'> <h5 class='mb-1'>" + item[0] + "</h5> <small>" + item[2] + "</small></div><p class='mb-1 d-inline'>File location: " + item[3] + "</p><p class='ml-3 mb-1 d-inline'>Owner: " + item[4] + "</p>";
	       						
	       						if(item.length == 6)
	       							append += "<p class='ml-3 mb-1 d-inline'>Coincidence Pages: " + item[5] + "</p>";
	       							
	       						append+="</a>";
	       						
	       						$("#search-list").append(append);
	       					});
       					}else{
       						$("#search-list").append("<p>Oops, couldnt find any file.</p>");
       					}
					},
					error: function (e) {

					}
				});
			},
			minLength: 3
		});
		
		$("#clear").click(function(event){
			event.preventDefault();
			$("#search-list").empty();
			$("#searchFiles").val('');
		});
		
		$("#searchUser").autocomplete({
			source: function( request, response ) {
				$.ajax({ 
					type: "POST",
					url: "/user/autocomplete/name",
					contentType: "text/plain",
					dataType: "json",
					data: request.term,
					beforeSend: function(xhr) {
						xhr.setRequestHeader(header, token);
					},
					success: function( data ) {
						response($.map(data, function(v,i){
							return {
						    	label: v.name + " <" + v.email + ">",
						    	value: v.email
							};
						}));
		          	},
					error: function (e) { }
				});
			},
			minLength: 1,
			select: function( event, ui ){
				$("#shareContent").append("<div id='dataU'><div class='border border-dark rounded m-2 p-1 d-inline-flex' id='shareContent'><span id='uE'>" + ui.item.value + "</span></div><a href='#' id='removeU' class='text-danger'><i class='fa fa-times' aria-hidden='true'></i></a></div>");
				$("a#removeU").on('click', function(event){
					event.preventDefault();
				    $(this).parent().remove();
				});
				$("#searchUser").val('');
				return false;
			}
		});
		$("#searchUser").autocomplete( "option", "appendTo", "#sUf" );
		
		$("a#removeAll").on('click', function(event){
			event.preventDefault();
			$("#shareContent").empty();
		});
		
		$("a#addAll").on('click', function(event){
			event.preventDefault();
			$.ajax({ 
				type: "POST",
				url: "/user/autocomplete/name",
				contentType: "text/plain",
				dataType: "json",
				data: "empty",
				beforeSend: function(xhr) {
					xhr.setRequestHeader(header, token);
				},
				success: function( data ) {
					$("#shareContent").empty();
					$.map(data, function(v,i){
						$("#shareContent").append("<div id='dataU'><div class='border border-dark rounded m-2 p-1 d-inline-flex' id='shareContent'><span id='uE'>" + v.email + "</span></div><a href='#' id='removeU' class='text-danger'><i class='fa fa-times' aria-hidden='true'></i></a></div>");
						$("a#removeU").on('click', function(event){
							event.preventDefault();
						    $(this).parent().remove();
						});
						$("#searchUser").val('');
					});
	          	},
				error: function (e) { }
			});
		});
		
		$("a#letsShare").on('click', function(event){
			event.preventDefault();
			
			var elems = $("[id=uE]");
			var emails = [];
			
			$.each(elems, function(i,e){
				emails.push(e.firstChild.data);
			});
			
			$.ajax({
				type: "PUT",
				url: "/user/share/files/".concat($("#dTs").val()),
				data: {
					emails
				},
				beforeSend: function(xhr) {
					xhr.setRequestHeader(header, token);
				},
				success: function( data ) {
					alertify.success('Shared Correctly!');
	          	},
				error: function (e) {
					alertify.error('Error Sharing!');
				}
			});
			
			$("#shareModal").modal('hide');
		});
});

/*$.each(arr, function(i,e){
	console.log(e.firstChild.data);
});*/