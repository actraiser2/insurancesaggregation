pipeline{
	agent any
	stages{
		stage ('Compile'){
			steps{
				bat 'mvn compile'
			}
			
		}
		stage ('Build image & publish it'){
			steps{
				bat 'mvn -DskipTests=true spring-boot:build-image'
			}
			
		}
		stage('apply manifets'){
			steps{
				bat 'kubectl apply -f '-/kubernetes/*'
			}
		}
		stage('List pods deployed'){
			steps{
				bat 'kubectl get pods'
			}
		}
		
	}
}