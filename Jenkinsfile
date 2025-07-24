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
                script {
                    // Kill any existing process on QA port
                    try {
                        bat """
                            for /f \"tokens=5\" %%i in ('netstat -aon ^| findstr :${QA_PORT}') do taskkill /F /PID %%i
                        """
                    } catch (Exception e) {
                        echo "No process found on port ${QA_PORT}"
                    }
                    
                    // Start QA instance in the background
                    bat "start \"QA Instance\" cmd /c java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --spring.profiles.active=qa --server.port=${QA_PORT}"
                    
                    // Wait for application to start
                    sleep(time: 30, unit: "SECONDS")
                    
                    // Verify health check
                    bat "curl -f http://localhost:${QA_PORT}/actuator/health || exit 1"
                }
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
                script {
                    // Kill any existing process on Pre-Prod port
                    try {
                        bat """
                            for /f \"tokens=5\" %%i in ('netstat -aon ^| findstr :${PREPROD_PORT}') do taskkill /F /PID %%i
                        """
                    } catch (Exception e) {
                        echo "No process found on port ${PREPROD_PORT}"
                    }
                    
                    // Start Pre-Prod instance in the background
                    bat "start \"Pre-Prod Instance\" cmd /c java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --spring.profiles.active=preprod --server.port=${PREPROD_PORT}"
                    
                    // Wait for application to start
                    sleep(time: 30, unit: "SECONDS")
                    
                    // Verify health check
                    bat "curl -f http://localhost:${PREPROD_PORT}/actuator/health || exit 1"
                }
            }
        }
    }
    post {
        always {
            echo 'Pipeline execution completed.'
        }
        success {
            echo 'Pipeline completed successfully! Both QA and Pre-Prod instances should be running.'
            echo "QA: http://localhost:${QA_PORT}"
            echo "Pre-Prod: http://localhost:${PREPROD_PORT}"
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
            // Clean up any running instances
            bat "taskkill /FI \"WINDOWTITLE eq QA Instance*\" /T /F || exit 0"
            bat "taskkill /FI \"WINDOWTITLE eq Pre-Prod Instance*\" /T /F || exit 0"
        }
    }
}
