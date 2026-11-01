pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Test') {
            steps {
                sh '''
                    chmod +x mvnw
                    ./mvnw clean verify
                '''
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'pi-control-postgres',
                        usernameVariable: 'SPRING_DATASOURCE_USERNAME',
                        passwordVariable: 'SPRING_DATASOURCE_PASSWORD'
                    ),
                    string(
                        credentialsId: 'pi-control-db-url',
                        variable: 'SPRING_DATASOURCE_URL'
                    )
                ]) {
                    sh 'docker compose up -d --remove-orphans'
                }
            }
        }
    }

    post {
        success {
            echo 'Pi Control Center frontend and backend deployed successfully!'
        }
        failure {
            echo 'Pi Control Center pipeline failed. Check the console output.'
        }
    }
}
