# Script PowerShell pour lancer le backend Spring Boot
Write-Host "Lancement du backend Spring Boot..." -ForegroundColor Green

# Vérifier Java
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "ERREUR: Java n'est pas installé ou n'est pas dans le PATH." -ForegroundColor Red
    Write-Host "Veuillez installer Java 17+ et configurer JAVA_HOME." -ForegroundColor Yellow
    exit 1
}

# Configurer JAVA_HOME automatiquement si non défini
if (-not $env:JAVA_HOME) {
    Write-Host "Configuration automatique de JAVA_HOME..." -ForegroundColor Yellow
    $javaExe = (Get-Command java).Source
    $javaHome = "C:\Program Files\Java\jdk-24"
    
    # Vérifier si le chemin standard existe
    if (-not (Test-Path "$javaHome\bin\java.exe")) {
        # Chercher dans les emplacements standards
        $possiblePaths = @(
            "C:\Program Files\Java\jdk-24",
            "C:\Program Files\Java\jdk-24.0.2",
            "C:\Program Files\Java\jdk-17",
            "C:\Program Files (x86)\Java\jdk-24"
        )
        
        foreach ($path in $possiblePaths) {
            if (Test-Path "$path\bin\java.exe") {
                $javaHome = $path
                break
            }
        }
        
        # Si toujours pas trouvé, essayer de déduire depuis java.exe
        if (-not (Test-Path "$javaHome\bin\java.exe")) {
            $javaHome = Split-Path (Split-Path $javaExe)
            # Si c'est un lien symbolique Oracle, chercher le vrai JDK
            if ($javaExe -like "*Common Files*") {
                $javaHome = "C:\Program Files\Java\jdk-24"
            }
        }
    }
    
    if (Test-Path "$javaHome\bin\java.exe") {
        $env:JAVA_HOME = $javaHome
        Write-Host "JAVA_HOME configuré à: $javaHome" -ForegroundColor Green
    } else {
        Write-Host "ATTENTION: Impossible de trouver JAVA_HOME automatiquement." -ForegroundColor Yellow
        Write-Host "Veuillez définir la variable d'environnement JAVA_HOME manuellement." -ForegroundColor Yellow
    }
}

# Afficher la version Java
Write-Host "`nVersion Java:" -ForegroundColor Cyan
java -version

# Lancer avec Maven Wrapper
Write-Host "`nDémarrage du serveur Spring Boot..." -ForegroundColor Green
Write-Host "Le serveur sera accessible sur http://localhost:8080/api" -ForegroundColor Cyan
Write-Host "Appuyez sur Ctrl+C pour arrêter le serveur`n" -ForegroundColor Yellow

.\mvnw.cmd spring-boot:run
