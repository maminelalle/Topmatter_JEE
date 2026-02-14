# Script pour reinitialiser le mot de passe PostgreSQL apres modification de pg_hba.conf

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
Write-Host "ETAPE 1 : Redemarrage de PostgreSQL pour appliquer les changements..." -ForegroundColor Cyan
Write-Host ""

# Redemarrer PostgreSQL
try {
    Write-Host "Arret de PostgreSQL..." -ForegroundColor Yellow
    Stop-Service $serviceName -Force -ErrorAction Stop
    Start-Sleep -Seconds 2
    Write-Host "Demarrage de PostgreSQL..." -ForegroundColor Yellow
    Start-Service $serviceName -ErrorAction Stop
    Start-Sleep -Seconds 3
    Write-Host "[OK] PostgreSQL redemarre" -ForegroundColor Green
} catch {
    Write-Host "[ERREUR] Impossible de redemarrer PostgreSQL : $_" -ForegroundColor Red
    Write-Host "Redemarrez manuellement le service PostgreSQL" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "ETAPE 2 : Connexion sans mot de passe..." -ForegroundColor Cyan
Write-Host ""

# Tester la connexion sans mot de passe
$testConnection = & $psqlPath -U postgres -c "SELECT version();" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERREUR] Impossible de se connecter sans mot de passe." -ForegroundColor Red
    Write-Host "Verifiez que pg_hba.conf a bien ete modifie avec 'trust'." -ForegroundColor Yellow
    exit 1
}

Write-Host "[OK] Connexion reussie sans mot de passe" -ForegroundColor Green
Write-Host ""

Write-Host "ETAPE 3 : Reinitialisation du mot de passe..." -ForegroundColor Cyan
Write-Host ""

# Demander le nouveau mot de passe
$newPassword = Read-Host "Entrez le nouveau mot de passe pour l'utilisateur 'postgres'" -AsSecureString
$newPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($newPassword))

# Changer le mot de passe
Write-Host "Changement du mot de passe..." -ForegroundColor Yellow
$changePasswordSQL = "ALTER USER postgres WITH PASSWORD '$newPasswordPlain';"
$result = & $psqlPath -U postgres -c $changePasswordSQL 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Mot de passe modifie avec succes !" -ForegroundColor Green
} else {
    Write-Host "[ERREUR] Impossible de modifier le mot de passe : $result" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "ETAPE 4 : Remise en securite de pg_hba.conf..." -ForegroundColor Cyan
Write-Host ""

# Remettre scram-sha-256 dans pg_hba.conf
$pgHbaPath = "C:\Program Files\PostgreSQL\$($psqlPath -replace '.*\\(\d+)\\bin.*', '$1')\data\pg_hba.conf"

if (Test-Path $pgHbaPath) {
    Write-Host "Remise de scram-sha-256 dans pg_hba.conf..." -ForegroundColor Yellow
    
    $content = Get-Content $pgHbaPath -Raw
    $content = $content -replace 'local\s+all\s+all\s+trust', 'local   all             all                                     scram-sha-256'
    $content = $content -replace 'host\s+all\s+all\s+::1/128\s+trust', 'host    all             all             ::1/128                 scram-sha-256'
    $content = $content -replace 'local\s+replication\s+all\s+trust', 'local   replication     all                                     scram-sha-256'
    $content = $content -replace 'host\s+replication\s+all\s+127\.0\.0\.1/32\s+trust', 'host    replication     all             127.0.0.1/32            scram-sha-256'
    $content = $content -replace 'host\s+replication\s+all\s+::1/128\s+trust', 'host    replication     all             ::1/128                 scram-sha-256'
    
    Set-Content -Path $pgHbaPath -Value $content -NoNewline
    
    Write-Host "[OK] pg_hba.conf remis en securite" -ForegroundColor Green
    
    # Redemarrer PostgreSQL pour appliquer les changements
    Write-Host ""
    Write-Host "Redemarrage de PostgreSQL pour appliquer la securite..." -ForegroundColor Yellow
    Stop-Service $serviceName -Force
    Start-Sleep -Seconds 2
    Start-Service $serviceName
    Start-Sleep -Seconds 3
    Write-Host "[OK] PostgreSQL redemarre avec securite activee" -ForegroundColor Green
} else {
    Write-Host "[ATTENTION] Impossible de trouver pg_hba.conf. Remettez manuellement scram-sha-256." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "ETAPE 5 : Test de connexion avec le nouveau mot de passe..." -ForegroundColor Cyan
Write-Host ""

# Tester avec le nouveau mot de passe
$env:PGPASSWORD = $newPasswordPlain
$testResult = & $psqlPath -U postgres -c "SELECT current_user;" 2>&1
Remove-Item Env:\PGPASSWORD

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Connexion reussie avec le nouveau mot de passe !" -ForegroundColor Green
} else {
    Write-Host "[ERREUR] La connexion avec le nouveau mot de passe a echoue." -ForegroundColor Red
    Write-Host "Essayez de vous connecter manuellement pour verifier." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== RESUME ===" -ForegroundColor Cyan
Write-Host "Nouveau mot de passe : $newPasswordPlain" -ForegroundColor White
Write-Host ""
Write-Host "IMPORTANT : Modifiez maintenant application.yml avec ce mot de passe :" -ForegroundColor Yellow
Write-Host "  backend/src/main/resources/application.yml" -ForegroundColor White
Write-Host "  spring.datasource.password: $newPasswordPlain" -ForegroundColor White
Write-Host ""
Write-Host "Puis créez la base de données avec :" -ForegroundColor Yellow
Write-Host "  .\creer-base.ps1" -ForegroundColor White
Write-Host ""
