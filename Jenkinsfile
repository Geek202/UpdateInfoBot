@Library("Forge-Libs")_

pipeline {
  agent any
  environment {
    WEBHOOK_URL = credentials('updateinfobot-discord-webhook')
    WEBHOOK_TITLE = "UpdateInfoBot Build #${BUILD_NUMBER}"
    JENKINS_HEAD = 'https://wiki.jenkins-ci.org/download/attachments/2916393/headshot.png'
  }

  stages {
    stage('Notify-Build-Start') {
      when {
        not {
          changeRequest()
        }
      }
      
      steps {
        discordSend(
          title: "${WEBHOOK_TITLE} Started",
          successful: true,
          result: 'ABORTED',
          thumbnail: JENKINS_HEAD,
          webhookURL: WEBHOOK_URL
        )
      }
    }

    stage('Build') {
      steps {
        sh 'chmod +x gradlew'
        sh './gradlew build'
      }
    }

  }

  post {
    always {
      script {
        archiveArtifacts(artifacts: '**/build/libs/*.jar', fingerprint: true, onlyIfSuccessful: true, allowEmptyArchive: true)
        archiveArtifacts(artifacts: '**/build/distributions/*', fingerprint: true, onlyIfSuccessful: true, allowEmptyArchive: true)

        if (env.CHANGE_ID == null) {
          discordSend(
            title: "${WEBHOOK_TITLE} Finished ${currentBuild.currentResult}",
            description: '```\n' + getChanges(currentBuild) + '\n```',
            successful: currentBuild.resultIsBetterOrEqualTo("SUCCESS"),
            result: currentBuild.currentResult,
            thumbnail: JENKINS_HEAD,
            webhookURL: WEBHOOK_URL
          )
        }
      }
    }
  }
}
