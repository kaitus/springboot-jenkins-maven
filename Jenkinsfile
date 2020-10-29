node {
    stage('Initialize'){
        def dockerHome = tool name: 'docker', type: 'dockerTool'
        env.PATH = "${dockerHome}/bin:${env.PATH}"
    }
    stage('Scm Checkout') {
        git credentialsId: 'd7be3735-e418-48f8-ac8a-8e220d54c042', url: 'https://github.com/kaitus/springboot-jenkins-maven.git'
    }
    stage('Mvc Package') {
        bat "mvn --version"
        bat "mvn clean package"
    }
    stage('Build docker image') {
        bat 'docker --version'
        //bat 'docker-compose up'
        //sh 'docker logs 05bf5a2ae0c9'
        //sh 'docker build -t carldihe/api-rest:dockerfile1 .'
    }
}