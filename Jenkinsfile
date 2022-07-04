pipeline{
	agent any
	stages{
		stage ('Compile'){
			steps{
				bat 'mvn compile'
			}
			
		}
		stage ('Build artifact'){
			steps{
				bat 'mvn -DskipTests=true package'
			}
			
		}
		stage ('Build image & upload it in the registry'){
			steps{
				bat 'mvn jib:build'
			}
			
		}
		stage('delete manifets'){
			steps{
				bat 'kubectl delete -f kubernetes'
			}
		}
		stage('apply manifets'){
			steps{
				bat 'kubectl apply -f kubernetes'
			}
		}
		stage('List pods deployed'){
			steps{
				bat 'kubectl get pods'
			}
		}
		
	}
}