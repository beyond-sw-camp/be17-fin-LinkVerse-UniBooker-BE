def modules = [
    'api-app',
    'api-discovery',
    'api-gateway',
    'api-queue',
    'api-reservation',
    'api-resource',
    'api-statistics'
]

// 이 함수는 Scripted Pipeline 문법을 사용합니다.
def buildAndDeploy(moduleName) {
    // 1. Git Change Detection (Scripted Logic)
    def changedFiles = sh(returnStdout: true, script: "git diff --name-only HEAD~1 HEAD").trim()
    if (!changedFiles.contains("${moduleName}/") && !changedFiles.contains("common/")) {
        echo "Skipping ${moduleName}: No relevant changes detected."
        return // 변경 사항이 없으면 함수 종료
    }

    // 2. CI/CD Logic (Scripted Steps)
    def PROJECT_NAME = "unibooker"
    def IMAGE_TAG = "${env.BUILD_NUMBER}"
    def MANIFEST_PATH = "${moduleName}/k8s/backend-${moduleName.split('-')[1]}-rollout.yaml"
    def DOCKER_IMAGE = "${env.DOCKER_REGISTRY}/${PROJECT_NAME}/${moduleName}:${IMAGE_TAG}"

    echo "--- Starting CI/CD for Module: ${moduleName} ---"

    // [Step 1] Gradle Build
    sh "./gradlew :${moduleName}:clean :${moduleName}:build -x test"

    // [Step 2] Docker Build & Push
    docker.withRegistry("https://${env.DOCKER_REGISTRY}", "${env.DOCKER_CREDENTIAL_ID}") {
        def image = docker.build("${DOCKER_IMAGE}", "-f ${moduleName}/Dockerfile .")
        image.push()
        image.push("${env.DOCKER_REGISTRY}/${PROJECT_NAME}/${moduleName}:latest")
    }

    // [Step 3] Update Manifest (GitOps)
    withCredentials([usernamePassword(credentialsId: env.GIT_CREDENTIAL_ID, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
        sh """
            git config user.email 'jenkins@yourcompany.com'
            git config user.name 'Jenkins Bot'

            sed -i.bak 's|image: .*${moduleName}:.*|image: ${DOCKER_IMAGE}|' ${MANIFEST_PATH}

            git add ${MANIFEST_PATH}
            git commit -m "Update ${moduleName} image tag to ${IMAGE_TAG} [skip ci]"
            git push https://${GIT_USERNAME}:${GIT_PASSWORD}@${env.GIT_REPO_URL} HEAD:main
        """
    }
}

pipeline {
    agent any

    environment {
        // 전역 환경 변수 설정
        DOCKER_REGISTRY = "linkverseunibooker"
        DOCKER_CREDENTIAL_ID = "dockerhub-cred"
        GIT_CREDENTIAL_ID = "github-user-auth"
        GIT_REPO_URL = "github.com/beyond-sw-camp/be17-fin-LinkVerse-UniBooker-BE.git"
    }

    stages {
        stage('Checkout & Setup') {
            steps {
                echo "Starting Monorepo Scan (Build ${env.BUILD_NUMBER})"
                sh "chmod +x ./gradlew"
            }
        }

        stage('Parallel Build & Deploy') {
            steps { // <--- steps 블록 추가 (Declarative Syntax)
                script { // <--- script 블록 추가 (Scripted Syntax 허용)
                    def parallelStages = [:]

                    modules.each { module ->
                        // 각 모듈에 대한 병렬 스테이지 정의
                        parallelStages["${module}"] = {
                            buildAndDeploy(module)
                        }
                    }

                    // 정의된 모든 스테이지를 병렬 실행
                    parallel parallelStages
                }
            }
        }
    }
}