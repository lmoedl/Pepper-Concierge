// keeping a pointer to the session is very useful!
var session;

try {
  QiSession( function (s) {
    console.log('connected!');
    session = s;
    // now that we are connected, we can use the buttons on the page
    $('button').prop('disabled', false);
    s.service('ALMemory').then(function (memory) {
      memory.subscriber('TouchChanged').then(function (subscriber) {
        subscriber.signal.connect(changeTitle);
      });
    });
  });
} catch (err) {
  console.log("Error when initializing QiSession: " + err.message);
  console.log("Make sure you load this page from the robots server.")
}

$(function () {
  $('#say').click(sayHelloWorld);
});

$(function () {
  $('#help').click(help);
});


$(function () {
  $('#areyousure').click(areyousure);
});

$(function () {
  $('#somethingtodrink').click(somethingtodrink);
});

function changeTitle(data) {
  $('h1').text('Message received!')
}

function sayHelloWorld() {
  session.service('ALTextToSpeech').then(function (tts) {
    tts.say('Sehr gut!, bitte denken Sie daran immer genug zu trinken');
  }, function (error) {
    console.log(error);
  })
  }
 
function somethingtodrink() {
  session.service('ALTextToSpeech').then(function (tts) {
    tts.say('Oh! Dann sollten Sie schnell etwas trinken!');
  }, function (error) {
    console.log(error);
  })
  }
  
function help() {
  session.service('ALTextToSpeech').then(function (tts) {
    tts.say('Wählen Sie ihre Antwort aus, in dem Sie die entsprechende Auswahl auf dem Bildschirm berühren!');
  }, function (error) {
    console.log(error);
  })
  }
  
  function areyousure() {
  session.service('ALTextToSpeech').then(function (tts) {
    tts.say('Sind Sie sich sicher?');
  }, function (error) {
    console.log(error);
  })
}
