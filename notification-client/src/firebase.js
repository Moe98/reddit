import firebase from 'firebase/app';
import 'firebase/messaging';
import 'firebase/firestore';

var firebaseConfig = {
    apiKey: "AIzaSyD0dQIsSM2S-ToVv3NP1hqahEO3Nfkjbeo",
    authDomain: "reddit-guc.firebaseapp.com",
    projectId: "reddit-guc",
    storageBucket: "reddit-guc.appspot.com",
    messagingSenderId: "1072681859465",
    appId: "1:1072681859465:web:8898b5300e2a82c313c958",
    measurementId: "G-W3Y9L5DVM6"
};

firebase.initializeApp(firebaseConfig);
var db = firebase.firestore();
console.log("db",db.collection("userTokens"))
const messaging = firebase.messaging();

export const getToken = (setTokenFound,flag,setFlag) => {
    return messaging.getToken().then((currentToken) => {
      if (currentToken) {
        console.log('current token for client: ', currentToken);
        if(!flag){
        db.collection("userTokens").doc("zizo").set({
            username: "zizo",
            token: currentToken
         })
    .then((docRef) => {
        console.log("Document written with ID");
    })
    .catch((error) => {
        console.error("Error adding document: ", error);
        setFlag(false)
    });
    }
    setFlag(true)


        setTokenFound(true);
        // Track the token -> client mapping, by sending to backend server
        // show on the UI that permission is secured
      } else {
        console.log('No registration token available. Request permission to generate one.');
        setTokenFound(false);
        // shows on the UI that permission is required 
      }
    }).catch((err) => {
      console.log('An error occurred while retrieving token. ', err);
      // catch error while creating client token
    });
  }


  export const onMessageListener = () =>
  new Promise((resolve) => {
    messaging.onMessage((payload) => {
      resolve(payload);
    });
});