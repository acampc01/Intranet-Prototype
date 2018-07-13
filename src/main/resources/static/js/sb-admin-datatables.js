$(document).ready(function() {
  $('#dataTable').DataTable(
	{
		"language": {
		      "emptyTable": "Oops, there are no files to show you."
		},
		"order": [],
		"columnDefs": [
			{ "width": "5%", "targets": 1 },
			{ "width": "55%", "targets": 0 },
			{ "width": "10%", "targets": 4 }
		],
		"pageLength": 25,
		"lengthMenu": [25, 50, 100]
	}	  
  );
});