pipeline {
    agent any
    tools {
        maven 'Maven-3'
        gradle 'Gradle-3'
        go 'go1.11'

    }

     options {
        skipDefaultCheckout true
    }
    environment {
        branch = 'master'
        myBranch = 'unbranded'
        mobileUrl = 'git@github.com:Nilepoint/nilepoint-mobile.git'
        commonUrl = 'git@github.com:Nilepoint/nilepoint-common.git'
        domainModelUrl = 'git@github.com:Nilepoint/nilepoint-domain-model.git'
        networkingUrl  = 'git@github.com:Nilepoint/nilepoint-networking.git'
        formBuilderUrl = 'git@github.com:Nilepoint/nilepoint-formbuilder.git'
        persistUrl = 'git@github.com:Nilepoint/nilepoint-persist.git'
        fhbridgeUrl = 'git@github.com:Nilepoint/nilepoint-fh-bridge.git'
        GOPATH = "${WORKSPACE}/go"
        PATH = "${PATH}:${GOPATH}/bin"
    }
    stages {
        stage('checkout'){
            // from https://github.com/allegro/axion-release-plugin/issues/195#issuecomment-319541175
            // this is a workaround jenkins' bug that doesn't pull tags which is required for distance based versioning
            steps {
                checkout([
	            $class: 'GitSCM',
	            branches: scm.branches,
	            doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
	            extensions: scm.extensions + [[$class: 'CloneOption', noTags: false, reference: '', shallow: true]],
	            submoduleCfg: [],
	            userRemoteConfigs: scm.userRemoteConfigs
                ])
            }
        }
        stage('initialize'){
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                    go get github.com/mistsys/github-release
                '''
            }
        }
        stage('checkout nilepoint-mobile deps') {
            steps {
                dir('nilepoint-common') {
                    git branch: branch, credentialsId: 'jenkins-np-ssh', url: commonUrl
                }
                dir('nilepoint-networking'){
                    git branch: myBranch, credentialsId: 'jenkins-np-ssh', url: networkingUrl
                }
                dir('nilepoint-fh-bridge') {
                    git branch: myBranch, credentialsId: 'jenkins-np-ssh', url: fhbridgeurl
                }
                dir('nilepoint-formbuilder'){
                    git branch: branch, credentialsId: 'jenkins-np-ssh', url: formbuilderUrl
                }
                dir('nilepoint-persist'){
                    git branch: branch, credentialsId: 'jenkins-np-ssh', url: persistUrl
                }


            }
        }

        stage('version the build'){
            steps {
                sshagent(credentials: ['jenkins-np-ssh']){
                    sh '''
                     env
                     nilepoint-common/git_distance.sh -t VERSION
                    '''
                }
            }
        }

        stage('build nilepoint-networking') {
            steps {
                dir ('nilepoint-networking'){
                    sh 'mvn clean install -DskipTests=true'
                }
            }
        }

        stage('build nilepoint-fh-bridge'){
            steps {
                dir ('nilepoint-fh-bridge'){
                    sh 'mvn clean install -DskipTests=true'
                }
            }
        }

        stage('build nilepoint-persist'){
            steps {
                dir ('nilepoint-persist'){
                    sh './gradlew install'
                }
            }
        }

        stage('build nilepoint-formbuilder'){
            steps {
                dir ('nilepoint-formbuilder'){
                    sh './gradlew install'
                }
            }
        }

       stage('build apk'){
            steps {
                    sh './gradlew clean -x test build'
            }
        }

        stage('make release'){
            steps {
                withCredentials([string(credentialsId: 'GithubToken', variable: 'GITHUB_TOKEN')]) {
                    sh '''
go get github.com/mistsys/github-release
env
curl -v -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user/issues
echo "PATH = ${PATH}"
VERSION=$(cat VERSION)
nilepoint-common/release.sh github ./app/build/outputs/apk/app-debug.apk nilepoint-mobile-debug-${VERSION}.apk
'''
                }
            }
        }
    }
}
