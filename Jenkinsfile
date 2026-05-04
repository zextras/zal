library(
        identifier: 'jenkins-lib-common@1.7.0',
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
                        profile: env.TAG_NAME ? '-Pproduction' : '',
                        deployArtifacts: true,
                        extraDeployArgs: env.TAG_NAME ? '-Dchangelist=' : ''
                    )
                }
            }
        }
        stage('Build deb/rpm') {
          stages {
              stage('Stash') {
                  steps {
                      sh 'cp target/zal.jar packages/'
                      stash includes: "yap.json,packages/**", name: 'binaries'
                  }
              }
              stage('yap') {
                  parallel {
                      stage('Ubuntu') {
                          agent {
                              node {
                                  label 'yap-ubuntu-20-v1'
                              }
                          }
                          steps {
                              container('yap') {
                                  unstash 'binaries'
                                  sh 'sudo yap build ubuntu .'
                                  stash includes: 'artifacts/', name: 'artifacts-deb'
                              }
                          }
                          post {
                              always {
                                  archiveArtifacts artifacts: "artifacts/*.deb", fingerprint: true
                              }
                          }
                      }

                      stage('Rocky') {
                          agent {
                              node {
                                  label 'yap-rocky-8-v1'
                              }
                          }
                          steps {
                              container('yap') {
                                  unstash 'binaries'
                                  sh 'sudo yap build rocky .'
                                  stash includes: 'artifacts/', name: 'artifacts-rpm'
                              }
                          }
                          post {
                              always {
                                  archiveArtifacts artifacts: "artifacts/*.rpm", fingerprint: true
                              }
                          }
                      }
                  }
              }
          }
      }
      stage('Upload To Playground') {
          when {
              anyOf {
                  branch 'playground/*'
                  expression { params.PLAYGROUND == true }
              }
          }
          steps {
              unstash 'artifacts-deb'
              unstash 'artifacts-rpm'
              script {
                  def server = Artifactory.server 'zextras-artifactory'
                  def buildInfo
                  def uploadSpec

                  buildInfo = Artifactory.newBuildInfo()
                  uploadSpec = """{
                      "files": [
                          {
                              "pattern": "artifacts/carbonio-zal*.deb",
                              "target": "ubuntu-playground/pool/",
                              "props": "deb.distribution=focal;deb.distribution=jammy;deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
                          },
                          {
                              "pattern": "artifacts/(carbonio-zal)-(*).x86_64.rpm",
                              "target": "centos8-playground/zextras/{1}/{1}-{2}.x86_64.rpm",
                              "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
                          },
                          {
                              "pattern": "artifacts/(carbonio-zal)-(*).x86_64.rpm",
                              "target": "rhel9-playground/zextras/{1}/{1}-{2}.x86_64.rpm",
                              "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
                          }
                      ]
                  }"""
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
              }
          }
      }
      stage('Upload To Devel') {
          when {
            anyOf {
                branch 'devel'
            }
          }
          steps {
              unstash 'artifacts-deb'
              unstash 'artifacts-rpm'
              script {
                  def server = Artifactory.server 'zextras-artifactory'
                  def buildInfo
                  def uploadSpec

                  buildInfo = Artifactory.newBuildInfo()
                  uploadSpec = """{
                      "files": [
                          {
                              "pattern": "artifacts/carbonio-zal*.deb",
                              "target": "ubuntu-devel/pool/",
                              "props": "deb.distribution=focal;deb.distribution=jammy;deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
                          },
                          {
                              "pattern": "artifacts/(carbonio-zal)-(*).x86_64.rpm",
                              "target": "centos8-devel/zextras/{1}/{1}-{2}.x86_64.rpm",
                              "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
                          },
                          {
                              "pattern": "artifacts/(carbonio-zal)-(*).x86_64.rpm",
                              "target": "rhel9-devel/zextras/{1}/{1}-{2}.x86_64.rpm",
                              "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
                          }
                      ]
                  }"""
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
              }
          }
      }
      stage('Upload & Promotion Config') {
          when {
              anyOf {
                  branch 'release/*'
                  buildingTag()
              }
          }
          steps {
              unstash 'artifacts-deb'
              unstash 'artifacts-rpm'
              script {
                  def server = Artifactory.server 'zextras-artifactory'
                  def buildInfo
                  def uploadSpec
                  def config

                  //ubuntu
                  buildInfo = Artifactory.newBuildInfo()
                  buildInfo.name += "-ubuntu"
                  uploadSpec= """{
                      "files": [
                          {
                              "pattern": "artifacts/carbonio-zal*.deb",
                              "target": "ubuntu-rc/pool/",
                              "props": "deb.distribution=focal;deb.distribution=jammy;deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
                          }
                      ]
                  }"""
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                  config = [
                          'buildName'          : buildInfo.name,
                          'buildNumber'        : buildInfo.number,
                          'sourceRepo'         : 'ubuntu-rc',
                          'targetRepo'         : 'ubuntu-release',
                          'comment'            : 'Do not change anything! Just press the button',
                          'status'             : 'Released',
                          'includeDependencies': false,
                          'copy'               : true,
                          'failFast'           : true
                  ]
                  Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: "Ubuntu Promotion to Release"
                  server.publishBuildInfo buildInfo

                  //rhel8
                  buildInfo = Artifactory.newBuildInfo()
                  buildInfo.name += "-centos8"
                  uploadSpec= """{
                      "files": [
                          {
                              "pattern": "artifacts/(carbonio-zal)-(*).x86_64.rpm",
                              "target": "centos8-rc/zextras/{1}/{1}-{2}.x86_64.rpm",
                              "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
                          }
                      ]
                  }"""
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                  config = [
                          'buildName'          : buildInfo.name,
                          'buildNumber'        : buildInfo.number,
                          'sourceRepo'         : 'centos8-rc',
                          'targetRepo'         : 'centos8-release',
                          'comment'            : 'Do not change anything! Just press the button',
                          'status'             : 'Released',
                          'includeDependencies': false,
                          'copy'               : true,
                          'failFast'           : true
                  ]
                  Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: "Centos8 Promotion to Release"
                  server.publishBuildInfo buildInfo

                  //rhel9
                  buildInfo = Artifactory.newBuildInfo()
                  buildInfo.name += "-rhel9"
                  uploadSpec= """{
                      "files": [
                          {
                              "pattern": "artifacts/(carbonio-zal)-(*).x86_64.rpm",
                              "target": "rhel9-rc/zextras/{1}/{1}-{2}.x86_64.rpm",
                              "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
                          }
                      ]
                  }"""
                  server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                  config = [
                          'buildName'          : buildInfo.name,
                          'buildNumber'        : buildInfo.number,
                          'sourceRepo'         : 'rhel9-rc',
                          'targetRepo'         : 'rhel9-release',
                          'comment'            : 'Do not change anything! Just press the button',
                          'status'             : 'Released',
                          'includeDependencies': false,
                          'copy'               : true,
                          'failFast'           : true
                  ]
                  Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: "Centos8 Promotion to Release"
                  server.publishBuildInfo buildInfo
              }
          }
       }
    }
}
