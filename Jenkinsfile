String archiveFileScriptId = '2a8d5f17-98d6-492e-a579-f605fb70a103'

pipeline {
    agent any
    options {
        skipDefaultCheckout(true)
        timeout(time: 5, activity: true, unit: 'MINUTES')
        sidebarLinks([[displayName: 'Javadoc', iconFileName: 'help.png', urlName: "https://frengor.com/javadocs/UltimateAdvancementAPI/build-server/${BRANCH_NAME}"]])
    }
    tools {
        maven 'Maven'
        jdk 'jdk21'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Cleaning...'
                cleanWs()
                checkout scm
            }
        }
        stage('Build') {
            steps {
                echo 'Building and testing...'
                sh 'mvn clean package -B -Pjenkins -U'
            }
        }
    }
    post {
        always {
            echo 'Archiving test results...'
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true, skipPublishingChecks: true
        }
        success {
            echo 'Archiving artifacts...'
            archiveArtifacts artifacts: "Distribution/API/target/*.jar", excludes: '**/original-*.jar'
            archiveArtifacts artifacts: "Distribution/Shadeable/target/*.jar", excludes: '**/original-*.jar'
            archiveArtifacts artifacts: "Distribution/ShadeableMojangMapped/target/*.jar", excludes: '**/original-*.jar'
            archiveArtifacts artifacts: "Distribution/Commands/target/*.jar", excludes: '**/original-*.jar'
            archiveArtifacts artifacts: "Distribution/CommandsMojangMapped/target/*.jar", excludes: '**/original-*.jar'
            archiveArtifacts artifacts: "Plugin/target/*.jar", excludes: '**/original-*.jar'
            echo 'Archiving javadoc...'
            //javadoc javadocDir: "Common/target/apidocs", keepAll: false
            configFileProvider([configFile(fileId: archiveFileScriptId, variable: 'JAVADOC_SCRIPT')]) {
                sh "/bin/bash +x $JAVADOC_SCRIPT ${BRANCH_NAME}"
            }
            
            echo 'Cleaning after successful build...'
            cleanWs(cleanWhenNotBuilt: false, notFailBuild: true)
        }
    }
}
