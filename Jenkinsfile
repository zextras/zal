library(
        identifier: 'jenkins-lib-common@v4.10.2',
        retriever: modernSCM([
                $class: 'GitSCMSource',
                credentialsId: 'jenkins-integration-with-github-account',
                remote: 'git@github.com:zextras/jenkins-lib-common.git',
        ])
)

properties(defaultPipelineProperties())

pipeline {
    agent {
        node {
            label 'zextras-v1'
        }
    }
    triggers {
        cron(env.BRANCH_IS_PRIMARY == 'true' ? 'H 5 * * *' : '')
    }
    parameters {
        booleanParam defaultValue: false, description: 'Whether to upload the packages in playground repositories', name: 'PLAYGROUND'
    }
    environment {
        JAVA_OPTS="-Dfile.encoding=UTF8"
        LC_ALL="C.UTF-8"
        jenkins_build="true"
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        disableConcurrentBuilds()
        timeout(time: 15, unit: 'MINUTES')
        skipDefaultCheckout()
    }
    stages {
        stage('Setup') {
            steps {
                checkout scm
                gitMetadata()
            }
        }
        stage('Skip CI') {
            steps {
                script { semanticRelease.guard() }
            }
        }
        stage('Security Scan') {
            steps {
                gitleaksStage()
            }
        }
        stage('Maven') {
            steps {
                script {
                    mavenStage(
                        profile: '',
                        deployArtifacts: env.TAG_NAME ? true : false,
                        extraDeployArgs: '-Dchangelist='
                    )
                }
            }
        }
        stage("Package/upload artifacts") {
            stages {
                stage('Build deb/rpm') {
                    steps {
                        echo 'Building deb/rpm packages'
                        sh 'cp target/zal.jar packages/'
                        buildStage([
                            buildDirs: ["."],
                            buildFlags: ' -ds ',
                            stashIncludes: 'yap.json, packages/**',
                        ])
                    }
                }

                stage('Upload artifacts') {
                    tools {
                        jfrog 'jfrog-cli'
                    }
                    steps {
                        script {
                            uploadStage()
                        }
                    }
                }
            }
        }
        stage('Bump version') {
            steps {
                script {
                    semanticRelease()
                }
            }
        }
    }
}
