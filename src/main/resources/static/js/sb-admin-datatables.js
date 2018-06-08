$(document).ready(function() {
  $('#dataTable').DataTable(
	{
		"order": [],
//		"order": [[ 3, "desc" ]],
		"columnDefs": [
			{ "width": "5%", "targets": 1 },
			{ "width": "60%", "targets": 0 }
		],
		"pageLength": 25,
		"lengthMenu": [25, 50, 100]
	}	  
  );
});
