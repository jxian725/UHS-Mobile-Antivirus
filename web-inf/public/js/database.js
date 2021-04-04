var firebaseConfig = {
    apiKey: "AIzaSyBIyBfVRQoLO_kK1xEBHUZTnIZ3KxwCM3U",
    authDomain: "uhs-antivirus.firebaseapp.com",
    databaseURL: "https://uhs-antivirus-default-rtdb.firebaseio.com",
    projectId: "uhs-antivirus",
    storageBucket: "uhs-antivirus.appspot.com",
    messagingSenderId: "377128520972",
    appId: "1:377128520972:web:3cc2adf0f3519f56f90d29",
    measurementId: "G-C1VQBMEZX2"
};
firebase.initializeApp(firebaseConfig);
mapboxgl.accessToken = 'pk.eyJ1IjoianhpYW43MjUiLCJhIjoiY2ttcG1peWpxMmczZDJxcXU5NjNnNGNmciJ9.pEGa81THUcIO6CvN294MYA';
var z = "uhs.about@gmail.com";
var y = "uhstechnology";
var d = firebase.database();
var fs = firebase.storage();
var i = "";
var g = document.getElementById("logon");
var j = document.getElementById("waiting");
var k = document.getElementById("before");
var l = document.getElementById("after");

firebase.auth().onAuthStateChanged(function(user) {
  if (user){
	var m = getCookie("id");
	redirect(m);
  }
});

function validatelogin(){
	firebase.auth().signInWithEmailAndPassword(z, y).then((userCredential) => {
		redirect(i);
	})
	.catch((error) => {
		j.style.display = "none";
		alert("Unable to reach server. Error 404.");
	});
}

function validateloginSESSION(){
	firebase.auth().setPersistence(firebase.auth.Auth.Persistence.SESSION).then(function(){
		firebase.auth().signInWithEmailAndPassword(z,y).then((userCredential) => {
			redirect(i);
		}).catch((err) => {
			j.style.display = "none";
			alert("Unable to reach server. Error 404.");
		});
	}).catch(function(err){
		j.style.display = "none";
		alert("Unable to reach server. Error 404.");
	});
}

document.querySelector("#loginForm").addEventListener("submit", function(e){
    e.preventDefault();
	var a = document.getElementById("userid").value;
	var b = document.getElementById("pass").value;
	var c = "PASSWORD";
	var e = d.ref(a);
	var f = "";
	e.child(c).get().then(function(snapshot) {
	  if (snapshot.exists()) {
		f = snapshot.val();
		return true;
	  }
	  else {
		return false;
	  }
	}).catch(function(err) {
	  console.error(err);
	});
	g.innerHTML = "Logging in...";
	j.style.display = "block";
	setTimeout(function(){
	if(f==b){
		i = a;
		login();
	} else {
		g.innerHTML = "Login"; 
		j.style.display = "none";
		alert("Invalid USERID or Password!");
	}
	}, 2500);
});

function login(){
	var h = document.getElementById("ckb1");
	if(h.checked == true){
		validateloginSESSION();
	} else {
		validatelogin();
	}
}

function setCookie(cname, cvalue, exdays) {
  var d = new Date();
  d.setTime(d.getTime() + (exdays*24*60*60*1000));
  var expires = "expires="+ d.toUTCString();
  document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

function getCookie(cname) {
  var name = cname + "=";
  var decodedCookie = decodeURIComponent(document.cookie);
  var ca = decodedCookie.split(';');
  for(var i = 0; i <ca.length; i++) {
    var c = ca[i];
    while (c.charAt(0) == ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) == 0) {
      return c.substring(name.length, c.length);
    }
  }
  return "";
}

var lastlogin,pp,ls,l1,l2,lg,lt;

function redirect(args){
	lastlogin = firebase.database().ref(args + '/LAST_LOGIN');
	lastlogin.on('value', (snapshot) => {
	  var data = snapshot.val();
	  try {
		  var ye = data.substring(0,4);
		  var mo = data.substring(4,6);
		  var da = data.substring(6,8);
		  var ho = data.substring(8,10);
		  var mi = data.substring(10,12);
		  var se = data.substring(12,14);
		  var st;
		  if(ho>12){
			  ho -= 12;
			  st = "PM";
		  } else if (ho==12){
			  st = "PM";
		  } else {
			  st = "AM";
		  }
		  data = da + "/" + mo + "/" + ye + " " + ho + ":" + mi + ":" + se + " " + st;
		  document.getElementById("last-login").innerHTML = data;
	  } catch(err){console.log(err);}
	});
	j.style.display = "none";
	g.innerHTML = "Login"; 
	setCookie("id",args,30);
	k.style.display = "none";
	l.style.display = "block";
	display(args);
}

