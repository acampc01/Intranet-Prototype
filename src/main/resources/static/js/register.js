$(document).ready(function () {
	
	function validateEmail(){
		var email = $("#email")[0];
		
		if(email.value.split("@")[1] !== "drotium.com"){
			email.setCustomValidity("Email should be a drotium one.");
		} else {
			email.setCustomValidity('');
		}
	}
	
	function validatePassword(){
		var pass = $("#password")[0];
		var passC = $("#confirmPassword")[0];

		if(pass.value !== passC.value) {
			passC.setCustomValidity("Passwords Don't Match");
		} else {
			passC.setCustomValidity('');
		}
	}
	
	$("#email").change(function() {
		validateEmail();
	});
	
	$("#password").change(function() {
		validatePassword();
	});
	
	$("#confirmPassword").keyup(function() {
		validatePassword();
	});
});