$(document).ready(function () {

	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");

	$("#upload_files").on('click', function(e){
		e.preventDefault();
		$('#progress').show();
		$("#uploadFiles:hidden").trigger('click');
	});

	$("#uploadFiles").on('change', function(){
		var root = $("#folder").val();
	
		$.ajax({
			type: "POST",
			enctype: 'multipart/form-data',
			url: "/user/upload/files/".concat(root),
			data: new FormData($('#fileUploadForm')[0]),
			cache: false,
			contentType: false,
			processData: false,
			async: true,
			beforeSend: function(xhr) {
				xhr.setRequestHeader(header, token);
				//xhr.upload.addEventListener('progress', progress, false);
			},
			xhr: function (){
				var jqXHR = null;
                if ( window.ActiveXObject ) {
                	jqXHR = new window.ActiveXObject( "Microsoft.XMLHTTP" );
               	} else {
                    jqXHR = new window.XMLHttpRequest();
                }
                
                //Upload progress
                jqXHR.upload.addEventListener( "progress", function ( evt ) {
					if ( evt.lengthComputable ) {
                    	var percentComplete = Math.round( (evt.loaded * 100) / evt.total );
                    	
                    	//Do something with upload progress
                        console.log( 'Uploaded percent', percentComplete );
                        $('#progressBar').css('width', percentComplete+'%');
                    }
           		}, false );
                return jqXHR;
            },
			timeout: 600000,
			success: function (data) {
				alertify.success('Files Uploaded!');
				setTimeout(function(){
					location.reload();
			    }, 2000);
			},
			error: function (e) {}
		});
	});

	$("#newFolderBtn").click(function (event) {
		if( $("#nameNewFolder").val() !== ""){
			event.preventDefault();
			var root = $("#folder").val();
			
			$.ajax({
				type: "POST",
				url: "/user/create/folder/".concat(root),
				data: $("#nameNewFolder").val(),
				contentType: "text/plain",
				beforeSend: function(xhr) {
					xhr.setRequestHeader(header, token);
				},
				timeout: 600000,
				success: function (data) {
					$("#nameNewFolder").val('');
					$('#folderModal').modal('toggle');
					alertify.success('Folder Created!');
					setTimeout(function(){
						location.reload();
				    }, 1000);
					
				},
				error: function (e) {
					$('#folderModal').modal('toggle');
					alertify.error('Error Creating!');
				}
			});
		}   
	});

});