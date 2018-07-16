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
					error: function (e) {
						
					}
				});
			},
			minLength: 1,
			select: function( event, ui ){
				$("#shareContent").append("<div><div class='border border-dark rounded m-2 p-1 d-inline-flex' id='shareContent'>" + ui.item.value + "</div><a class='text-danger' href='#'><i class='fa fa-times' aria-hidden='true'></i></a></div>");
			}
		});
		$( "#searchUser" ).autocomplete( "option", "appendTo", "#sUf" );
		    
});