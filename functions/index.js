const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Sends a notifications to all users when a new message is posted.
exports.sendNotifications = functions.database.ref('/comments/{postId}/{messageId}').onCreate((snapshot, context) => {
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

    return admin.database().ref('members/'+snapshot.val().postId).once('value').then(allMembers => {
      if (allMembers.val()) {
        console.log("Test log 1 " + allMembers);
        console.log("Test log 2 " + JSON.stringify(allMembers));
        members = Object.keys(allMembers.val());

        console.log("Test log 3 " + members);
        console.log("Test log 4 " + JSON.stringify(members));

        let result = processArray(members, context);
        return result
      }
    }).then(allTokens => {
      allTokens.forEach(item => {
        let tempArray = Object.keys(item.val())

        tempArray.forEach(item1 => {
          tokens.push(item1)
        })
      })

      // display all values
      for (var i = 0; i < tokens.length; i++) {
        console.log(tokens[i]);
      }

      if (tokens != null) {
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

      admin.database().ref(`members/${snapshot.val().postId}/${snapshot.val().userId}`).set(true)

      console.log('Notifications have been sent and tokens cleaned up.');
      return null;
    });
  });

  function processArray(array, context) {
    let members = [];

    /*let index = array.indexOf(context.auth.uid);
    if (index > -1) {
      array.splice(index, 1)
    }*/

    array.forEach(item => {
      members.push(processItem(item));
    })

    return Promise.all(members)
  }

  function processItem(item) {
    return new Promise(resolve => {
      admin.database().ref('users/'+item+'/tokens').once('value')
      .then(response => {
        console.log("Process " + JSON.stringify(response));
        resolve(response)
      }
        )
      .catch(err => {
        console.error(item, err);
        resolve();
      })
    })
  }