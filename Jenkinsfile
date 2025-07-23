pipeline {
    agent any
    tools {
        maven 'Maven'
        jdk 'JDK' // Configured for Java 21
    }
    environment {
        APP_NAME = "first"
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
                bat """
                    netstat -aon | findstr :${QA_PORT} > nul && (
                        for /f \"tokens=5\" %%i in ('netstat -aon ^| findstr :${QA_PORT}') do taskkill /F /PID %%i
                    ) || exit 0
                """
                bat "java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --spring.profiles.active=qa --server.port=${QA_PORT}"
                bat 'ping 127.0.0.1 -n 60 > nul' // Increased delay to 60 seconds
                bat "curl -f http://localhost:${QA_PORT}/actuator/health || exit 1"
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
                bat """
                    netstat -aon | findstr :${PREPROD_PORT} > nul && (
                        for /f \"tokens=5\" %%i in ('netstat -aon ^| findstr :${PREPROD_PORT}') do taskkill /F /PID %%i
                    ) || exit 0
                """
                bat "java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --spring.profiles.active=preprod --server.port=${PREPROD_PORT}"
                bat 'ping 127.0.0.1 -n 60 > nul' // Increased delay to 60 seconds
                bat "curl -f http://localhost:${PREPROD_PORT}/actuator/health || exit 1"
            }
        }
    }
    post {
        always {
            echo 'Pipeline execution completed.'
            // bat 'taskkill /F /IM java.exe /T || exit 0' // Commented out to keep app running for debugging
        }
        success {
            echo 'Pipeline completed successfully! Application remains running.'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}