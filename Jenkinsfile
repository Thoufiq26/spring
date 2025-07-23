pipeline {
    agent any

    environment {
        APP_NAME = "springapp"
        QA_PORT = "8082"
        PREPROD_PORT = "8083"
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/Thoufiq26/spring.git'
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean package'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './mvnw test'
            }
        }

        stage('Deploy to QA') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo "Deploying to QA environment on port ${QA_PORT}"
                sh 'java -jar target/*.jar --server.port=8082 &'
            }
        }

        stage('Approval for Pre-Prod') {
            steps {
                input message: 'Approve deployment to Pre-Prod?', ok: 'Deploy'
            }
        }

        stage('Deploy to Pre-Prod') {
            steps {
                echo "Deploying to Pre-Prod on port ${PREPROD_PORT}"
                sh 'java -jar target/*.jar --server.port=8083 &'
            }
        }
    }
}
