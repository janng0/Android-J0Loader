<?php

$path = $_SERVER["DOCUMENT_ROOT"]."j0Loader/bmp.jpg";
if (move_uploaded_file($_FILES["bmp"]["tmp_name"], $path)) { 
?>
	{
		'p1':'<?php echo $_POST['p1']; ?>',
		'p2':'<?php echo $_POST['p2']; ?>',
		'p3':'<?php echo $_POST['p3']; ?>',
		'p4_long':'<?php echo strlen($_POST['p4_long']); ?>',
		'bmp':'<?php echo "битмапа - ok"; ?>'
	}
<?php	
} else echo "{ 'error': 'bmp not ok!' }";

?>