import './css/App.css';
import 'bootstrap/dist/css/bootstrap.css';

let currentDate = new Date()
let f = x => x*x;
console.log(`Current Date: ${currentDate}: ${f(2)}`)
function App(props) {
  return (
    <div className="container">
	    <h1>Hello World !!!</h1>
	    <p>Date: {props.username} </p>
	</div>
  );
}

export default App

