def modules = [
    'api-app',
    'api-discovery',
    'api-gateway',
    'api-queue',
    'api-reservation',
    'api-resource',
    'api-statistics'
]

def buildAndDeploy(moduleName) {
    stage("Build & Deploy ${moduleName}") {
        // 해당 모듈 폴더나 common 폴더에 변경 사항이 있을 때만 실행
        when {
            expression {
                // git ls-files를 통해 변경된 파일 목록을 가져와 해당 모듈 경로가 포함되어 있는지 확인
                def changedFiles = sh(returnStdout: true, script: "git diff --name-only HEAD~1 HEAD").trim()
                return changedFiles.contains("${moduleName}/") || changedFiles.contains("common/")
            }
        }

        steps {
            script {
                def PROJECT_NAME = "unibooker"
                def IMAGE_TAG = "${env.BUILD_NUMBER}"
                def MANIFEST_PATH = "${moduleName}/k8s/backend-${moduleName.split('-')[1]}-rollout.yaml"
                def DOCKER_IMAGE = "${env.DOCKER_REGISTRY}/${PROJECT_NAME}/${moduleName}:${IMAGE_TAG}"

                echo "--- Starting CI/CD for Module: ${moduleName} ---"

                // 1. Gradle Build
                sh "./gradlew :${moduleName}:clean :${moduleName}:build -x test"

                // 2. Docker Build & Push
                docker.withRegistry("https://${env.DOCKER_REGISTRY}", "${env.DOCKER_CREDENTIAL_ID}") {
                    // Dockerfile은 각 모듈 폴더에 있다고 가정
                    def image = docker.build("${DOCKER_IMAGE}", "-f ${moduleName}/Dockerfile .")
                    image.push()
                    image.push("${env.DOCKER_REGISTRY}/${PROJECT_NAME}/${moduleName}:latest")
                }

                // 3. Update Manifest (GitOps)
                withCredentials([usernamePassword(credentialsId: env.GIT_CREDENTIAL_ID, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')]) {
                    sh """
                        # Git 설정
                        git config user.email 'jenkins@yourcompany.com'
                        git config user.name 'Jenkins Bot'

                        # Manifest 파일 이미지 태그 수정
                        sed -i.bak 's|image: .*${moduleName}:.*|image: ${DOCKER_IMAGE}|' ${MANIFEST_PATH}

                        # 변경사항 Commit & Push (ArgoCD 트리거)
                        git add ${MANIFEST_PATH}
                        git commit -m "Update ${moduleName} image tag to ${IMAGE_TAG} [skip ci]"
                        git push https://${GIT_USERNAME}:${GIT_PASSWORD}@${env.GIT_REPO_URL} HEAD:main
                    """
                }
            }
        }
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
                // 초기 설정을 위한 공통 작업이 필요하면 여기에 추가
            }
        }

        stage('Parallel Build & Deploy') {
            // 모든 모듈을 병렬로 처리
            parallel {
                // 정의된 모듈 리스트를 순회하며 buildAndDeploy 함수 호출
                modules.each { module ->
                    // 함수 호출을 위한 dynamic stage 생성
                    "${module}"(buildAndDeploy(module))
                }
            }
        }
    }
}