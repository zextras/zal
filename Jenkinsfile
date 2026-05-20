library(
        identifier: 'jenkins-lib-common@1.7.5',
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
        cron(env.BRANCH_NAME == 'devel' ? 'H 5 * * *' : '')
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
        stage('Maven') {
            steps {
                script {
                    mavenStage(
                        profile: '',
                        deployArtifacts: true,
                        extraDeployArgs: env.TAG_NAME ? '-Dchangelist=' : ''
                    )
                }
            }
        }
        stage('Bump version') {
            steps {
                script {
                    dt2_semanticRelease()
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
                    when {
                        expression { return uploadStage.shouldUpload() }
                    }
                    tools {
                        jfrog 'jfrog-cli'
                    }
                    steps {
                        script {
                            def yapFiles = [
                                'yap.json'
                            ] as Set
                            def packages = yapHelper.getPackageNamesFromFiles(yapFiles)

                            uploadStage([
                                packages: packages,
                            ])
                        }
                    }
                }
            }
        }
    }
}
