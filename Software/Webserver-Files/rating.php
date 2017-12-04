<!DOCTYPE html>
<html lang="ja">
  <head>
    <meta charset="UTF-8">

    <meta http-equiv="refresh" content="5;url=index.html" />
    <!--Import materialize.css-->
    <link type="text/css" rel="stylesheet" href="css/materialize.min.css"  media="screen,projection"/>
    <link type="text/css" rel="stylesheet" href="css/main.css"  media="screen,projection"/>
    <!--Let browser know website is optimized for mobile-->
    <meta name="viewport" content="width=device-width, initial-scale=0.5"/>
    <script src="js/jquery-3.1.0.min.js"></script>
   <script type="text/javascript" src="js/script.js"></script>

    <title>MOS Projekt</title>
    <script src="/libs/qimessaging/2/qimessaging.js"></script>
    <script type="text/javascript" src="js/jquery-3.1.0.min.js"></script>
    <script type="text/javascript" src="js/materialize.min.js"></script>


  </head>
  <body>
    <div class="base">
      <div class="content row">
	  <div class="col s12">
	    <div class="preview" align="center">
	      <img id="image" src="pics/Hochschule_Furtwangen_HFU_logo.svg" class="responsive-img image" align="right" width="450px"/>

	    </div>
	  </div>
      </div>
      <!DOCTYPE html>
<html>
<head>
	    <center>  <h2 id="title" class="title">Vielen Dank für Ihre Bewertung! <p>Wow <?php $rate = $_GET['rate']; echo $rate ?> Sterne, das gibt bestimmt eine Flasche Motoröl! <p> Bis bald! <img id="image" src="pics/robot.png" width="64px"/></h2></center>
	      <p>
		      <p>
			      <p>

<style>
.button {
  /*display: inline-block;*/
  padding: 15px 25px;
  font-size: 60px;
 /* text-decoration: blink */
  text-align: center;
  font-weight: bold;
  outline: none;
  border: none;
  border-radius: 120px;
  box-shadow: 0 12px #999; 
}

.button1 {width: 30%; background-color: green; padding: 100px 100px; margin: 20px;}
.button2 {width: 30%; background-color: yellow;padding: 100px 100px; margin: 20px; }
.button3 {width: 30%; background-color:red; padding: 100px 50px; margin: 20px;
 }

#button:active {
  background: url(button.png) no-repeat bottom;
}


</style>
</head>
<body>
<br>
<br>
<br>
<br>	
<center>

<?php
$rate = $_GET['rate'];


switch ($rate) {
case '1':
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';	  
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	break;

case '2':
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	break;

case '3':
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	break;

case '4':
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_unpressed.png" class="responsive-img image" width="256px" align="middle">';
	break;

case '5':
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	echo '<img src="pics/star_pressed.png" class="responsive-img image" width="256px" align="middle">';
	break;

}


?>
</center>
</body>
</html>


