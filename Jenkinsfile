pipeline{
	agent any
	stages{
		stage ('Compile'){
			steps{
				bat 'mvn compile'
			}
			
		}
		stage ('Build'){
			steps{
				bat 'mvn -DskipTests=true package'
			}
			
		}
	}
}