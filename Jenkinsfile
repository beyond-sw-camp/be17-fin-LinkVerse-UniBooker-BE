// 이 함수는 Scripted Pipeline 문법을 사용합니다.
def buildAndDeploy(moduleName) {

    // 1. 젠킨스 ChangeSet을 사용한 변경 감지 (안정적인 방법)
    def targetPath = "${moduleName}/"
    def commonPath = "common/"
    def isChanged = false

    // 현재 빌드에서 변경된 파일 목록을 순회하여 확인
    currentBuild.changeSets.each { set ->
        set.getPaths().each { path ->
            if (path.startsWith(targetPath) || path.startsWith(commonPath)) {
                isChanged = true
            }
        }
    }

    if (!isChanged) {
        echo "Skipping ${moduleName}: No relevant changes detected in ${targetPath} or ${commonPath}."
        return // 변경 사항이 없으면 함수 종료
    }

    // 2. CI/CD Logic (Scripted Steps)
    def PROJECT_NAME = "unibooker"
    def IMAGE_TAG = "${env.BUILD_NUMBER}"
    // Manifest 파일 경로를 동적으로 생성 (예: api-resource/k8s/backend-resource-rollout.yaml)
    def MANIFEST_PATH = "${moduleName}/k8s/backend-${moduleName.split('-')[1]}-rollout.yaml"
    def DOCKER_IMAGE = "${env.DOCKER_REGISTRY}/${PROJECT_NAME}/${moduleName}:${IMAGE_TAG}"

    echo "--- Starting CI/CD for Module: ${moduleName} ---"

    // [Step 1] Gradle Build
    // Multi-module 환경이므로 -p 옵션으로 프로젝트 경로 지정이 필요할 수 있지만,
    // 루트에서 실행 시 ':모듈명:태스크'가 일반적이므로 그대로 유지합니다.
    sh "./gradlew :${moduleName}:clean :${moduleName}:build -x test"

    // [Step 2] Docker Build & Push
    docker.withRegistry("https://${env.DOCKER_REGISTRY}", "${env.DOCKER_CREDENTIAL_ID}") {
        // -f 옵션으로 모듈 내 Dockerfile 지정, 컨텍스트는 루트(.)
        def image = docker.build("${DOCKER_IMAGE}", "-f ${moduleName}/Dockerfile .")
        image.push()
        image.push("${env.DOCKER_REGISTRY}/${PROJECT_NAME}/${moduleName}:latest")
    }

    // [Step 3] Update Manifest (GitOps)
    withCredentials([usernamePassword(credentialsId: env.GIT_CREDENTIAL_ID, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
        sh """
            # Git 설정
            git config user.email 'jenkins@yourcompany.com'
            git config user.name 'Jenkins Bot'

            # K8s Manifest 파일 이미지 태그 수정
            sed -i.bak 's|image: .*${moduleName}:.*|image: ${DOCKER_IMAGE}|' ${MANIFEST_PATH}

            # 변경사항 Commit & Push (ArgoCD 트리거)
            git add ${MANIFEST_PATH}
            git commit -m "Update ${moduleName} image tag to ${IMAGE_TAG} [skip ci]"
            git push https://${GIT_USERNAME}:${GIT_PASSWORD}@${env.GIT_REPO_URL} HEAD:main
        """
    }
}

// ---------------------------------------------------------------------------------------
def modules = [
    'api-app',
    'api-discovery',
    'api-gateway',
    'api-queue',
    'api-reservation',
    'api-resource',
    'api-statistics'
]

pipeline {
    agent any

    environment {
        // 사용자가 제공한 환경 변수
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
            steps {
                script {
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