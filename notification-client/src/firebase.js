import firebase from "firebase/app";
import "firebase/messaging";
import "firebase/firestore";
import axios from "axios";

var firebaseConfig = {
  apiKey: "AIzaSyD0dQIsSM2S-ToVv3NP1hqahEO3Nfkjbeo",
  authDomain: "reddit-guc.firebaseapp.com",
  projectId: "reddit-guc",
  storageBucket: "reddit-guc.appspot.com",
  messagingSenderId: "1072681859465",
  appId: "1:1072681859465:web:8898b5300e2a82c313c958",
  measurementId: "G-W3Y9L5DVM6",
};

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

export const getToken = (setTokenFound, username) => {
  return messaging
    .getToken()
    .then(async (currentToken) => {
      if (currentToken) {
        await axios
          .create({
            baseURL: "localhost:8080",
            headers: {
              "Function-Name": "REGISTER_DEVICE_TOKEN",
              "Content-Type": "application/json",
              "Access-Control-Allow-Origin": "*",
            },
          })
          .post(
            "/api/notification",
            { crossDomain: true },
            {
              username: username,
              token: currentToken,
            }
          )
          .then((res) => {
            if (res.status === 200) {
              setTokenFound(true);
            }
          })
          .catch((err) => console.log(err));
        console.log("current token for client: ", currentToken);
      } else {
        console.log(
          "No registration token available. Request permission to generate one."
        );
        setTokenFound(false);
      }
    })
    .catch((err) => {
      console.log("An error occurred while retrieving token. ", err);
    });
};

export const onMessageListener = () =>
  new Promise((resolve) => {
    messaging.onMessage((payload) => {
      resolve(payload);
    });
  });
