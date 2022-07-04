pipeline{
	agent{
		docker{
			image 'maven:3.8.6-openjdk-18'
		}
	}
	stages{
		stage ('Compile'){
			steps{
				bat 'mvn compile'
			}
			
		}
		stage ('Build image & publish it'){
			steps{
				bat 'mvn -DskipTests=true build-image'
			}
			
		}
		
	}
}