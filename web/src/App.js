import './css/App.css';
import 'bootstrap/dist/css/bootstrap.css';
import React, {useState, useEffect} from 'react';
import Modal from 'react-modal'
import DatePicker from 'react-date-picker'
import config from './Config'
import {DateTime} from 'luxon'


function App(props) {
  
  const [executions, setExecutions] = useState([])
  const [execution, setExecution] = useState({});
  const [counter, setCounter] = useState(0)
  const [modalIsOpen, setModalIsOpen] = useState(false)
  const [fromDate, setFromDate] = useState(new Date())
  const [toDate, setToDate] = useState(new Date())
  
  const modalCustomStyles = {
	  content : {
	    top                   : '50%',
	    left                  : '50%',
	    right                 : 'auto',
	    bottom                : 'auto',
	    marginRight           : '-50%',
	    transform             : 'translate(-50%, -50%)'
	  }
	};
  
  useEffect(() => {
	let fromDateAsString = DateTime.fromJSDate(fromDate).toISODate();
	console.log("fromDateAsString:" + fromDateAsString)
	let toDateAsString = DateTime.fromJSDate(toDate).toISODate();
	let query = {
		query: `query{executions(size:100, fromDate:\"${fromDateAsString}\", toDate:\"${toDateAsString}\"){` + 
		 "companyName,id,executionStatus,executionDate,username,totalDuration"  +
		 "}}"
	}
	fetch(config.BACKEND_URL + '/graphql',{
		'method':'POST',
		'headers':{'Content-Type':'application/json'},
		'body':JSON.stringify(query)
	}).then(r => r.json()).then(d => {
		setExecutions(d.data.executions)
	})
  },[counter]);
  
  
  const executionDetails = (executionId) => {
	console.log("Getting the execution details:" + executionId)
	
	var query = {
		query: `query{execution(executionId:${executionId}){companyName, totalDuration, id, executionStatus, executionDate}}`
	}
	fetch(config.BACKEND_URL + '/graphql',{
		'method':'POST',
		'headers':{'Content-Type':'application/json'},
		'body':JSON.stringify(query)
	}).then(r => r.json()).
		then(d => {
			setExecution(d.data.execution);
			setModalIsOpen(true)
			console.log(fromDate)
		})
  }
  
  

  return (
    <div className="container">
	   <div className="jumbotron">
  			<h1 className="display-4">Last Executions</h1>
  			<p className="lead">Here, you can view the last executions launched against the Insurance companies</p>
  			<hr className="my-4"></hr>
 
 			<div className="card">
 				<div className="card-body">
 					<form className="w-75">
		 				<div className="row">
		 					<div className="col">
		 						<label>Select Start Date</label>
			 					<DatePicker value={fromDate} className="form-control" 
			 						onChange={v => setFromDate(v)}>
			 					</DatePicker>
		 					</div>
		 					
		 					<div className="col">
		 						<label>Select End Date</label>
		 						<DatePicker value={toDate} className="form-control" onChange={v => setToDate(v)}>
		 						</DatePicker>
		 					</div>
 					
			 			</div>
		 				
		 			</form>
		 				</div>
 			</div>
 			
 			
		</div>
		
	  <button className="btn btn-primary mb-2 mt-2" onClick={() => setCounter(counter + 1)}>Refresh executions</button>

	   <table className="table table-striped table-bordered">
	   		<thead>
	   			<tr>
	   				<th>Id</th>
	   				<th>Compnay Name</th>
	   				<th>Execution Status</th>
	   				<th>Execution Date</th>
	   				<th>Username</th>
	   				<th>Total Duration (Seg)</th>
	   			</tr>
	   		</thead>
	   		<tbody>
	   			{executions.map((e, i) => {
					return (
						<tr key={i}>
							<td ><a href="#" onClick={() => executionDetails(e.id)}>{e.id}</a></td>
							<td>{e.companyName}</td>
							<td>{e.executionStatus}</td>
							<td>{e.executionDate}</td>
							<td>{e.username}</td>
							<td>{e.totalDuration}</td>
						</tr>
					)
				})
				}
	   		</tbody>
	   </table>
	    <Modal
          ariaHideApp={false}
          isOpen={modalIsOpen}
          style={modalCustomStyles}
          contentLabel="Execution Details">
          <p className="lead">Execution Details</p>
          <ul className="list-group">
			  <li className="list-group-item">Execution Id: {execution.id}</li>
			  <li className="list-group-item">Company Name: {execution.companyName}</li>
			  <li className="list-group-item">Execution Status: {execution.executionStatus}</li>
			  <li className="list-group-item">Execution Date: {execution.executionDate}</li>
		</ul>
          
           <button className ="btn btn-primary mt-3" onClick={() => setModalIsOpen(false)}>close</button>
        </Modal>

	</div>
  );
}

export default App

