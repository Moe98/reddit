import firebase from "firebase/app";
import "firebase/messaging";
import "firebase/firestore";

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
  const AUTH_TOKEN = getAuthToken(username);
  return messaging
    .getToken()
    .then(async (currentToken) => {
      if (currentToken) {
        fetch("http://34.116.182.85/api/notification", {
          method: "POST",
          headers: {
            "Content-Type": "appliaction/json",
            "Function-Name": "REGISTER_DEVICE_TOKEN",
            Authorization: `Bearer ${AUTH_TOKEN}`,
          },
          body: JSON.stringify({
            username: username,
            token: currentToken,
          }),
        })
          .then((res) => {
            console.log(res);
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

const jwtTokens = [
  "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvcmcuc2FiIiwiZXhwIjo2MTYyMzUzMTA1MywidXNlcklkIjoiYWRhZmU4YjgtNzliMS00ODVhLWIxZjgtM2Y2N2MxMWI0YzZjIiwiaWF0IjoxNjIzNTMxMDUzLCJ1c2VybmFtZSI6InJvbmljIn0.TvLDUPQPb7-OwKClMIVxzs2rxIXrXqxmTO1v8Npfuq4aQ0iBSEBe6JZp_WYgRzIrPKFdbWzJbTP4TZ-MovwdSg",
  "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvcmcuc2FiIiwiZXhwIjo2MTYyMzUzMTA1MywidXNlcklkIjoiNjU4ZjNlMGMtZDFkZi00MmMwLThjMDMtMDAxYjRhOGViNjMwIiwiaWF0IjoxNjIzNTMxMDUzLCJ1c2VybmFtZSI6Im91ZGEifQ.HL8kwF-ZTkYOMwAppJfzLmNRtFq6Z2TbWfiZ3q3Bo-zlMOfAmcaE9uDrs6a_sH4Ryy18VM3QtZwAC9aNm7eC1Q",
  "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvcmcuc2FiIiwiZXhwIjo2MTYyMzUzMTA1MywidXNlcklkIjoiMmQyMDg5ZmYtYjVjNi00Njk1LWFlM2MtMWRmOTAxYzU4ZTNkIiwiaWF0IjoxNjIzNTMxMDUzLCJ1c2VybmFtZSI6ImpvZSJ9.20mxUyqsqzLSyXjbUYCDz-8nj0fWaAEX8DKaAwXUYxVzH5BvFxQwqOcFrusiv2i_8hr6XAlFBfD0qdTBhcCsNg",
  "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJvcmcuc2FiIiwiZXhwIjo2MTYyMzUzMTA1MywidXNlcklkIjoiNmI1NzU2MmItMzY2Ny00MjZjLWFlNmEtMzcyYzhlYTZmZjkxIiwiaWF0IjoxNjIzNTMxMDUzLCJ1c2VybmFtZSI6ImFidSJ9.t-fFHMBaSb_zHACcQ615auryJVSPP_HzlhrgDO4muT0bMq8RS0Fp4HfKieHSHZex1hxdOL0S63OlDhNcgYF94Q",
];

const getAuthToken = (username) => {
  switch (username) {
    case "abu":
      return jwtTokens[3];
    case "joe":
      return jwtTokens[2];
    case "ouda":
      return jwtTokens[1];
    case "ronic":
      return jwtTokens[0];
    default:
      return username;
  }
};
