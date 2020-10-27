node {
    stage('Initialize'){
        def dockerHome = tool name: 'docker', type: 'dockerTool'
        def mavenHome  = tool name: 'maven-3', type: 'maven'
        env.PATH = "${dockerHome}/bin:${mavenHome}/bin:${env.PATH}"
    }
    stage('Scm Checkout') {
        git credentialsId: 'd7be3735-e418-48f8-ac8a-8e220d54c042', url: 'https://github.com/kaitus/springboot-jenkins-maven.git'
    }
    stage('Mvc Package') {
        sh "mvn clean package"
    }
    stage('Build docker image') {
        sh 'service docker start'
        sh 'docker build -t carldihe/api-rest:0.0.1-SNAPSHOT .'
    }
}