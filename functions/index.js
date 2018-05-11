//import { resolve } from 'dns';

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

    
    let members = [];
    /*return admin.database().ref('members/'+snapshot.val().postId).once('value').then(allMembers => {
      if (allMembers.val()) {
        members = Object.keys(allMembers.val());
        console.log('FirebaseTest ' + JSON.stringify(allMembers));
        

        return admin.database().ref('users/'+snapshot.val().userId+'/tokens').once('value')
      }
      return {results: []};
    })*/
    admin.database().ref('members/'+snapshot.val().postId).once('value').then(allMembers => {
      if (allMembers.val()) {
        members = Object.keys(allMembers.val());
        let result = processArray(members).then(response => {
          console.log('FirebaseTest 2 ' + JSON.stringify(response));
        });
        console.log('FirebaseTest ' + result);
      }
    })
    

    
    
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

      //var obj = {user_id: snapshot.val().userId};
      //admin.database().ref(`members/${snapshot.val().postId}`).set(obj)
      admin.database().ref(`members/${snapshot.val().postId}/${snapshot.val().userId}`).set(true)

      console.log('Notifications have been sent and tokens cleaned up.');
      return null;
    });
  });

  function processArray(array) {
    let members = {};
    let testArray = []
    let testArrayPromise = []

    //const promises = array.map
    //let a = array.pop()
    //console.log('FirebaseTest Promise 1 ' + JSON.stringify(admin.database().ref('users/'+a+'/tokens').once('value')));
    //console.log('FirebaseTest Promise 2 ' + admin.database().ref('users/'+a+'/tokens').once('value'));
    /*admin.database().ref('users/'+a+'/tokens').once('value').then(result => {
      console.log('FirebaseTest 111 ' + JSON.stringify(result));
    })*/

    array.forEach(item => {
      console.log('Promise item 1 ' + item)
      testArray.push(processItem(item));
      testArrayPromise.push(admin.database().ref('users/'+item+'/tokens').once('value'))
      console.log('Promise item 2 ' + admin.database().ref('users/'+item+'/tokens').once('value'))
      /*admin.database().ref('users/'+item+'/tokens').once('value').then(result => {
        console.log('FirebaseTest 123 ' + JSON.stringify(result));
      })*/
    })
    console.log('Promise array ' + testArray + ' length: ' + testArray.length)
    

    /*for(const item of array) {
      admin.database().ref('users/'+item+'/tokens').once('value').then(result => {
        members[item] = result
      })
    }*/
    console.log('FirebaseTest Done');
    
    // return Promise.all(testArray)
    return Promise.all(testArrayPromise)
  }

  //processArray([1]).then(res)

  function processItem(item) {
    return new Promise(resolve => {
      admin.database().ref('users/'+item+'/tokens').once('value')
      .then(response => resolve(response))
      .catch(err => {
        console.error(item, err);
        resolve();
      })
    })
  }