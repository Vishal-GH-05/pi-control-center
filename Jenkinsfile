
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
                withCredentials([
                    usernamePassword(
                        credentialsId: 'pi-control-postgres',
                        usernameVariable: 'SPRING_DATASOURCE_USERNAME',
                        passwordVariable: 'SPRING_DATASOURCE_PASSWORD'
                    )
                ]) {
                    withEnv([
                        'SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:5432/pi-control-center',
                        'SPRING_DATA_REDIS_HOST=127.0.0.1',
                        'SPRING_DATA_REDIS_PORT=6379'
                    ]) {
                        sh '''
                            chmod +x mvnw
                            ./mvnw clean verify
                        '''
                    }
                }
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
