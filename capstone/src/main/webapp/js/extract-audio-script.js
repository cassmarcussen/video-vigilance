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

window.onload = function() { createAudioTranscription() };

function createAudioTranscription() {
  console.log('Fetching audio transcription and effect.');
  // Fetch the audio transcription of the passed in video.
  fetch('/audio-effect', {
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
      const errorMessageToUser = determineError(effectObj);

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
        + '<p>Attributes are scored from 0 - 10, with 0 being most unlikely to be perceived as the attribute and 10 being most '
        + 'likely to be perceived as the attribute. Scores below 2 are preferable, below 3 are considered low, between 3 and 5 '
        + 'are advised against, and above 5 are flagged.</p>'
        + '<p><label for="toxicity">Toxicity Score: ' + effectObj.TOXICITY + '</label> \
          <meter id="toxicity" value="' + effectObj.TOXICITY + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
        + '<p><label for="insult">Insult Score: ' + effectObj.INSULT + '</label> \
          <meter id="insult" value="' + effectObj.INSULT + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
        + '<p><label for="threat">Threat Score: ' + effectObj.THREAT + '</label> \
          <meter id="threat" value="' + effectObj.THREAT + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
        + '<p><label for="profanity">Profanity Score: ' + effectObj.PROFANITY + '</label> \
          <meter id="profanity" value="' + effectObj.PROFANITY + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
        + '<p><label for="adult">Adult Score: ' + effectObj.SEXUALLY_EXPLICIT + '</label> \
          <meter id="adult" value="' + effectObj.SEXUALLY_EXPLICIT + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'
        + '<p><label for="identity-attack">Identity Attack Score: ' + effectObj.IDENTITY_ATTACK + '</label> \
          <meter id="identity-attack" value="' + effectObj.IDENTITY_ATTACK + '"  min="0" low="3" high="5" optimum="2" max="10"></meter></p>'; 
      effectDiv.appendChild(confidenceElement); 
      effectDiv.appendChild(scoresElement);
      effectElement.appendChild(effectDiv);

      // Determine if any attributes' scores should be flagged and display proper message to user.
      const flaggedMessage = '<p>Your video was analyzed and scored across seven different metrics for negative effect. ' +
        'The scores range from 0 to 10 and represent the likelihood that the audio will be perceived as that attribute. The scores are below. </p>' + 
        '<h2>Your audio was flagged for negative content. Please review.</h2>';
      const notFlaggedMessage = '<p>Your video was analyzed and scored across seven different metrics for negative effect. ' +
        'The scores range from 0 to 10 and represent the likelihood that the audio will be perceived as that attribute. The scores are below. </p>' + 
        '<h2>Your audio was not flagged for any negative content.</h2>';
      document.getElementById('results-audio-overview').innerHTML = effectObj.flag.localeCompare("true") == 0 ? flaggedMessage : notFlaggedMessage; 
    
    } else {
      // There was a timeout error. Request took longer than 60 seconds and GAE abruptly forced the request to end.
      const errorMessage = 'We\'re sorry, but we were unable to generate results for your video as the request to analyze your video\'s audio took too long. '
        + 'Sometimes this happens! If you wish your video\'s audio to be analyzed by Video Vigilance, please submit another request and refresh the page. Wait '
        + 'another minute and if you see this error message, follow the same steps until your video\'s audio\'s results are displayed. This may take a few tries.';
      
      // Set error message.
      const errorElement = document.createElement('p');
      errorElement.innerText = errorMessage;  
      
      // Display error message.
      effectDiv.appendChild(errorElement);
      effectElement.appendChild(effectDiv);
    }
  });
}

/**
 * If an error key was returned in the HashMap response from the servlet, use the value associated with the
 * error key to display an appropriate error message to the user.
 */
function determineError(effectObj) {
  const perspectiveError = 'We\'re sorry, but we were were unable to generate results for your video as we were unable to retrieve results when analyzing '
    + 'your video\'s audio.';
  const videoIntelligenceError = 'We\'re sorry, but we were unable to generate results for your video as we were unable to generate a transcription. ' 
    + 'This may be due to a lack of audio or background noise such as music and singing that Video Vigilance does not register as speech to translate. '
    + 'This may also be due to a corrupted video file. If your video file does have audio you wish to be analyzed, please ensure you are uploading a '
    + 'supported video file format.'; 
  const emptyTranscription = 'There are no results to display since our application did not detect any spoken word in your video to analyze.';
  const timeoutError = 'We\'re sorry, but we were unable to generate results for your video as the request to analyze your video\'s audio took too long. '
    + 'Sometimes this happens! If you wish your video\'s audio to be analyzed by Video Vigilance, please submit another request and refresh the page. Wait '
    + 'another minute and if you see this error message, follow the same steps until your video\'s audio\'s results are displayed. This may take a few tries.';
  const unforseenError = 'We\'re sorry, but we were unable to generate results for your video for an unforseen reason. ' 
    + 'This may be due to a bug in the server or APIs.';
  const panicError = 'We\'re sorry, but we were unable to generate results for your video. ' 
    + 'This error message should never be displayed. If it is displaying, panic time.';
  if (effectObj.error.localeCompare("Perspective") == 0) {
    return perspectiveError;
  } else if (effectObj.error.localeCompare("VI") == 0) {
    return videoIntelligenceError;
  } else if (effectObj.error.localeCompare("emptyTranscription") == 0) {
    return emptyTranscription;
  } else if (effectObj.error.localeCompare("timeout") == 0) {
    return timeoutError;
  } else if (effectObj.error.localeCompare("unforseen") == 0) {
    return unforseenError;
  } else {
    return panicError;
  }
}