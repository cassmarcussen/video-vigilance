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
  getUrl();
  fetchImageEffect();
};

/**
 * Retrieve the gcsUri of the video the user uploaded.
 */
function getUrl() {
  console.log('Fetching url of uploaded video file.');
  fetch("/video-upload").then(response => response.json()).then(jsonObj => {
	console.log(jsonObj);
    if (jsonObj.error) {
      // If there was an error getting the url, do not attempt to run analysis.
      const effectElement = document.getElementById('results-audio-effect');
      effectElement.innerHTML = '';
      const effectDiv = document.createElement('div');
      // Set error message.
      const errorMessageToUser = 'We cannot generate results for you until you successfully upload a video. '
        + 'Please upload a video for us to analyze.';
      const errorElement = document.createElement('p');
      errorElement.innerText = errorMessageToUser;
      // Display error message.
      effectDiv.appendChild(errorElement);
      effectElement.appendChild(effectDiv);
    } else {
      createAudioTranscription(jsonObj.url);
    }
  });
}

/**
 * Run an analysis and return the effect of the video's audio.
 */
function createAudioTranscription(url) {
  console.log('Fetching audio transcription and effect.');
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
      console.log('An error/exception was caught in the process of fetching audio transcription and effect.');
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
      confidenceElement.innerHTML = '<p>Our confidence level in these results is: ' + effectObj.confidence +'%.'
        + '<span class="tooltip-info"> '
          + '<i class="fa fa-info-circle" aria-hidden="true"></i> '
          + '<span class="tooltiptext-info">'+ getDefinition("confidence") + '</span> '
        + '</span></p>'; 
 
      // Display results always, regardless of value.
      console.log('Generating display of effects.');
      const scoresElement = document.createElement('p');
      scoresElement.className = 'audio-effects-text';
      scoresElement.innerHTML = createScoresHTML(effectObj);
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
 * Creates the innerHTML for the element created to display the scores for each attribute's score returned
 * by Perspective API
 * @param effectObj: the response from the servlet
 */
function createScoresHTML(effectObj) {
  var content = '<h2>Effect of the audio</h2>'
    + '<p>Attributes are scored from 0 - 10, with 0 being most unlikely to be perceived as the attribute and 10 being most '
    + 'likely to be perceived as the attribute. Scores below 2 are very unlikely, between 2 and 4 are unlikely, between 4 and 6 '
    + 'are possible, between 6 and 8 are likely, and above 8 are very likely. Values greater than or equal to 6 are flagged.</p>'
    + createMeterHTML("toxicity", "Toxicity", effectObj.TOXICITY)
    + createMeterHTML("insult", "Insult", effectObj.INSULT)
    + createMeterHTML("threat", "Threat", effectObj.THREAT)
    + createMeterHTML("profanity", "Profanity", effectObj.PROFANITY)
    + createMeterHTML("adult", "Adult", effectObj.SEXUALLY_EXPLICIT)
    + createMeterHTML("identity-attack", "Identity Attack", effectObj.IDENTITY_ATTACK);
  return content;
}

/**
 * Creates the meter HTML for each attribute and its score.
 * @param scoreId the id to be set for the label and corresponding meter
 * @param name the name of the attribute
 * @param score the numerical score for that attribute
 */
function createMeterHTML(scoreId, name, score) {
  var meterContent = '<div class="attribute-meter-wrapper"> '
    + '<p><label for="' + scoreId + '">' + name 
      + '<span class="tooltip-info"> '
        + '<i class="fa fa-info-circle" aria-hidden="true"></i> '
        + '<span class="tooltiptext-info">'+ getDefinition(scoreId) + '</span> '
      + '</span> '
    + '</label> '
    + '</p>'
    + '<meter id="' + scoreId + '" value="' + getScoreAsValue(score) + '"  min="0" low="2" high="3" optimum="1" max="5"></meter>'
    + '<p class="scoreText"> ' + getScoresAsLikelihood(score) 
      + '<span id="tooltip-orig-score"> '
        + '<i class="fa fa-info-circle" aria-hidden="true"></i> '
        + '<span id="tooltiptext-orig-score">Your video\'s toxicity score was a ' + score + ' out of 10. Meaning, the likelihood '
        + 'that your video will be perceived as toxic by your audience is ' + (score*10).toFixed(1) + '%.</span>'
      + '</span>'
    + ' </p> '
    + '</div> ';
  return meterContent;
}

/**
 * Return the definition of a given Perspective API attribute, as outlined by the API developers
 * Reference: https://github.com/conversationai/perspectiveapi/blob/master/2-api/models.md#all-attribute-types
 * @param attributeId: a string to identify the attribute whose definition we seek
 */
function getDefinition(attributeId) {
  var definition = '';
  if (attributeId.localeCompare("confidence") == 0) {
    definition = 'In order to analyze the video you uploaded, we used machine learning to generate a speech transcription. We '
      + 'understand this transcription may not be accurate. This percentage reflects our confidence in how accurate the '
      + 'computer-generated transcription is.';
  } else if (attributeId.localeCompare("toxicity") == 0) {
    definition = 'Toxicity: rude, disrespectful, or unreasonable language that is likely to make people leave a discussion.';
  } else if (attributeId.localeCompare("insult") == 0) {
    definition = 'Insult: insulting, inflammatory, or negative language towards a person or a group of people.';
  } else if (attributeId.localeCompare("threat") == 0) {
    definition = 'Threat: an intention to inflict pain, injury, or violence against an individual or group.';
  } else if (attributeId.localeCompare("profanity") == 0) {
    definition = 'Profanity: swear words, curse words, or other obscene or profane language.';
  } else if (attributeId.localeCompare("adult") == 0) {
    definition = 'Adult: references to sexual acts, body parts, or other lewd content.';
  } else {
    definition = 'Identity Attack: negative or hateful language targeting someone because of their identity.';
  }
  return definition;
}

/**
 * Returns a value properly formatted for the new meter range corresponding to the
 * attributes' summary score returned by Perspective API.
 * Attribute summary scores below 2 are formatted to 1, below 4 are formatted to 2, below 6 
 * are formatted to 3, below 8 are formatted to 4, and between 8 and 10 are formatted to 5. 
 */
function getScoreAsValue(score) {
  var value = '';
  if (score < 2) {
    value = 1;
  } else if (score < 4) {
    value = 2;
  } else if (score < 6) {
    value = 3;
  } else if (score < 8) {
    value = 4;
  } else {
    value = 5;
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
  if (score < 2) {
    likelihood = 'Very Unlikely';
  } else if (score < 4) {
    likelihood = 'Unlikely';
  } else if (score < 6) {
    likelihood = 'Possible';
  } else if (score < 8) {
    likelihood = 'Likely';
  } else {
    likelihood = 'Very Likely';
  }
  return likelihood; 
}

/**
 * If the HashMap returned from the servlet includes an 'error' key, something went wrong in the request
 * and, depending on what, we will display an appropriate error message to the user
 * @param effectObj: the response from the servlet
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
