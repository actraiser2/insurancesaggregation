const BACKEND_URL = process.env['NODE_ENV'] === 'development' ?
	'http://localhost:9090' : 'http://localhost:9090';
	
const config = {
	BACKEND_URL: BACKEND_URL
}
export default config