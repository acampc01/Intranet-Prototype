$(document).ready(function () {

	$("#upload_files").on('click', function(e){
		e.preventDefault();
		$("#uploadFiles:hidden").trigger('click');
	});

	$("#uploadFiles").on('change', function(){
		var token = $("meta[name='_csrf']").attr("content");
		var header = $("meta[name='_csrf_header']").attr("content");
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
			},
			timeout: 600000,
			success: function (data) {
				alertify.success('Files Uploaded!');
				setTimeout(function(){
					location.reload();
			    }, 1000);
			},
			error: function (e) {}
		});
	});

	$("#newFolderBtn").click(function (event) {
		if( $("#nameNewFolder").val() !== ""){
			event.preventDefault();

			var token = $("meta[name='_csrf']").attr("content");
			var header = $("meta[name='_csrf_header']").attr("content");
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
				error: function (e) {}
			});
		}   
	});

});