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
		
	}
}