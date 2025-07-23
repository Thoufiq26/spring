pipeline {
    agent any
    tools {
        maven 'Maven' // Ensure 'Maven' is configured in Jenkins
        jdk 'JDK'     // Ensure 'JDK' is configured in Jenkins
    }
    environment {
        APP_NAME = "springapp"
        QA_PORT = "8082"
        PREPROD_PORT = "8083"
    }
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/Thoufiq26/spring.git', branch: 'main'
            }
        }
        stage('Build') {
            steps {
                bat 'mvnw.cmd clean package'
            }
        }
        stage('Unit Tests') {
            steps {
                bat 'mvnw.cmd test'
            }
        }
        stage('Deploy to QA') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo "Deploying to QA environment on port ${QA_PORT}"
                bat "start /B java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --server.port=${QA_PORT}"
                // Wait for the app to start
                bat 'ping 127.0.0.1 -n 11 > nul'
                // Verify QA deployment
                bat "curl -f http://localhost:${QA_PORT} || exit 1"
            }
        }
        stage('Approval for Pre-Prod') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                input message: 'Approve deployment to Pre-Prod?', ok: 'Deploy'
            }
        }
        stage('Deploy to Pre-Prod') {
            steps {
                echo "Deploying to Pre-Prod on port ${PREPROD_PORT}"
                bat "start /B java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --server.port=${PREPROD_PORT}"
                // Wait for the app to start
                bat 'ping 127.0.0.1 -n 11 > nul'
                // Verify Pre-prod deployment
                bat "curl -f http://localhost:${PREPROD_PORT} || exit 1"
            }
        }
    }
    post {
        always {
            // Clean up running Java processes
            bat 'taskkill /F /IM java.exe /T || exit 0'
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}
