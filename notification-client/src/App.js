import logo from './logo.svg';
import './App.css';
import {useState,useEffect,useInput} from 'react';
import { getToken, onMessageListener } from './firebase';
import {Button, Toast,Form} from 'react-bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
// import { useInput } from './hooks/input-hook';


function App() {

  const [show, setShow] = useState(false);
  // const [notification, setNotification] = useState({title: '', body: ''});
  const [notification, setNotification] = useState([]);
  const [isTokenFound, setTokenFound] = useState(false);
  // const [flag,setFlag] = useState(false);
  const [username, setUsername] = useState('');



  onMessageListener().then(payload => {
    setShow(true);
    // setNotification({title: payload.notification.title, body: payload.notification.body})
    setNotification(oldArray => [{title: payload.notification.title, body: payload.notification.body},...oldArray]);
    console.log(payload);
  }).catch(err => console.log('failed: ', err));


  const handleSubmit = (evt) => {
    evt.preventDefault();
    alert(`Submitting Name ${username} `);
    getToken(setTokenFound,username);

}
const onChange = (event) => {
  setUsername(event.target.value);
};

  return (
    <div className="App">
      {notification.map((obj ,index)=>{
              return  <Toast onClose={() => setShow(false)} show={show} delay={3000} autohide animation style={{
                  position: 'relative',
                  top: 20,
                  right: 20,
                  minWidth: 200
                }}>
                  <Toast.Header>
                    <img
                      src="holder.js/20x20?text=%20"
                      className="rounded mr-2"
                      alt=""
                    />
                    <strong className="mr-auto">{obj.title}</strong>
                    <small>just now</small>
                  </Toast.Header>
                  <Toast.Body>{obj.body}</Toast.Body>
                </Toast>
      })}
      <header className="App-header">
        <div>
      <Form>
        <Form.Group className="mb-3" controlId="exampleForm.ControlInput1">
          <Form.Label>User Name</Form.Label>
          <Form.Control value={username} onChange={onChange}  placeholder="username" />
        </Form.Group>
        <Button onClick={handleSubmit}>Submit</Button>

    </Form>
    <div>
    {isTokenFound && <h1> Notification permission enabled 👍🏻 </h1>}
        {!isTokenFound && <h1> Need notification permission ❗️ </h1>}
        {/* <img src={logo} className="App-logo" alt="logo" /> */}
    {isTokenFound&& <Button onClick={() => setShow(true)}>Show Notifications</Button>}
    
    </div>
    </div>
      </header>


    </div>
  );
}

export default App;