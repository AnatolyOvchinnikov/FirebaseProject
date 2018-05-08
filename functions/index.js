const functions = require('firebase-functions');

// Берем модуль firebase-admin для доступа в базу данных Firebase Realtime Database
const admin = require('firebase-admin');
// инициализируем приложение
admin.initializeApp();

// Sends a notifications to all users when a new message is posted.
exports.sendNotifications = functions.database.ref('/comments/{postId}/{messageId}').onCreate(snapshot => {
    // Notification details.
    const text = snapshot.val().message;
    /*const payload = {
      notification: {
        title: `${snapshot.val().userName} posted ${text ? 'a message' : 'an image'}`,
        body: text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : '',
        icon: snapshot.val().photoUrl || '/images/profile_placeholder.png',
        click_action: `https://${process.env.GCLOUD_PROJECT}.firebaseapp.com`,
      }
    };*/

    var fireTitle = `${snapshot.val().userName} posted ${text ? 'a message' : 'an image'}`
    var fireBody = text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : ''
    var fireJson = `{
        "title" : "${fireTitle}",
        "body" : "${fireBody}"
    }`;

    var payload = {
      /*notification: {
        title: `${snapshot.val().userName} posted ${text ? 'a message' : 'an image'}`,
        body: text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : ''
      },*/
      data: {
        "firebase_data": fireJson
     }
    };
  
    let tokens = []; // All Device tokens to send a notification to.
    // Get the list of device tokens.
    return admin.database().ref('users/'+snapshot.val().userId+'/tokens').once('value').then(allTokens => {
      if (allTokens.val()) {
        // Listing all tokens.
        tokens = Object.keys(allTokens.val());
  
        // Send notifications to all tokens.
        return admin.messaging().sendToDevice(tokens, payload);
      }
      return {results: []};
    }).then(response => {
      // For each notification we check if there was an error.
      const tokensToRemove = {};
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove[`users/${snapshot.val().userId}/tokens/${tokens[index]}`] = null;
          }
        }
      });
      return admin.database().ref().update(tokensToRemove);
    }).then(() => {

      var obj = {user_id: snapshot.val().userId};
      admin.database().ref('comments/members').set(obj)

      console.log('Notifications have been sent and tokens cleaned up.');
      return null;
    });
  });