<?php
$file = $_GET['path'];


//usermod -a -G video www-data
//wird benötigt!
//system('omxplayer NFS.mp4');
system('omxplayer '. $file);

?>
