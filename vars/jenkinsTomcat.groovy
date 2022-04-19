def call(String registryin = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a') {
pipeline {
environment { 
    registry = "$registryin"   
    dockerTag = "${docTag}$BUILD_NUMBER"
    gitRepo = "${grepo}"
    gitBranch = "${gbranch}"
  }
  
  agent none
  
  stages {
	stage('check out') {
		agent {label 'docker'}
		steps {
			checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/Megha494/tomcat-config.git']]])
		}
	}

	stage('BUILD IMAGE') {
		agent {label 'docker'}
		steps {
			script {
				dockerImage=docker.build("'$registry:$dockerTag'")
			}
		}
	}

	stage('PUSH TO DOCKER HUB') {
		agent {label 'docker'}
		steps {
			dockerImage.push()
		
		}
	}

	stage('DEPLOY') {
		agent {label 'kubernetes'}
		steps {
			sh 'kubectl set image deployment webapp-deployment tomcat="$registry:$dockerTag" --record'
		}
	}
}
}
  