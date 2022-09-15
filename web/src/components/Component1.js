import '../css/App.css'
import React from 'react'



function Component1(){
	const [counter, setCounter] = React.useState(0);
	return (
		<p>This is a component: {counter}</p>
	)
}

export default Component1