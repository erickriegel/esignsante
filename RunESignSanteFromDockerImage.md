# Installation de ESignSante à partir de l'image Docker

Document d'installation de ESignSante à partir de l'image docker

## Prérequis

- Docker
- Fichier de configuration au format json.
- Fichier application.properties (recommendé)

## Installation docker

Docker est disponible sur les platformes Linux, Mac et Windows (sauf Windows 10 Home Edition - non pro) via Docker Desktop, Windows Server et en tant qu'installation binaire statique. Trouvez l'installation pour votre système d'exploitation ici [documentation](https://docs.docker.com/install/). 

Pour installer Docker Desktop sur Windows 10 home edition il faut suivre les étapes suivantes

### Installation Docker Desktop - Windows 10 Home Edition

La raison pour laquelle Docker a besoin de Windows Pro ou Enterprise est qu'il utilise Hyper-V et des conteneurs (désactivés pour les autre versions de windows 10). Installons-les.

#### Installation de Hyper-V et Containers

1. On va créer le fichier `InstallHyperV.bat` dans le lequel on va rajouter les commandes suivantes:
```shell script
pushd "%~dp0"
dir /b %SystemRoot%\servicing\Packages\*Hyper-V*.mum>hyper-v.txt
for /f %%i in ('findstr /i . hyper-v.txt') do dism /online /norestart /add-package:"%SystemRoot%\servicing\Packages\%%i"
del hyper-v.txt
dism /online /enable-feature /featurename:Microsoft-Hyper-V -All /LimitAccess /ALL
pause
```
2. Lancer `InstallHyperV.bat` en mode administrateur (cette étape peux prendre plusieur minutes).
3. On va créer le fichier `InstallContainers.bat` dans le lequel on va rajouter les commandes suivantes:
```shell script
pushd "%~dp0"
dir /b %SystemRoot%\servicing\Packages\*containers*.mum >containers.txt
for /f %%i in ('findstr /i . containers.txt 2^>nul') do dism /online /norestart /add-package:"%SystemRoot%\servicing\Packages\%%i"
del containers.txt
dism /online /enable-feature /featurename:Containers -All /LimitAccess /ALL 
pause
```
4. Lancer `InstallContainers.bat` en mode administrateur (cette étape peux prendre plusieur minutes).

Maintenant que Hyper-V et les conteneurs sont installés, il est temps de tromper Docker que nous exécutons sur Windows Pro.

#### Changer la Version de Windows dans la Registry

1. Appuier sur `Windows + R` et tapper `regedit`
2. Dans l'éditeur de la Registry, aller dans `\HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows NT\CurrentVersion`
3. Click-droit sur `EditionID` et `Modify`
4. Changer la valeur dans `Value Data` à `Professional` puis OK.

#### Installer Docker Desktop

