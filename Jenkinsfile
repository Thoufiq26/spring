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
        LOG_DIR = "${WORKSPACE}/logs"
        TEST_REPO_DIR = "${WORKSPACE}/spring-integration-tests"
    }
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/Thoufiq26/spring.git', branch: 'main'
            }
        }
        stage('Create Log Directory') {
            steps {
                bat 'mkdir "%LOG_DIR%" || exit 0'
            }
        }
        stage('Build') {
            steps {
                bat 'mvnw.cmd clean package'
            }
        }
        stage('Unit Tests') {
            steps {
                bat 'mvnw.cmd surefire:test'
            }
            post {
                always {
                    echo 'Unit Tests completed'
                }
            }
        }
        stage('Deploy to QA') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo "Deploying to QA environment on port ${QA_PORT}"
                script {
                    try {
                        bat """
                            for /f \"tokens=5\" %%i in ('netstat -aon ^| findstr :${QA_PORT}') do taskkill /F /PID %%i
                        """
                    } catch (Exception e) {
                        echo "No process found on port ${QA_PORT}"
                    }
                    bat """
                        set JAVA_CMD=java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --spring.profiles.active=qa --server.port=${QA_PORT}
                        echo Starting QA instance: %JAVA_CMD%
                        start \"QA_Instance_${BUILD_ID}\" /B cmd /c \"%JAVA_CMD% > ${LOG_DIR}\\qa.log 2>&1\"
                    """
                    sleep(time: 60, unit: "SECONDS")
                    bat """
                        curl -f http://localhost:${QA_PORT}/students/health | findstr \"\\\"status\\\":\\\"UP\\\"\" | findstr \"\\\"stage\\\":\\\"qa\\\"\" || exit 1
                    """
                    echo "QA is running on http://localhost:${QA_PORT}/students/health"
                    bat """
                        netstat -aon | findstr :${QA_PORT} || exit 1
                    """
                }
            }
        }
        stage('Integration Tests Post-QA') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                echo "Running integration tests against QA environment"
                script {
                    // Clone test repository
                    bat """
                        if exist "%TEST_REPO_DIR%" rmdir /S /Q "%TEST_REPO_DIR%"
                        git clone https://github.com/Thoufiq26/spring-integration-tests.git "%TEST_REPO_DIR%"
                    """
                    // Run integration tests
                    bat """
                        cd "%TEST_REPO_DIR%"
                        mvnw.cmd clean failsafe:integration-test failsafe:verify
                    """
                }
            }
            post {
                always {
                    echo 'Integration Tests Post-QA completed'
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
                    try {
                        bat """
                            for /f \"tokens=5\" %%i in ('netstat -aon ^| findstr :${PREPROD_PORT}') do taskkill /F /PID %%i
                        """
                    } catch (Exception e) {
                        echo "No process found on port ${PREPROD_PORT}"
                    }
                    bat """
                        set JAVA_CMD=java -jar target/${APP_NAME}-0.0.1-SNAPSHOT.jar --spring.profiles.active=preprod --server.port=${PREPROD_PORT}
                        echo Starting Pre-Prod instance: %JAVA_CMD%
                        start \"PreProd_Instance_${BUILD_ID}\" /B cmd /c \"%JAVA_CMD% > ${LOG_DIR}\\preprod.log 2>&1\"
                    """
                    sleep(time: 60, unit: "SECONDS")
                    bat """
                        curl -f http://localhost:${PREPROD_PORT}/students/health | findstr \"\\\"status\\\":\\\"UP\\\"\" | findstr \"\\\"stage\\\":\\\"preprod\\\"\" || exit 1
                    """
                    echo "Pre-Prod is running on http://localhost:${PREPROD_PORT}/students/health"
                    bat """
                        netstat -aon | findstr :${PREPROD_PORT} || exit 1
                    """
                }
            }
        }
    }
    post {
        success {
            echo 'Pipeline completed successfully! Both instances are running:'
            echo "QA: http://localhost:${QA_PORT}/students/health (logs: ${LOG_DIR}\\qa.log)"
            echo "Pre-Prod: http://localhost:${PREPROD_PORT}/students/health (logs: ${LOG_DIR}\\preprod.log)"
            echo "To stop these instances, run:"
            echo " taskkill /FI \"WINDOWTITLE eq QA_Instance_${BUILD_ID}\" /T /F"
            echo " taskkill /FI \"WINDOWTITLE eq PreProd_Instance_${BUILD_ID}\" /T /F"
        }
        failure {
            echo 'Pipeline failed. Check logs for details: ${LOG_DIR}\\qa.log and ${LOG_DIR}\\preprod.log'
            bat "taskkill /FI \"WINDOWTITLE eq QA_Instance_*\" /T /F || exit 0"
            bat "taskkill /FI \"WINDOWTITLE eq PreProd_Instance_*\" /T /F || exit 0"
        }
    }
}
