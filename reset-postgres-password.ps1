# Script pour reinitialiser le mot de passe PostgreSQL

Write-Host "=== Reinitialisation du mot de passe PostgreSQL ===" -ForegroundColor Cyan
Write-Host ""

# Trouver PostgreSQL
$pgVersions = @(17, 16, 15, 14, 13, 12)
$psqlPath = $null
$serviceName = $null

foreach ($version in $pgVersions) {
    $testPath = "C:\Program Files\PostgreSQL\$version\bin\psql.exe"
    if (Test-Path $testPath) {
        $psqlPath = $testPath
        $serviceName = "postgresql-x64-$version"
        Write-Host "PostgreSQL trouve : Version $version" -ForegroundColor Green
        break
    }
}

if (-not $psqlPath) {
    Write-Host "ERREUR : PostgreSQL non trouve." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Ce script va vous aider a reinitialiser le mot de passe de l'utilisateur 'postgres'." -ForegroundColor Yellow
Write-Host ""
Write-Host "OPTIONS :" -ForegroundColor Cyan
Write-Host "1. Changer le mot de passe via pgAdmin (recommandé)" -ForegroundColor White
Write-Host "2. Reinitialiser via modification de pg_hba.conf (avance)" -ForegroundColor White
Write-Host "3. Tester la connexion avec un mot de passe" -ForegroundColor White
Write-Host ""
$choice = Read-Host "Choisissez une option (1/2/3)"

if ($choice -eq "1") {
    Write-Host ""
    Write-Host "Instructions pour pgAdmin :" -ForegroundColor Cyan
    Write-Host "1. Ouvrez pgAdmin" -ForegroundColor White
    Write-Host "2. Connectez-vous au serveur PostgreSQL" -ForegroundColor White
    Write-Host "3. Developpez votre serveur -> Login/Group Roles" -ForegroundColor White
    Write-Host "4. Clic droit sur 'postgres' -> Properties" -ForegroundColor White
    Write-Host "5. Onglet 'Definition' -> Modifiez le mot de passe" -ForegroundColor White
    Write-Host "6. Cliquez sur 'Save'" -ForegroundColor White
    Write-Host ""
    Write-Host "Apres avoir change le mot de passe, modifiez aussi application.yml avec le nouveau mot de passe." -ForegroundColor Yellow
    
} elseif ($choice -eq "2") {
    Write-Host ""
    Write-Host "ATTENTION : Cette methode necessite des droits administrateur." -ForegroundColor Red
    Write-Host ""
    $confirm = Read-Host "Voulez-vous continuer ? (O/N)"
    if ($confirm -ne "O" -and $confirm -ne "o") {
        Write-Host "Operation annulee." -ForegroundColor Yellow
        exit 0
    }
    
    $pgDataPath = "C:\Program Files\PostgreSQL\$($psqlPath -replace '.*\\(\d+)\\bin.*', '$1')\data\pg_hba.conf"
    Write-Host ""
    Write-Host "Fichier pg_hba.conf : $pgDataPath" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Arretez PostgreSQL :" -ForegroundColor White
    Write-Host "   Stop-Service $serviceName" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "2. Ouvrez pg_hba.conf dans un editeur de texte (en tant qu'administrateur)" -ForegroundColor White
    Write-Host ""
    Write-Host "3. Trouvez la ligne :" -ForegroundColor White
    Write-Host "   host    all             all             127.0.0.1/32            scram-sha-256" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "4. Remplacez 'scram-sha-256' par 'trust'" -ForegroundColor White
    Write-Host ""
    Write-Host "5. Redemarrez PostgreSQL :" -ForegroundColor White
    Write-Host "   Start-Service $serviceName" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "6. Connectez-vous et changez le mot de passe :" -ForegroundColor White
    Write-Host "   & '$psqlPath' -U postgres" -ForegroundColor Yellow
    Write-Host "   ALTER USER postgres WITH PASSWORD 'postgres';" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "7. Remettez 'scram-sha-256' dans pg_hba.conf et redemarrez PostgreSQL" -ForegroundColor White
    
} elseif ($choice -eq "3") {
    Write-Host ""
    Write-Host "Test de connexion PostgreSQL" -ForegroundColor Cyan
    Write-Host ""
    $testPassword = Read-Host "Entrez le mot de passe a tester" -AsSecureString
    $testPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($testPassword))
    
    $env:PGPASSWORD = $testPasswordPlain
    $testResult = & $psqlPath -U postgres -c "SELECT version();" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "[OK] Le mot de passe fonctionne !" -ForegroundColor Green
        Write-Host ""
        Write-Host "Assurez-vous que application.yml utilise ce mot de passe :" -ForegroundColor Yellow
        Write-Host "  spring.datasource.password: $testPasswordPlain" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "[ERREUR] Le mot de passe ne fonctionne pas." -ForegroundColor Red
        Write-Host "Essayez une autre solution ou reinitialisez le mot de passe." -ForegroundColor Yellow
    }
    
    Remove-Item Env:\PGPASSWORD
} else {
    Write-Host "Option invalide." -ForegroundColor Red
}

Write-Host ""