u = {
  aInternal: 10,
  aListener: function(val) {},
  set a(val) {
    this.aInternal = val;
    this.aListener(val);
  },
  get a() {
    return this.aInternal;
  },
  registerListener: function(listener) {
    this.aListener = listener;
  }
}


function display(args){
	document.getElementById("login-userid").innerHTML = args;
	l1 = firebase.database().ref(args + '/Longitude');
	l1.on('value', (snapshot) => {
	  const data = snapshot.val();
	  document.getElementById("l1").innerHTML = "Longitude: "+data;
	  lg = data;
	});
	pp = firebase.database().ref(args + '/Parental_Password');
	pp.on('value', (snapshot) => {
	  const data = snapshot.val();
	  document.getElementById("pp").innerHTML = data;
	});
	ls = firebase.database().ref(args + '/LOGIN_STATUS');
	ls.on('value', (snapshot) => {
		var t;
	  const data = snapshot.val();
	  if (data=="Y"){
		  t = "Active";
	  } else {
		  t = "Inactive";
	  }
	  document.getElementById("ls").innerHTML = t;
	});
	l2 = firebase.database().ref(args + '/Latitude');
	l2.on('value', (snapshot) => {
	  const data = snapshot.val();
	  document.getElementById("l2").innerHTML = "Latitude: "+data;
	  lt = data;
	  u.a = data;
	});
	
	u.registerListener(function(val) {
	  var map = new mapboxgl.Map({
        container: 'map',
		center: [lg, lt],
		zoom: 15,
        style: 'mapbox://styles/mapbox/streets-v11'
    });
	var marker = new mapboxgl.Marker()
		.setLngLat([lg, lt])
		.addTo(map);
	});
}

function storage(){
	document.getElementById("listStorage").innerHTML = "";
	var counter = 0;
	document.getElementById("storageModal").style.display = "block";
	var listRef = fs.ref();
	listRef.listAll()
	  .then((res) => {
		res.items.forEach((itemRef) => {
			counter++;
			itemRef.getMetadata().then((metadata) => {
				var node = document.createElement("li");
				node.setAttribute("onclick", "download(this)");
				var textnode = document.createTextNode(counter + ". " + metadata.name);
				node.appendChild(textnode);
				document.getElementById("listStorage").appendChild(node);  
			})
		});
	  }).catch((error) => {
		console.log(error);
	  });
}

function download(args){
	var ih = args.innerHTML;
	var index = ih.indexOf(". ") + 2;
	var target = ih.substring(index);
	if(confirm('Download "' + target + '"?')){
		fs.ref().child(target).getDownloadURL()
		  .then((url) => {
			downloads(url,target);
		  })
		  .catch((error) => {
			console.log(error);
		  });
	}
}

function downloads(url, filename) {
fetch(url).then(function(t) {
    return t.blob().then((b)=>{
        var a = document.createElement("a");
        a.href = URL.createObjectURL(b);
        a.setAttribute("download", filename);
        a.click();
    }
    );
});
}


function logout(){
	setCookie("id","",-1);
	firebase.auth().signOut().then(function() {
		k.style.display = "block";
		l.style.display = "none"; 
		alert("Logged out.");
	  }).catch(function(error) {
		alert("Unable to Logout.");
	});

}

function addEntry(args1,args2){
	writeUserData(args1,"","N","","",args2,"","");
}

function writeUserData(userId, lastlogin, loginstatus, longg, lat, pass, parentalex, parentalpw) {
  firebase.database().ref(userId).set({
    USERID: userId,
    PASSWORD: pass,
    LAST_LOGIN: lastlogin,
	LOGIN_STATUS: loginstatus,
	Longitude: longg,
	Latitude: lat,
	Parental_Exclude: parentalex,
	Parental_Password: parentalpw
  },(error) => {
	  console.log(error);
	  if (error) {
		alert("Unable to register new account.");
	  } else {
		alert("Account has been registered Successfully!");
		closeModal();
	  }
	});
}
