pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        NEXUS_CREDENTIALS_ID = 'nexus_cred_id'
        CENTRAL_CREDENTIALS_ID = 'central_cred_id'
        GITHUB_CREDENTIALS_ID = 'github_cred_id'
        GPG_PASSPHRASE_CREDENTIALS_ID = 'gpg_passphrase_cred_id'
        GITHUB_REPO_URL = 'github.com/atchurey/talkativebot.git'
        GITHUB_GIT_HTTP_ORIGIN = 'https://github.com/atchurey/talkativebot.git'
        MAVEN_PUBLISH_MODULES = ':talkativebot-parent,talkativebot-core,talkativebot-spring-boot-starter'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/${env.BRANCH_NAME}"]],
                    extensions: [[
                        $class: 'LocalBranch',
                        localBranch: "${env.BRANCH_NAME}"
                    ]],
                    userRemoteConfigs: scm.userRemoteConfigs
                ])
            }
        }

        stage('Maven Build and Deploy Snapshot') {
            when {
                branch 'develop'
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: "${NEXUS_CREDENTIALS_ID}",
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    sh '''
                        set -e
                        ./mvnw -B -s .jenkins/settings.xml \
                            -pl "${MAVEN_PUBLISH_MODULES}" -am \
                            clean deploy
                    '''
                }
            }
        }

        stage('Maven Verify') {
            when {
                branch 'master'
            }
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: "${NEXUS_CREDENTIALS_ID}",
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    )
                ]) {
                    sh '''
                        set -e
                        ./mvnw -B -s .jenkins/settings.xml \
                            -pl "${MAVEN_PUBLISH_MODULES}" -am \
                            clean verify
                    '''
                }
            }
        }

        stage('Create Tag and Release to Maven Central') {
            when {
                branch 'master'
            }
            steps {
                input message: 'Create a release tag and publish to Maven Central?', ok: 'Release'

                withCredentials([
                    usernamePassword(
                        credentialsId: "${NEXUS_CREDENTIALS_ID}",
                        usernameVariable: 'NEXUS_USERNAME',
                        passwordVariable: 'NEXUS_PASSWORD'
                    ),
                    usernamePassword(
                        credentialsId: "${CENTRAL_CREDENTIALS_ID}",
                        usernameVariable: 'CENTRAL_USERNAME',
                        passwordVariable: 'CENTRAL_PASSWORD'
                    ),
                    usernamePassword(
                        credentialsId: "${GITHUB_CREDENTIALS_ID}",
                        usernameVariable: 'GITHUB_USERNAME',
                        passwordVariable: 'GITHUB_PASSWORD'
                    ),
                    string(
                        credentialsId: "${GPG_PASSPHRASE_CREDENTIALS_ID}",
                        variable: 'GPG_PASSPHRASE'
                    )
                ]) {
                    sh '''
                        set -e
                        test -n "${GPG_KEY_ID:?GPG_KEY_ID must be set on the Jenkins controller (docker .env)}"

                        git config user.email "jenkins@local"
                        git config user.name "Jenkins"

                        AUTHENTICATED_GITHUB_URL="https://${GITHUB_USERNAME}:${GITHUB_PASSWORD}@${GITHUB_REPO_URL}"

                        git remote set-url origin "$AUTHENTICATED_GITHUB_URL"
                        git fetch origin master
                        git checkout -B master origin/master
                        git reset --hard origin/master
                        git clean -fd

                        export GITHUB_GIT_HTTP_ORIGIN="$AUTHENTICATED_GITHUB_URL"

                        ./mvnw -B -s .jenkins/settings.xml \
                            -pl "${MAVEN_PUBLISH_MODULES}" -am \
                            -Dgpg.keyname="${GPG_KEY_ID}" \
                            -DlocalCheckout=true \
                            -DscmCommentPrefix="[skip ci]" \
                            release:clean release:prepare release:perform
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'talkativebot-core/target/*.jar,talkativebot-spring-boot-starter/target/*.jar', allowEmptyArchive: true
            junit allowEmptyResults: true, testResults: 'talkativebot-core/target/surefire-reports/*.xml,talkativebot-spring-boot-starter/target/surefire-reports/*.xml'
        }
    }
}
