// CI/CD-конвейер: сборка Docker-образа Flask-приложения, тестирование,
// деплой в staging, проверка доступности и деплой в production после ручного подтверждения.
pipeline {
    agent any

    environment {
        IMAGE_NAME   = 'flask-cicd-demo'
        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        DOCKER_NET   = 'cicd-net'
        STAGING_PORT = '8081'
        PROD_PORT    = '8082'
        IT_CONTAINER = "flask-it-${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 40, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
                sh 'chmod +x scripts/*.sh'
            }
        }

        stage('Build image') {
            steps {
                echo "Сборка образа ${IMAGE_NAME}:${IMAGE_TAG}"
                sh '''
                    docker build --target runtime \
                        --build-arg APP_VERSION=${IMAGE_TAG} \
                        -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${IMAGE_NAME}:latest .
                    docker build --target test -t ${IMAGE_NAME}-test:${IMAGE_TAG} .
                    docker image ls ${IMAGE_NAME}
                '''
            }
        }

        stage('Test') {
            steps {
                echo 'Модульные тесты (pytest) внутри контейнера'
                sh 'docker run --rm ${IMAGE_NAME}-test:${IMAGE_TAG}'

                echo 'Интеграционная проверка: запуск контейнера из собранного образа'
                sh '''
                    docker network inspect ${DOCKER_NET} >/dev/null 2>&1 || docker network create ${DOCKER_NET}
                    docker run -d --name ${IT_CONTAINER} --network ${DOCKER_NET} ${IMAGE_NAME}:${IMAGE_TAG}
                    ./scripts/smoke_test.sh http://${IT_CONTAINER}:5000
                '''
            }
            post {
                always {
                    sh 'docker rm -f ${IT_CONTAINER} || true'
                }
            }
        }

        stage('Deploy to Staging') {
            steps {
                sh './scripts/deploy.sh staging ${IMAGE_NAME}:${IMAGE_TAG}'
            }
        }

        stage('Verify Staging') {
            steps {
                sh './scripts/smoke_test.sh http://flask-staging:5000'
                echo "Staging доступен: http://localhost:${STAGING_PORT}"
            }
        }

        stage('Approve Production') {
            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    input message: "Развернуть сборку #${env.BUILD_NUMBER} в production?", ok: 'Развернуть'
                }
            }
        }

        stage('Deploy to Production') {
            steps {
                sh './scripts/deploy.sh production ${IMAGE_NAME}:${IMAGE_TAG}'
                sh './scripts/smoke_test.sh http://flask-production:5000'
                echo "Production доступен: http://localhost:${PROD_PORT}"
            }
        }
    }

    post {
        success {
            echo "Сборка #${env.BUILD_NUMBER} развёрнута в production"
        }
        failure {
            echo "Сборка #${env.BUILD_NUMBER} провалилась, подробности: ${env.BUILD_URL}console"
            script {
                // Письмо уходит, если в задании задана переменная NOTIFY_EMAIL
                // и в Jenkins настроен SMTP (Настроить Jenkins -> System -> Уведомление по почте)
                if (env.NOTIFY_EMAIL) {
                    mail to: env.NOTIFY_EMAIL,
                         subject: "${env.JOB_NAME} – сборка № ${env.BUILD_NUMBER} провалилась",
                         body: "Подробности в консольном выводе: ${env.BUILD_URL}console"
                }
            }
        }
        always {
            sh 'docker image rm ${IMAGE_NAME}-test:${IMAGE_TAG} || true'
        }
        cleanup {
            cleanWs()
        }
    }
}
