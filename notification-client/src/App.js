import "./App.css";
import { useState } from "react";
import { getToken, onMessageListener } from "./firebase";
import { Button, Form, Card } from "react-bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";

function App() {
  const [show, setShow] = useState(false);
  const [notification, setNotification] = useState([]);
  const [isTokenFound, setTokenFound] = useState(false);
  const [username, setUsername] = useState("");

  onMessageListener()
    .then((payload) => {
      setShow(true);
      setNotification((oldArray) => [
        { title: payload.notification.title, body: payload.notification.body },
        ...oldArray,
      ]);
      console.log(payload);
    })
    .catch((err) => console.log("failed: ", err));

  const handleSubmit = (evt) => {
    console.log(notification);
    evt.preventDefault();
    alert(`Submitting Name ${username} `);
    getToken(setTokenFound, username);
  };
  const onChange = (event) => {
    setUsername(event.target.value);
  };

  return (
    <div className="App">
      <header className="App-header">
        <div>
          {!isTokenFound && (
            <Form>
              <Form.Group
                className="mb-3"
                controlId="exampleForm.ControlInput1"
              >
                <Form.Label>username</Form.Label>
                <Form.Control
                  value={username}
                  onChange={onChange}
                  placeholder="username"
                />
              </Form.Group>
              <Button onClick={handleSubmit}>Submit</Button>
            </Form>
          )}
          <div>
            {isTokenFound && <h1> Notification permission enabled 👍🏻 </h1>}
            {!isTokenFound && <h1> Need notification permission ❗️ </h1>}
            {isTokenFound && (
              <Button onClick={() => setShow(!show)}>Show Notifications</Button>
            )}
          </div>
        </div>
        {
          <div>
            {show &&
              notification.map((obj, index) => {
                return (
                  <Card
                    style={{
                      marginTop: "10px",
                      width: "35rem",
                      backgroundColor: "#343a40",
                      borderColor: "#282c34",
                    }}
                    key={index}
                  >
                    <Card.Body>
                      <Card.Title>{obj.title}</Card.Title>
                      <Card.Text>{obj.body}</Card.Text>
                      <Button
                        variant="primary"
                        onClick={() =>
                          setNotification((oldArray) => [
                            ...oldArray.slice(0, index),
                            ...oldArray.slice(index + 1, oldArray.length),
                          ])
                        }
                      >
                        Delete
                      </Button>
                    </Card.Body>
                  </Card>
                );
              })}
          </div>
        }
      </header>
    </div>
  );
}

export default App;
