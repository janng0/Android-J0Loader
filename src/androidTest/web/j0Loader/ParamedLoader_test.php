<?php

function gp($name) { 
	if (isset($_POST[$name])) return $_POST[$name];
	else if (isset($_GET[$name])) return $_GET[$name];
	else return null; 
}

$test_name = gp("test_name");

if ($test_name == null) {
	echo "no парамз!";
}

if ($test_name == "short_text_request") {
	echo $_GET['p1']." -- ".$_GET['p2']." -- ".$_GET['p3'];
}

if ($test_name == "long_text_request") {
	echo $_POST['p1']." -- ".$_POST['p2']." -- ".$_POST['p3']." -- ".strlen($_POST['p4_long']);
}

if ($test_name == "bitmap_request") {
	$path = $_SERVER["DOCUMENT_ROOT"]."j0Loader/bmp.jpg";
	if (move_uploaded_file($_FILES["bmp"]["tmp_name"], $path)) 
		echo "битмапа - ok";
	else echo "not ok!";
}

if ($test_name == "mixed_request") {
	$path = $_SERVER["DOCUMENT_ROOT"]."j0Loader/bmp.jpg";
	if (move_uploaded_file($_FILES["bmp"]["tmp_name"], $path)) 
		echo $_POST['p1']." -- ".$_POST['p2']." -- ".$_POST['p3']." -- ".strlen($_POST['p4_long'])." -- битмапа - ok";
	else echo "bmp not ok!";
}

?>