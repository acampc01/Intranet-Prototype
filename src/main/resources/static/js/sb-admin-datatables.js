$(document).ready(function() {
	
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");

	$('#dataTable').DataTable({
		"language": {
			"emptyTable": "Oops, there are no files to show you."
		},
		"order": [],
		"columnDefs": [
			{ "width": "5%", "targets": 1 },
			{ "width": "45%", "targets": 0 }
		],
		"pageLength": 25,
		"lengthMenu": [5, 25, 50, 100]
	});
  
        $.contextMenu({
            selector: '#fila', 
            items: {
                "edit": {
                	name: "Edit", 
                	icon: "edit",
                	callback: function(key, opt, e) {
               		
                	}
                },
                "share": {
               		name: "Share",
               		icon: "fa-share-alt",
                	callback: function(key, opt, e) {
		                var inp = $(this).find("input").val();
						$("#dTs").val(inp);
						$.ajax({
							type: "GET",
							url: "/user/file/shared/users/".concat($("#dTs").val()),
							beforeSend: function(xhr) {
								xhr.setRequestHeader(header, token);
							},
							success: function( data ) {
								$("#sharedWith").empty();
								if($.trim(data)){
									var emails = "Shared with";
									$.each(data, function(e, i) {
										emails = emails.concat(" " + i.email + ",");
									});
									emails = emails.substring(0, emails.length - 1);
									$("#sharedWith").append("<small>" + emails + "</small>");
								}
							},
							error: function (e) {}
						});
               			$("#shareModal").modal('toggle');
                	}
                },
                "download": {
               		name: "Download",
               		icon: "fa-download",
                	callback: function(key, opt, e) {
                		window.location="/user/download/folder/" + $(this).find("input").val();
                	}
                },
                "delete": {
                	name: "Delete",
                	icon: "delete",
                	callback: function(key, opt, e) {
               			var data = $(this).find("input").val();
						var msg = alertify.error('Click here to delete', 5);
						msg.callback = function (isClicked) {
							if(isClicked)
								$.ajax({
									type: "DELETE",
									url: "/file/delete/".concat(data),
									cache: false,
									beforeSend: function(xhr) {
										xhr.setRequestHeader(header, token);
									},
									timeout: 6000,
									success: function (data) {
										alertify.success('Resource Deleted!');
										setTimeout(function(){
											location.reload();
									    }, 1000);
									},
									error: function (e) {}
								});
							else
								alertify.error('Not Deleted!');
						};
                	}
                }
   				/*"sep":"",
                "quit": {
                	name: "Quit",
                	icon: function(){
                    	return 'context-menu-icon context-menu-icon-quit';
                	}
                }*/
            }
        });

        $('#fila').on('click', function(e){
            console.log('clicked', this);
        });  

});