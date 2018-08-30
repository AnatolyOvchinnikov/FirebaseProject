const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Sends a notifications to all users when a new message is posted.
exports.sendNotifications = functions.database.ref('/comments/{postId}/{messageId}').onCreate((snapshot, context) => {
    const text = snapshot.val().message;

    var fireTitle = `${snapshot.val().userName} posted ${text ? 'a message' : 'an image'}`
    var fireBody = text ? (text.length <= 100 ? text : text.substring(0, 97) + '...') : ''
    var fireJson = `{
        "title" : "${fireTitle}",
        "body" : "${fireBody}"
    }`;

    var payload = {
      data: {
        "firebase_data": fireJson
     }
    };
  
    let tokens = []; // All Device tokens to send a notification to.
    // Get the list of device tokens.
    let members = [];
    
    return admin.database().ref('members/'+snapshot.val().postId).once('value').then(allMembers => {
      if (allMembers.val()) {
        allMembers.forEach(function(childSnapshot) {
          if(childSnapshot.key != context.auth.uid) {
            members.push(childSnapshot.val().fcm_token)
          }
        })
        console.log(`Array : ${JSON.stringify(members)}`)

        return members
      }
    }).then(tokens => {
      if (tokens != null && tokens.length > 0) {
        // Send notifications to all tokens.
        return admin.messaging().sendToDevice(tokens, payload);
      }
      return {results: []};
    }).then(response => {
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
        }
      });

      return {};
    }).then(() => {
      admin.database().ref(`users/${snapshot.val().userId}/fcm_token`).once('value').then(response => {
        admin.database().ref(`members/${snapshot.val().postId}/${snapshot.val().userId}/fcm_token`).set(response.val())
      })

      console.log('Notifications have been sent.');
      return null;
    });
  });

  exports.getUserInfo = functions.https.onRequest((req, res) => {
    let data = req.body.data;

    admin.auth().getUser(data).then(function(userRecord) {
    console.log("Successfully fetched user data:", userRecord.toJSON());
      res.json({ data: {userInfo: {
        email: userRecord.email,
        name: userRecord.displayName,
        avatar: userRecord.photoURL
      }} });
    })
    .catch(function(error) {
      console.log("Error fetching user data:", error);
    });
  });