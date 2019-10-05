var btn = document.getElementById("save");
var label = document.getElementById("label");
var tokenInput = document.getElementById("token");
btn.onclick = function() {
	var loc = window.location.href;
	var url = new URL(loc);
	var id = url.searchParams.get("id");
	var token = url.searchParams.get("token");
	if(id){
		if(token){
			window.location.href = window.location.origin;
			return;
		}
		token = tokenInput.value;
		if(!token){
			return;
		}
		url = window.location.origin+"/add?id="+id+"&token="+token;
		window.location.href = url;
	}else{
		label.innerText = "Идентификатор пользователя не найден. Получите новую ссылку."
		btn.disabled = true;
	}
}