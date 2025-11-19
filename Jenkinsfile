// 이 함수는 Scripted Pipeline 문법을 사용합니다.
def buildAndDeploy(moduleName) {

    // 1. 젠킨스 ChangeSet을 사용한 변경 감지 (오류 해결 및 안정화)
    def targetPath = "${moduleName}/"
    def commonPath = "common/"
    def isChanged = false

    if (currentBuild.changeSets.isEmpty()) {
        // 이 메시지는 첫 빌드 시 자주 발생합니다. (정상 동작)
        echo "Skipping ${moduleName}: No change sets found."
        return
    }

    currentBuild.changeSets.each { changeSetList ->
        changeSetList.items.each { changeSet ->
            changeSet.paths.each { path ->

                // ★★★ 이 부분이 수정되었습니다: .getPath()를 사용하여 명시적인 String을 가져옵니다. ★★★
                def filePath = path.getPath()

                if (filePath.startsWith(targetPath) || filePath.startsWith(commonPath)) {
                    isChanged = true
                }
            }
        }
    }

    if (!isChanged) {
        echo "Skipping ${moduleName}: No relevant changes detected in ${targetPath} or ${commonPath}."
        return // 변경 사항이 없으면 함수 종료
    }

    // 2. CI/CD Logic (이하 동일)
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
                        parallelStages["${module}"] = {
                            buildAndDeploy(module)
                        }
                    }
                    parallel parallelStages
                }
            }
        }
    }
}