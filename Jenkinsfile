// 이 함수는 Scripted Pipeline 문법을 사용합니다.
def buildAndDeploy(moduleName) {

    // 1. 변경 감지 로직 (이 부분은 어느 컨테이너에서 실행되든 상관없습니다.)
    def targetPath = "${moduleName}/"
    def commonPath = "common/"
    def isChanged = false

    // ... (이전과 동일한 변경 감지 로직) ...

    if (currentBuild.changeSets.isEmpty()) {
        echo "Skipping ${moduleName}: No change sets found."
        return
    }

    currentBuild.changeSets.each { changeSetList ->
        changeSetList.items.each { changeSet ->
            changeSet.paths.each { path ->
                def filePath = path.getPath()
                if (filePath.startsWith(targetPath) || filePath.startsWith(commonPath)) {
                    isChanged = true
                }
            }
        }
    }

    if (!isChanged) {
        echo "Skipping ${moduleName}: No relevant changes detected in ${targetPath} or ${commonPath}."
        // ★★★ 이 return 문이 병렬 처리에서 스텝을 즉시 종료하는 역할을 합니다.
        return
    }

    // 2. CI/CD Logic (변수 선언은 그대로 둡니다.)
    def PROJECT_NAME = "unibooker"
    def IMAGE_TAG = "${env.BUILD_NUMBER}"
    def MANIFEST_PATH = "${moduleName}/k8s/backend-${moduleName.split('-')[1]}-rollout.yaml"
    def DOCKER_IMAGE = "${env.DOCKER_REGISTRY}/${PROJECT_NAME}/${moduleName}:${IMAGE_TAG}"

    echo "--- Starting CI/CD for Module: ${moduleName} ---"

    // [Step 1] Gradle Build: JNLP (기본) 컨테이너에서 실행
    // Gradle 및 Java 환경이 갖춰진 기본 컨테이너 이름(보통 'jnlp' 또는 Agent 기본 컨테이너 이름)으로 지정합니다.
    container('jnlp') {
        sh "./gradlew :${moduleName}:clean :${moduleName}:build -x test"
    }

    // [Step 2 & 3] Docker Build & GitOps: 'dind-client' 컨테이너에서 실행
    // Docker, Git 클라이언트가 설치된 컨테이너로 컨텍스트를 전환합니다.
    container('dind-client') {

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
        DOCKER_REGISTRY = "docker.io"
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

                                    // 모든 로직을 하나의 Script Block으로 감싸서 안정성을 높입니다.
                                    buildAndDeploy(module)

                                }
                            }

                            // 병렬 실행
                            parallel parallelStages
                        }
                    }
                }
    }
}