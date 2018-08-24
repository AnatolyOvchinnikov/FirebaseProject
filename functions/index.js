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

  exports.testFunction = functions.https.onRequest((req, res) => {
    // console.log("Test function called");
    // res.status(200).send(`{"title" : "test"}`)
    /*let data = JSON.stringify({
      message: 'OK',
    });
    res.status(200).type('application/json').send(data);*/

    admin.database().ref(`users`).once('value').then(response => {
      console.log(`Fire ${response.val()}`)
      console.log(`Fire ${JSON.stringify(response.val())}`)
      console.log(`Fire ${Object.keys(response.val())}`)
      console.log(`Fire ${JSON.stringify(Object.keys(response.val()))}`)
    })
    
    res.json({ data: {user: 'tj'} });
    
    
    /*return admin.database().ref('users').once('value').then(response => {
      res.status(200).send(response)
    });*/
  });

  exports.getUsers = functions.https.onRequest((req, res) => {
    // console.log("Test function called");
    // res.status(200).send(`{"title" : "test"}`)
    /*let data = JSON.stringify({
      message: 'OK',
    });
    res.status(200).type('application/json').send(data);*/

    let data = req.body.data;
    /*console.log(`REQ 1 ${data}`)
    console.log(`REQ 2 ${JSON.stringify(data)}`)*/

    var qqq;
    admin.database().ref(`users`).once('value').then(response => {
      /*console.log(`Fire_Users ${response.val()}`)
      console.log(`Fire_Users ${JSON.stringify(response.val())}`)
      console.log(`Fire_Users ${Object.keys(response.val())}`)
      console.log(`Fire_Users ${JSON.stringify(Object.keys(response.val()))}`)*/

      qqq = Object.keys(response.val())
      console.log(`Fire ${qqq}`)
      /*var a_array = Object.keys(response.val()).val()
    console.log(`Fire ${a_array}`)
    var m_array = Array.from(JSON.stringify(Object.keys(response.val())));
    console.log(`Fire ${m_array}`)*/

      // res.json({ data: {users: qqq} });
    })

    admin.auth().getUser(data).then(function(userRecord) {
      // See the UserRecord reference doc for the contents of userRecord.
      console.log("Successfully fetched user data:", userRecord.toJSON());
      res.json({ data: {users: userRecord.toJSON()} });
    })
    .catch(function(error) {
      console.log("Error fetching user data:", error);
    });
  
  
    
    
    /*return admin.database().ref('users').once('value').then(response => {
      res.status(200).send(response)
    });*/
  });