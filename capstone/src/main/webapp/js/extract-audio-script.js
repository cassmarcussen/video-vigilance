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
    effectElement.classList.add('card');
    effectElement.classList.add('audio-card');
    const effectDiv = document.createElement('div');
    effectDiv.classList.add('card-body');
    effectDiv.classList.add('audio-card-body');

    // Check if key 'error' exists in HashMap
    if ('error' in effectObj) {
      // There was an error/exception when generating transcription.

      // Determine which error message to display.
      const errorMessageToUser = determineError(effectObj.error);

      // Set error message.
      const errorElement = document.createElement('p');
      errorElement.innerText = errorMessageToUser;  
      
      // Display error message.
      effectDiv.appendChild(errorElement);
      effectElement.appendChild(effectDiv);
    } else if ('transcription' in effectObj) {
      // There was no error/exception and transcription and effect was generated successfully.

      // Display results always, regardless of value.
      console.log('Generating display of effects.');

      // Create HTML for displaying attribute summary scores through likelihood metric.
      const scoresElement = document.createElement('p');
      scoresElement.className = 'audio-effects-text';
      scoresElement.innerHTML = createScoresHTML(effectObj);
      // Create HTML for displaying the transcription.
      const transcriptElement = document.createElement('p');
      transcriptElement.className = 'audio-effects-text';
      transcriptElement.innerHTML = createTranscriptHTML(effectObj);

      // Determine if any attributes' scores should be flagged and display proper message to user.
      const flaggedMessage = '<p>Your video was analyzed and scored across seven different metrics for negative effect. ' +
        'The scores range from 0 to 10 and represent the likelihood that the audio will be perceived as that attribute. The scores are below. </p>' + 
        '<h2>Your audio was flagged for negative content. Please review.</h2>';
      const notFlaggedMessage = '<p>Your video was analyzed and scored across seven different metrics for negative effect. ' +
        'The scores range from 0 to 10 and represent the likelihood that the audio will be perceived as that attribute. The scores are below. </p>' + 
        '<h2>Your audio was not flagged for any negative content.</h2>';
      document.getElementById('results-audio-overview').innerHTML = effectObj.flag.localeCompare("true") == 0 ? flaggedMessage : notFlaggedMessage; 
      
      // Display the elements on DOM.
      effectDiv.appendChild(scoresElement);
      effectDiv.appendChild(transcriptElement);
      effectElement.appendChild(effectDiv);
    
    } else {
      // There was a timeout error. Request took longer than 60 seconds and GAE abruptly forced the request to end.
      const errorMessage = determineError("timeout")
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
 * Creates the innerHTML for the element created to display the scores for each attribute's score returned
 * by Perspective API
 * @param effectObj: the response from the servlet
 */
function createScoresHTML(effectObj) {
  var htmlForToxicity = createMeterHTML('toxicity', 'Toxicity', effectObj.TOXICITY);
  var htmlForInsult = createMeterHTML('insult', 'Insult', effectObj.INSULT);
  var htmlForThreat = createMeterHTML('threat', 'Threat', effectObj.THREAT);
  var htmlForProfanity = createMeterHTML('profanity', 'Profanity', effectObj.PROFANITY);
  var htmlForAdult = createMeterHTML('adult', 'Adult', effectObj.SEXUALLY_EXPLICIT);
  var htmlForIdentityAttack = createMeterHTML('identityattack', 'Identity Attack', effectObj.IDENTITY_ATTACK);
  var content = '<h2 class="card-title">Effect of the audio</h2>'
    + '<div class="card-text" id="card-image">'
    + '<p>Attributes are scored from 0 - 10, with 0 being most unlikely to be perceived as the attribute and 10 being most '
    + 'likely to be perceived as the attribute. Scores below 2 are classified as Very Unlikely, between 2 and 4 are Unlikely, between 4 and 6 '
    + 'are Possible, between 6 and 8 are Likely, and above 8 are Very Likely. Values greater than or equal to 6 are flagged.</p>'
    + htmlForToxicity + htmlForInsult + htmlForThreat + htmlForProfanity + htmlForAdult + htmlForIdentityAttack
    + '</div>';
  return content;
}

/**
 * Creates the meter HTML for a specified attribute and its score.
 * @param scoreId the id to be set for the label and corresponding meter
 * @param name the name of the attribute to be displayed to user
 * @param score the numerical score for that attribute
 */
function createMeterHTML(scoreId, name, score) {
  var meterContent = '<label for="' + scoreId + '">' + name 
      + '<div class="tooltip-info"> '
        + '<i class="fa fa-info-circle" aria-hidden="true"></i> '
        + '<span class="tooltiptext-info">'+ Definition[scoreId] + '</span> '
      + '</div> '
    + '</label> '
    + '<meter id="' + scoreId + '" value="' + getScoreAsValue(score) + '"  min="0" low="2" high="3" optimum="1" max="5"></meter>'
    + '<label class="scoreText"> ' + getScoresAsLikelihood(score) 
      + '<div class="tooltip-orig-score"> '
        + '<i class="fa fa-info-circle" aria-hidden="true"></i> '
        + '<span id="tooltiptext-orig-score">Your video\'s toxicity score was a ' + score + ' out of 10. Meaning, the likelihood '
        + 'that your video will be perceived as toxic by your audience is ' + (score*10).toFixed(1) + '%.</span>'
      + '</div>'
    + '</label> ';
  return meterContent;
}


/**
 * Returns the confidence level of the transcription and allows the user to
 * view the transcription generated by Video Intelligence API.
 */
function createTranscriptHTML(effectObj) {
  var content = '<hr>'
    + '<h2 class="card-title">Transcription of the Audio</h2>'
    + '<div class="card-text" id="card-image">'
      + '<p>To analyze your video\'s audio, we utilized a computer-generated transcription of your '
        + 'video as the basis. With this transcription, we were able to analyze the content for any '
        + 'negative attributes. Your scores shown above were based on the following transcription.</p>'
      + createCollapsibleTranscript(effectObj)
    + '</div>'
  return content;
}

/**
 * Create html for a button that will expand a collapsible div containing the transcript
 * of the user's audio and the confidence level.  
 */
function createCollapsibleTranscript(effectObj) {
  var button = '<label class="conf-label">Our confidence level in this transcription is ' + effectObj.confidence + '%.' 
      + '<div class="tooltip-info"> '
        + '<i class="fa fa-info-circle" aria-hidden="true"></i> '
        + '<span class="tooltiptext-info">'+ Definition['confidence'] + '</span> '
      + '</div> ' 
    + '</label>'
    + '<button type="button" class="transcript-collapse collapsed" data-toggle="collapse" data-target="#transcript" aria-expanded="false" aria-controls="transcript">Click For Transcription</button> '
    + '<div class="transcript" id="transcript"> '
      + '<p>"' + effectObj.transcription + '"</p> '
    + '</div>';
  return button;
}

/**
 * Return the tooltip text explaining what an attribute/confidence is.
 * @enum {String}
 */
const Definition = {
  confidence: 'In order to analyze the video you uploaded, we used machine learning to generate a speech transcription. We '
    + 'understand this transcription may not be accurate. This percentage reflects our confidence in how accurate the '
    + 'computer-generated transcription is.',
  toxicity: 'Toxicity: rude, disrespectful, or unreasonable language that is likely to make people leave a discussion.',
  insult: 'Insult: insulting, inflammatory, or negative language towards a person or a group of people.',
  threat: 'Threat: an intention to inflict pain, injury, or violence against an individual or group.',
  profanity: 'Profanity: swear words, curse words, or other obscene or profane language.',
  adult: 'Adult: references to sexual acts, body parts, or other lewd content.',
  identityattack: 'Identity Attack: negative or hateful language targeting someone because of their identity.'
};

/**
 * Returns a value properly formatted for the new meter range corresponding to the
 * attributes' summary score returned by Perspective API.
 * Attribute summary scores below 2 are formatted to 1, below 4 are formatted to 2, below 6 
 * are formatted to 3, below 8 are formatted to 4, and between 8 and 10 are formatted to 5. 
 */
function getScoreAsValue(score) {
  var value = '';
  var range = Math.floor((score/2) + 1);
  switch(range) {
    case 1:
      value = 1;
      break;
    case 2:
      value = 2;
      break;
    case 3:
      value = 3;
      break;
    case 4:
      value = 4;
      break;
    case 5: 
      value = 5;
      break;
    case 6:
      value = 5;
      break;
    default:
      value = 0;
      break;
  }
  return value;
}

/**
 * Returns the likelihood that the audio will be perceived as an attribute based on 
 * the numerical score returned by Perspective API.
 * Attribute summary scores below 2 are very unlikely, below 4 are unlikely, below 6 
 * are possible, below 8 are likely, and between 8 and 10 are very likely. 
 */
function getScoresAsLikelihood(score) {
  var likelihood = '';
  var range = Math.floor((score/2) + 1);
  switch(range) {
    case 1:
      likelihood = 'Very Unlikely';
      break;
    case 2:
      likelihood = 'Unlikely';
      break;
    case 3:
      likelihood = 'Possible';
      break;
    case 4:
      likelihood = 'Likely';
      break;
    case 5: 
      likelihood = 'Very Likely';
      break;
    case 6:
      likelihood = 'Very Likely';
      break;
    default:
      likelihood = 'Unknown';
      break;
  }
  return likelihood; 
}

/**
 * If an error key was returned in the HashMap response from the servlet, use the value associated with the
 * error key to display an appropriate error message to the user.
 */
function determineError(error) {
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
  const unforeseenError = 'We\'re sorry, but we were unable to generate results for your video for an unforseen reason. ' 
    + 'This may be due to a bug in the server or APIs.';
  const panicError = 'We\'re sorry, but we were unable to generate results for your video. ' 
    + 'This error message should never be displayed. If it is displaying, panic time.';
  if (error.localeCompare("Perspective") == 0) {
    return perspectiveError;
  } else if (error.localeCompare("VI") == 0) {
    return videoIntelligenceError;
  } else if (error.localeCompare("emptyTranscription") == 0) {
    return emptyTranscription;
  } else if (error.localeCompare("timeout") == 0) {
    return timeoutError;
  } else if (error.localeCompare("unforeseen") == 0) {
    return unforeseenError;
  } else {
    return panicError;
  }
}