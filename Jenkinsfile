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
                    
                    // Start QA instance in a persistent way
                    bat """
                        set JAVA_CMD=java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --spring.profiles.active=qa --server.port=${QA_PORT}
                        echo Starting QA instance: %JAVA_CMD%
                        start \"QA Instance\" /B cmd /c \"%JAVA_CMD%\" ^& exit
                    """
                    
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
                    
                    // Start Pre-Prod instance in a persistent way
                    bat """
                        set JAVA_CMD=java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --spring.profiles.active=preprod --server.port=${PREPROD_PORT}
                        echo Starting Pre-Prod instance: %JAVA_CMD%
                        start \"Pre-Prod Instance\" /B cmd /c \"%JAVA_CMD%\" ^& exit
                    """
                    
                    // Wait for application to start
                    sleep(time: 30, unit: "SECONDS")
                    
                    // Verify health check
                    bat "curl -f http://localhost:${PREPROD_PORT}/actuator/health || exit 1"
                }
            }
        }
    }
    post {
        success {
            echo 'Pipeline completed successfully! Both instances should continue running:'
            echo "QA: http://localhost:${QA_PORT}"
            echo "Pre-Prod: http://localhost:${PREPROD_PORT}"
            // Add instructions to stop them later if needed
            echo 'To stop these instances later, run:'
            echo 'taskkill /FI "WINDOWTITLE eq QA Instance*" /T /F'
            echo 'taskkill /FI "WINDOWTITLE eq Pre-Prod Instance*" /T /F'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
            // Clean up any running instances
            bat "taskkill /FI \"WINDOWTITLE eq QA Instance*\" /T /F || exit 0"
            bat "taskkill /FI \"WINDOWTITLE eq Pre-Prod Instance*\" /T /F || exit 0"
        }
    }
}
