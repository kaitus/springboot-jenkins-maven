node {
    stage('Scm Checkout') {
        git credentialsId: 'd7be3735-e418-48f8-ac8a-8e220d54c042', url: 'https://github.com/kaitus/springboot-jenkins-maven.git'
    }
    stage('Mvc Package') {
        sh 'mvn clean package'
    }
}