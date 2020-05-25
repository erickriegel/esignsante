@Library('AnsPipeline') _


pipeline {
    agent {
        label 'java-slaves'
    }
    environment {
        // Récupération de nom de l'artifact et de la version depuis le pom à l'aide du Pipeline Utility Steps plugin
        pomArtifact = readMavenPom().getArtifactId()
        pomVersion = readMavenPom().getVersion()
        // Message en cas d'échec du Pipeline
        failureMessage="!"
        // Version utilisée de SonarQube
        sonarQubeEnv = "${env.SONARQUBE_VERSION}"
        // Identifiant du serveur de l'artifactory
        artifactoryServerId = "${env.ARTIFACTORY_SERVER_ID}"
        // Destinataires du mail de notification
        mailList = "pipelineParams?.mailList ? pipelineParams.mailList : ${env.MAIL_TMA}"
    }
    // Récupération des outils nécessaires au projet
    tools {
        maven "${env.MVN_363}"
        jdk "${env.JDK_11}"
    }
    stages {
        stage ('Artifactory configuration') {
            steps {
                rtMavenResolver (
                    id: "MAVEN_RESOLVER",
                    serverId: "${artifactoryServerId}",
                    releaseRepo: "repo-dev",
                    snapshotRepo: "repo-dev"
                )
                rtMavenDeployer (
                    id: "MAVEN_DEPLOYER",
                    serverId: "${artifactoryServerId}",
                    releaseRepo: "asip-snapshots",
                    snapshotRepo: "asip-snapshots"
                )
            }
        }
        stage ('Builds') {
            parallel {
                stage ('Exec Maven') {
                    steps {
                        script {
                            configFileProvider([
                                configFile(
                                    fileId: 'org.jenkinsci.plugins.configfiles.maven.GlobalMavenSettingsConfig1427306179475',
                                    targetLocation: 'ASIPGlobalSettings.xml',
                                    variable: 'ASIPGlobalSettings'
                                ),
                                configFile(
                                    fileId: 'org.jenkinsci.plugins.configfiles.maven.MavenSettingsConfig1452787041167',
                                    targetLocation: 'ASIPProfilesSettings.xml',
                                    variable: 'ASIPProfilesSettings'
                                )
                            ])
                            {
                                withSonarQubeEnv("${sonarQubeEnv}") {
                                    sh 'mvn clean -s $ASIPProfilesSettings -gs $ASIPGlobalSettings org.jacoco:jacoco-maven-plugin:prepare-agent install org.jacoco:jacoco-maven-plugin:report sonar:sonar assembly:single -P !SONAR_SCM_DISABLED'
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
