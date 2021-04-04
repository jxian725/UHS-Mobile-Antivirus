(function ($) {
    "use strict";
    var input = $('.validate-input .input100');
    $('.validate-form').on('submit', function () {
        var check = true;
        for (var i = 0; i < input.length; i++) {
            if (validate(input[i]) == false) {
                showValidate(input[i]);
                check = false;
            }
        }
        return check;
    });
    $('.validate-form .input100').each(function () {
        $(this).focus(function () {
            hideValidate(this);
        });
    });
    function validate(input) {
        if ($(input).attr('type') == 'email' || $(input).attr('name') == 'email') {
            if ($(input).val().trim().match(/^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{1,5}|[0-9]{1,3})(\]?)$/) == null) {
                return false;
            }
        } else {
            if ($(input).val().trim() == '') {
                return false;
            }
        }
    }
    function showValidate(input) {
        var thisAlert = $(input).parent();
        $(thisAlert).addClass('alert-validate');
    }
    function hideValidate(input) {
        var thisAlert = $(input).parent();
        $(thisAlert).removeClass('alert-validate');
    }
    $('.upper').keyup(function() {
        this.value = this.value.toUpperCase();
    });
})(jQuery);

function closeModal(){
	document.getElementById("myModal").style.display = "none";
}

function closeStorage(){
	document.getElementById("storageModal").style.display = "none";
}

function register(){
	document.getElementById("myModal").style.display = "block";
}

function addAccount(){
	var uid = document.getElementById("reg-userid").value;
	var pw1 = document.getElementById("reg-pw1").value;
	var pw2 = document.getElementById("reg-pw2").value;
	if (pw1==""||pw2==""){
		alert("Password cannot be empty!");
	} else if (pw1!=pw2){
		alert("Password does not match!");
		pw1 = "";
		pw2 = "";
	} else {
		addEntry(uid,pw1);
	}
}

window.addEventListener('click', function(e){   
	if (document.getElementById("modal-cnt").style.display == "block"){
		if (!document.getElementById('modal-cnt').contains(e.target)){
			closeModal();
		}
	}
});
