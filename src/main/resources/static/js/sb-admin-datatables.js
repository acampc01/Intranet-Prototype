// Call the dataTables jQuery plugin
$(document).ready(function() {
  $('#dataTable').DataTable(
	{
		"pageLength": 9,
		"lengthMenu": [10, 25, 50, 75, 100]
	}	  
  );
});
