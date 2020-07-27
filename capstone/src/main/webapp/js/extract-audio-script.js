// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

window.onload = function() { 
    createAudioTranscription();
    fetchImageEffect();
};

function createAudioTranscription() {
  console.log('Fetching url of uploaded video file.');
  fetch("/video-upload").then(response => response.json()).then(jsonObj => {
	console.log(jsonObj);
    return jsonObj.url;
  }).then((url) => {
    console.log('Fetched url: ' + url); 
    console.log('Fetching audio transcription and effect.');
    // Fetch the audio transcription of the passed in video.
    fetch('/audio-effect?url=gs:/' + url, {
      method: 'GET'
    }).then(response => response.text()).then((effect) => {
      console.log('Fetched audio transcription and effect: ' + effect);
      const effectObj = JSON.parse(effect);

      // Display effect of audio and confidence level of effect.
      const effectElement = document.getElementById('results-audio-effect');
      effectElement.innerHTML = '';
      const effectDiv = document.createElement('div');

      // Check if key 'error' exists in HashMap
      if ('error' in effectObj) {
        // There was an error/exception when generating transcription.

        // Determine which error message to display.
        var errorMessageToUser = determineError(effectObj);

        // Set error message.
        const errorElement = document.createElement('p');
        errorElement.innerText = errorMessageToUser;  
      
        // Display error message.
        effectDiv.appendChild(errorElement);
        effectElement.appendChild(effectDiv);
      } else if ('transcription' in effectObj) {
        // There was no error/exception and transcription and effect was generated successfully.

        // Display confidence level in results.
        const confidenceElement = document.createElement('p');
        confidenceElement.innerText = 'Our confidence level in these results is: ' + effectObj.confidence +'%.'; 

        // Display results always, regardless of value.
        console.log('Generating display of effects.');
        const scoresElement = document.createElement('p');
        scoresElement.className = 'audio-effects-text';
        scoresElement.innerHTML = '<h2>Effect of the audio</h2>'
          + '<p><label for="toxicity">Toxicity Score: ' + effectObj.toxicityScore + '</label> \
            <meter id="toxicity" value="' + effectObj.toxicityScore + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
          + '<p><label for="insult">Insult Score: ' + effectObj.insultScore + '</label> \
            <meter id="insult" value="' + effectObj.insultScore + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
          + '<p><label for="threat">Threat Score: ' + effectObj.threatScore + '</label> \
            <meter id="threat" value="' + effectObj.threatScore + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
          + '<p><label for="profanity">Profanity Score: ' + effectObj.profanityScore + '</label> \
            <meter id="profanity" value="' + effectObj.profanityScore + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
          + '<p><label for="adult">Adult Score: ' + effectObj.adultScore + '</label> \
            <meter id="adult" value="' + effectObj.adultScore + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
          + '<p><label for="identity-attack">Identity Attack Score: ' + effectObj.identityAttackScore + '</label> \
            <meter id="identity-attack" value="' + effectObj.identityAttackScore + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
          + '<p>Attributes are scored from 0 - 10, with 0 being most unlikely to be perceived as the attribute. Values below 2 are '
          + 'preferable, below 3 are low, and above 5 are high.</p>'; 
        effectDiv.appendChild(confidenceElement); 
        effectDiv.appendChild(scoresElement);
        effectElement.appendChild(effectDiv);

        // Determine if any attributes' scores should be flagged.
        const effectsScores = new Array(6);
        effectsScores.push(effectObj.toxicityScore);
        effectsScores.push(effectObj.insultScore);
        effectsScores.push(effectObj.threatScore);
        effectsScores.push(effectObj.profanityScore);
        effectsScores.push(effectObj.adultScore);
        effectsScores.push(effectObj.identityAttackScore);

        if (effectsScores.some(e => e >= 5)) {
          document.getElementById('results-audio-overview').innerHTML = '<p>Your video was analyzed and scored across seven different metrics for negative effect. ' +
          'The scores range from 0 to 10 and represent the likelihood that the audio will be perceived as that attribute. The scores are below. </p>' + 
          '<h2>Your audio was flagged for negative content. Please review.</h2>';
        } else {
         document.getElementById('results-audio-overview').innerHTML = '<p>Your video was analyzed and scored across seven different metrics for negative effect. ' +
          'The scores range from 0 to 10 and represent the likelihood that the audio will be perceived as that attribute. The scores are below. </p>' + 
          '<h2>Your audio was not flagged for any negative content.</h2>';
        }
      } else {
        // There was an error/exception when generating transcription.

        // Set error message.
        const errorElement = document.createElement('p');
        errorElement.innerText = 'We\'re sorry, but we were unable to generate results for your video as the request to analyze your video\'s audio took too long. '
          + 'Sometimes this happens! If you wish your video\'s audio to be analyzed by Video Vigilance, please submit another request and refresh the page. Wait '
          + 'another minute and if you see this error message, follow the same steps until your video\'s audio\'s results are displayed. This may take a few tries. '
          + 'This timeout was NOT CAUGHT.';  
      
        // Display error message.
        effectDiv.appendChild(errorElement);
        effectElement.appendChild(effectDiv);
      }
    });
  });
}

function determineError(effectObj) {
  var errorMessageToUser = '';
  if(effectObj.error.localeCompare("Perspective") == 0) {
    errorMessageToUser = 'We\'re sorry, but we were were unable to generate results for your video as we were unable to retrieve results when analyzing '
      + 'your video\'s audio.'; 
  } else if (effectObj.error.localeCompare("VI") == 0) {
    errorMessageToUser = 'We\'re sorry, but we were unable to generate results for your video as we were unable to generate a transcription. ' 
      + 'This may be due to a lack of audio or background noise such as music and singing that Video Vigilance does not register as speech to translate. '
      + 'This may also be due to a corrupted video file. If your video file does have audio you wish to be analyzed, please ensure you are uploading a '
      + 'supported video file format.'; 
  } else if (effectObj.error.localeCompare("timeout") == 0) {
    errorMessageToUser = 'We\'re sorry, but we were unable to generate results for your video as the request to analyze your video\'s audio took too long. '
      + 'Sometimes this happens! If you wish your video\'s audio to be analyzed by Video Vigilance, please submit another request and refresh the page. Wait '
      + 'another minute and if you see this error message, follow the same steps until your video\'s audio\'s results are displayed. This may take a few tries.';
  } else if (effectObj.error.localeCompare("unforseen") == 0) {
    errorMessageToUser =  'We\'re sorry, but we were unable to generate results for your video for an unforseen reason. ' 
      + 'This may be due to a bug in the server or APIs. ';
  } else {
    errorMessageToUser = 'We\'re sorry, but we were unable to generate results for your video. ' 
      + 'This error message should never be displayed. If it is, panic time.';
  }
  return errorMessageToUser; 
}