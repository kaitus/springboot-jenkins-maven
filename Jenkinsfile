node {
    stage('Scm Checkout') {
        git credentialsId: 'd7be3735-e418-48f8-ac8a-8e220d54c042', url: 'https://github.com/kaitus/springboot-jenkins-maven.git'
    }
    stage('Mvc Package') {
        def mvnHome = tool name: 'maven-3', type: 'maven'
        def mvnCMD = "${mvnHome}/bin/mvn"
        sh "${mvnCMD} clean package"
    }
    stage('Build docker image') {
        def dockerHome = tool name: 'docker', type: 'dockerTool'
        sh '${dockerHome} build -t carldihe/api-rest:0.0.1-SNAPSHOT .'
    }
}