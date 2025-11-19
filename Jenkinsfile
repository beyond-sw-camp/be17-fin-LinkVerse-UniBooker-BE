// 이 함수는 Scripted Pipeline 문법을 사용합니다.
def buildAndDeploy(moduleName) {

    // 1. 젠킨스 ChangeSet을 사용한 변경 감지 (오류 해결 및 안정화)
    def targetPath = "${moduleName}/"
    def commonPath = "common/"
    def isChanged = false

    // ChangeSets 리스트가 비어있지 않은지 확인
    if (currentBuild.changeSets.isEmpty()) {
        echo "Skipping ${moduleName}: No change sets found."
        return
    }

    // currentBuild.changeSets (List<ChangeSetList>)를 순회
    currentBuild.changeSets.each { changeSetList ->
        // ChangeSetList 내의 개별 ChangeSet (Commit)을 순회
        changeSetList.items.each { changeSet ->
            // ChangeSet 내의 변경된 파일 경로 목록을 순회
            changeSet.paths.each { path -> // <--- 이 부분이 수정되었습니다.
                if (path.startsWith(targetPath) || path.startsWith(commonPath)) {
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
        // 사용자가 제공한 환경 변
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