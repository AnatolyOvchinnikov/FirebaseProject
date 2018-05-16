const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

// Sends a notifications to all users when a new message is posted.
exports.sendNotifications = functions.database.ref('/comments/{postId}/{messageId}').onCreate((snapshot, context) => {
    // Notification details.
    // console.log(`Snapshot : ${JSON.stringify(snapshot)}`)
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



    admin.database().ref("tokenA").once("value").then(snapshot => {
        console.log(`Snapshot key : ${snapshot.key}`)
        console.log(`Snapshot val : ${snapshot.val()}`)
        console.log(`Snapshot val pr : ${JSON.stringify(snapshot.val())}`)
        console.log(`Snapshot : ${JSON.stringify(snapshot)}`)
        console.log('-----------');
      })


    return admin.database().ref('members/'+snapshot.val().postId).once('value').then(allMembers => {
      if (allMembers.val()) {
        members = Object.keys(allMembers.val());

        let result = processArray(members, context);
        return result
      }
    }).then(allTokens => {

      // console.log(`Snapshot key : ${allTokens.key}`)
      // console.log(`Snapshot val : ${allTokens.val()}`)
      // console.log(`Snapshot val pr : ${JSON.stringify(allTokens.val())}`)
      // console.log(`Snapshot : ${JSON.stringify(allTokens)}`)

      allTokens.forEach(item => {
        console.log(`Test ${item}`)
        console.log(`Test ${JSON.stringify(item)}`)
        console.log(`Key ${item.tokens}`)
        let tempArray = Object.keys(item.val())

        tempArray.forEach(item1 => {
          tokens.push(item1)
        })
      })

      /*// display all values
      for (var i = 0; i < tokens.length; i++) {
        console.log(tokens[i]);
      }*/

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

            
            /*admin.database().ref('users/').once('value').then(iter => {
              console.log(`Iter ${iter}`)
              console.log(`Iter pretty ${JSON.stringify(iter)}`)
            })

            admin.database().ref('users/').equalTo("valueme").once('value').then(snapshot => {
              snapshot.forEach(child => {
                const post = child.val();
                post.id = child.key;

                console.log("Test " + post.id)
              })
            })*/

            /*dmin.database().ref('/comments/One/').orderByChild("email").equalTo('mail2@mail.com').on('value', function(snapshot) {
              //snapshot would have list of NODES that satisfies the condition
              console.log(snapshot.val())
              console.log('-----------');

            //go through each item found and print out the emails
            snapshot.forEach(function(childSnapshot) {

            var key = childSnapshot.key;
            var childData = childSnapshot.val();

              //this will be the actual email value found
              console.log(childData.email);
            });

            })*/


            

/*
            admin.database().ref('/users/').orderByPriority().equalTo("ezLy-82iAR4:APA91bEMAbM8fLbSEPMsP39bCr9PCrB1djfsC0rzRPIUtPAGyMHvccEg-dpfldOCXvB711U_6KCJiVjhsVfrBXaUpNgFpqEfFCVp637sLVi9G1Y-5kgr2yeVVtAgF9vlY3wpxTmhF6GR").on('value', function(snapshot) {
              //snapshot would have list of NODES that satisfies the condition
              console.log(snapshot.val())
              console.log(`Snapshot : ${JSON.stringify(snapshot)}`)
              console.log('-----------');

            //go through each item found and print out the emails
            snapshot.forEach(function(childSnapshot) {

            var key = childSnapshot.key;
            var childData = childSnapshot.val();

              //this will be the actual email value found
              console.log(childData.email);
            });

            })*/

            

          }
        }
      });
      
      /*admin.database().ref("tokenA").once("value").then(snapshot => {
        console.log(`Snapshot key : ${snapshot.key}`)
        console.log(`Snapshot val : ${snapshot.val()}`)
        console.log(`Snapshot val pr : ${JSON.stringify(snapshot.val())}`)
        console.log(`Snapshot : ${JSON.stringify(snapshot)}`)
        console.log('-----------');
      })*/

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

  function processItem(userId) {
    return new Promise(resolve => {
      admin.database().ref('users/'+userId+'/tokens').once('value')
      .then(response => {
        // console.log("Process " + JSON.stringify(response));
        resolve({ userId: userId, tokens: Object.keys(response.val())})
      }
        )
      .catch(err => {
        console.error(userId, err);
        resolve();
      })
    })
  }