Télécharger [Docker Desktop](https://hub.docker.com/editions/community/docker-ce-desktop-windows) pour windows.

Lancer le `Docker Desktop Installer.exe`.

Après l'initialisation, il va demander de redémarrer la machine pour installer `hyper-v` et `containers`. La mise à jour windows peut prendre plusieurs minutes. Avec le redémarrage de la machine `EditionID` sera réinitialisé mais on aura pas à la redéfinir à nouveau.

Finalement, réexécutez le programme d'installation de Docker. Il devrait se dérouler sans aucun problème.

Lancer Docker Desktop puis ouvrir `PowerShell` pour exécuter les commandes Docker. Guide [Quickstart](https://docs.docker.com/get-started/#set-up-your-docker-environment).

## Fichier de configuration
### Mode de fonctionnement
La configuration de ESignSante et des fonctionnalités présentes après le démarrage de l'appli, est défini pour l'instant dans un unique fichier externe au format Json, esignsante-conf.json.
L'objet Json qui définit la configuration est constitué de 5 listes d'objets (la casse est respectée) :
- signature : liste de configurations de signature.
- proof : liste de configurations de preuve de validation de signature.
- signatureVerification : liste de configurations de validation de signature.
- certificateVerification : liste de configurations de validation de certificat.
- ca : liste de certificats au formats pem  qui constituent le bundle d'autorités de confiance et le bundle des listes de révocation de certificats.
Voici donc à quoi ressemblerait le squelette du fichier de configuration :
```json
{
"signature": [
		{
			...
		},
		...
	],
"proof": [
		{
			...
		},
		...
	],
	"signatureVerification": [
		{
			...
		},
		...
	],
	"certificateVerification": [
		{
			...
		},
		 ...
	],
	 "ca": [
		{
			...
		},
		...
	]
}
```
Remarque : L'application pourrait être démarrée avec une configuration « vide », par contre le contrôle sur les objets et les champs est strict, et un fichier de conf malformé empêchera le démarrage de l'appli. Une fois l'application démarré, les modifications apportés au fichier de configuration seront prisent en compte si elles sont légitimes, sinon les configurations en mémoire seront retenues.

### Configuration de signature
L'objet configuration de signature contient les éléments suivants :
- idSignConf : ID unique de configuration de signature.
- secret : le ou les secrets pour accéder à la configuration, la valeur du secret ici sera le Hash généré dans “/secrets”, plusieurs secrets doivent être séparés par un espace blanc.
- idProofConf : ID de la configuration de la preuve associé à cette configuration de signature.
- description
- certificate : certificat de signature au format pem, sur une ligne pour respecter le format Json, ‘\n' pour remplacer les retours à la line (tous les 64 caractères), sinon le certificat ne sera pas traité correctement.
- privateKey : clé privée qui correspond au certificat.
- canonicalisationAlgorithm : canonicalisation algorithm.
- digestAlgorithm : digest algorithm.
- signaturePackaging : type de signature, ENVELOPING ou ENVELOPED.
- signId : Id de la balise <ds:Signature>
- signValueId : Id de la balise <ds:SignatureValue>
- objectId : Id de la balise <ds:Object> et URI de la balise <ds:Reference> précédée d'un caractère #.
Les propriétés signId, signValueId et objectId ne sont prises en comptes que par les opérations de signatures XmlDsig. En effet pour les signatures xades ces champs sont générés.
Voici un exemple d'un objet de configuration de signature :
```json
"signature": [
	{
		"idSignConf": "1",
		"secret": "$2a$12$0BhliUZtpwtMysnuDqJ4Z.lQO41/vfclnB4H3p3YtNquCRQ./1cfO $2a$12$wKbipqCY5PRjxfC0fgTXJ.hZhVXTlb84Zbm5LU0ygrdxPNJbo86M2",
		"idProofConf": "1",
		"description": "Scheduling Test.",	
		"certificate": "-----BEGIN CERTIFICATE-----\nMIIIiDC … VtpQ==\n-----END CERTIFICATE-----",
		"privateKey": "-----BEGIN PRIVATE KEY-----\nMIIEv … zcipw==\n-----END PRIVATE KEY-----",
		"canonicalisationAlgorithm": "http://www.w3.org/2001/10/xml-exc-c14n#",
		"digestAlgorithm": "SHA512",
		"signaturePackaging": "ENVELOPING",
		"signId":"Tao_Sig_Id_SIG",
		"signValueId":"smth",
		"objectId":"Tao_Sig_Id_SignedDocument"
	}
]
```
### Configuration de preuve de validation
L'objet configuration de preuve contient les éléments suivants :
- idProofConf: ID unique de configuration de preuve de validation de signature.
- description
- certificate : certificat de signature de preuve au format pem, sur une ligne pour respecter le format Json, ‘\n' pour remplacer les retours à la line (tous les 64 caractères), sinon le certificat ne sera pas traité correctement.
- privateKey : clé privée qui correspond au certificat.
- canonicalisationAlgorithm : canonicalisation algorithm.
- digestAlgorithm : digest algorithm.
- signaturePackaging : type de signature, ENVELOPING ou ENVELOPED.
Voici un exemple :
```json
"proof": [
	{
		"idProofConf": "1",
		"description": "Scheduling Test.",	
		"certificate": "-----BEGIN CERTIFICATE-----\nMIIIiDC … VtpQ==\n-----END CERTIFICATE-----",
		"privateKey": "-----BEGIN PRIVATE KEY-----\nMIIEv … zcipw==\n-----END PRIVATE KEY-----",
		"canonicalisationAlgorithm": "http://www.w3.org/2001/10/xml-exc-c14n#",
		"digestAlgorithm": "SHA512",
		"signaturePackaging": "ENVELOPING",
	}
]
```
### Configuration de validation de signature
L'objet configuration de validation de signature contient les éléments suivants :
- idVerifSign: ID unique de configuration de validation de signature.
- description
- metadata : liste de métadonnées à inclure dans le rapport de validation ESignSante
- rules : règles ESignSante de validation de signature.
Voici un exemple :
```json
"signatureVerification": [
	{
		"idVerifSign": "1",
		"description": "",
		"metadata": "DATE_SIGNATURE,DN_CERTIFICAT,RAPPORT_DIAGNOSTIQUE,DOCUMENT_ORIGINAL_NON_SIGNE,RAPPORT_DSS",
		"rules":	"TrustedCertificat,FormatSignature,SignatureCertificatValide,ExistenceBaliseSigningTime,ExistenceDuCertificatDeSignature,ExpirationCertificat,NonRepudiation,RevocationCertificat,SignatureNonVide,SignatureIntacte,DocumentIntact"
	}
]
```
Valeurs possibles des règles et métadonnées :
```properties
# Liste des règles de validation, rules, d'une signature ; valeurs possibles :
SignatureCertificatValide
ExistenceBaliseSigningTime
ExistenceDuCertificatDeSignature
ExpirationCertificat
FormatSignature
NonRepudiation
RevocationCertificat 
SignatureNonVide
TrustedCertificat
SignatureIntacte
DocumentIntact
```
```properties
# Types de metadata retournées lors d'une validation de signature ; valeurs possibles :
DATE_SIGNATURE
DN_CERTIFICAT
RAPPORT_DIAGNOSTIQUE
DOCUMENT_ORIGINAL_NON_SIGNE
RAPPORT_DSS
```
### Configuration de validation de certificat
L'objet configuration de validation de certificat contient les éléments suivants :
- idVerifCert: ID unique de configuration de validation de certificat.
- description
- metadata : liste de métadonnées à inclure dans le rapport de validation ESignSante
- rules : règles ESignSante de validation de certificat.
Voici un exemple :
```json
{
	"idVerifSign": "1",
	"description": "",
	"metadata": "DN_CERTIFICAT,RAPPORT_DIAGNOSTIQUE,RAPPORT_DSS",
	"rules": "ExpirationCertificat,RevocationCertificat,SignatureCertificatValide,TrustedCertificat,NonRepudiation"
}
```

Valeurs possibles des règles et métadonnées :
```properties
# Liste des règles de validation, rules, d'une signature ; valeurs possibles :
SignatureCertificatValide
ExpirationCertificat
NonRepudiation
RevocationCertificat 
SignatureNonVide
TrustedCertificat
```
```properties
# Types de metadata retournées lors d'une validation de signature ; valeurs possibles :
DN_CERTIFICAT
RAPPORT_DIAGNOSTIQUE
RAPPORT_DSS
```
### Configuration des autorités de confiance et la liste de révocation
Un objet « ca » contient les éléments suivants :
- certificate: certificat de confiance au format pem, sur une ligne pour respecter le format Json, ‘\n' pour remplacer les retours à la line (tous les 64 caractères), sinon le certificat ne sera pas traité correctement. Ce certificat fera partie du bundle de CAs de confiance.
- crl : url (http ou ldap) de téléchargement de la CRL.
Voici un exemple :
```json
"ca": [
	{
	  "certificate": "-----BEGIN CERTIFICATE-----\n MIIHb ... 3cyxN\n -----END CERTIFICATE-----",
	  "crl": "ldap://annuaire-igc.esante.gouv.fr/cn=TEST%20AC%20IGC-SANTE%20ELEMENTAIRE%20ORGANISATIONS,ou=TEST%20AC%20RACINE%20IGC-SANTE%20ELEMENTAIRE,ou=IGC-SANTE%20TEST,ou=0002%20187512751,o=ASIP-SANTE,c=FR?certificaterevocationlist;binary?base?objectClass=pkiCA"
	},
]
```
Note : pour trouver l'url des CRLs associés à un certificat il suffit de consulter les [points de distribution](https://www.digicert.com/kb/util/utility-test-ocsp-and-crl-access-from-a-server.htm) de la CRL en ouvrant le certificat.

## Installation de l'Image Docker

### Installation à partir du TAR

```shell script
docker load --input [path-to]image.tar
```

`docker images ls` pour vérifier que l'image est bien chargé.

Pour facilité la tâche de démarrage du conteneur, il est recommender d'avoir le fichier `conf.json` et `application.properties` dans un répertoir accessible sur notre hôte. 
Le chemin vers ce répertoir sera `[path-to-conf-files-parent-dir]`.

Exemple du fichier `application.properties`:
```properties
spring.servlet.multipart.max-file-size=200MB
spring.servlet.multipart.max-request-size=2MB
config.secret=enable
server.servlet.context-path=/esignsante/v1
// config.crl.scheduling=[chron job pour rechargement des crl] (optionnel)
server.tomcat.accesslog.enabled=true
server.tomcat.accesslog.directory=/var/esignsante/logs
```

Pour démarrer le conteneur il suffit de lancer :
```shell script
docker run -v [path-to-conf-files-parent-dir]:/var/esignsante -p 8080:8080 -e JAVA_TOOL_OPTIONS="-Xms1g -Xmx1g -Dspring.config.location=/var/esignsante/application.properties -Dspring.profiles.active=swagger -Dhttp.proxyHost=[http-proxy-ip] -Dhttps.proxyHost=[https-proxy-ip]  -Dhttp.proxyPort=[http-proxy-port] -Dhttps.proxyPort=[https-proxy-port]" -d [image-id] --ws.conf=var/esignsante/conf.json
```
Options docker:
* -v : Se compose de deux champs dans notre cas, séparés par des deux points (:).
    - le premier champ est le chemin absolue d'accès au répertoire sur la machine hôte.
    - Le deuxième champ est le chemin absolue où le répertoire est monté dans le conteneur. Ici on a choisi `/var/esignsante`.
* -p : Pour lier un port du conteneur à un port de sont choix sur la machine hôte. Ici c'est tout simplement `8080:8080` (hôte:conteneur)
* -d : Detached, le conteneur tourne en tâche de fond. Ne pas renseinger pour dérouler les logs de démarrage.

Options jvm :
* `-Dspring.config.location` : chemin vers le fichier application.properties *sur le conteneur* (optionnel - ici /var/esignsante/application.properties)
* `-Dspring.profiles.active` : égale à `swagger` pour activer l'interface (désactivé par défaut)
* Proxy: `-Dhttp.proxyHost=`... `-Dhttps.proxyHost=`...  `-Dhttp.proxyPort=`... `-Dhttps.proxyPort=`... (optionnel)

Arguments java :
* `--ws.conf` : chemin vers le fichier de configuration *sur le conteneur* (obligatoir - ici /var/esignsante/conf.json)

L'appli est maintenant accessible sur http://localhost:8080/esignsante/v1/swagger-ui.html#/

Pour afficher les conteneurs :
```shell script
docker ps -all
```
Pour afficher les logs de l'appli :
```shell script
docker container logs [container-name]
```
Pour arrêter le conteneur :
```shell script
docker stop [container-name]
```
