# Script pour finaliser la configuration PostgreSQL apres changement de mot de passe

Write-Host "=== Finalisation de la configuration PostgreSQL ===" -ForegroundColor Cyan
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
Write-Host "ETAPE 1 : Remise en securite de pg_hba.conf..." -ForegroundColor Cyan
Write-Host ""

# Remettre scram-sha-256 dans pg_hba.conf
$pgHbaPath = "C:\Program Files\PostgreSQL\$($psqlPath -replace '.*\\(\d+)\\bin.*', '$1')\data\pg_hba.conf"

if (Test-Path $pgHbaPath) {
    Write-Host "Modification de pg_hba.conf pour remettre la securite..." -ForegroundColor Yellow
    
    $content = Get-Content $pgHbaPath -Raw
    
    # Remplacer trust par scram-sha-256 pour toutes les lignes sauf IPv4 local qui reste trust temporairement
    $content = $content -replace 'local\s+all\s+all\s+trust', 'local   all             all                                     scram-sha-256'
    $content = $content -replace 'host\s+all\s+all\s+::1/128\s+trust', 'host    all             all             ::1/128                 scram-sha-256'
    $content = $content -replace 'local\s+replication\s+all\s+trust', 'local   replication     all                                     scram-sha-256'
    $content = $content -replace 'host\s+replication\s+all\s+127\.0\.0\.1/32\s+trust', 'host    replication     all             127.0.0.1/32            scram-sha-256'
    $content = $content -replace 'host\s+replication\s+all\s+::1/128\s+trust', 'host    replication     all             ::1/128                 scram-sha-256'
    
    Set-Content -Path $pgHbaPath -Value $content -NoNewline
    
    Write-Host "[OK] pg_hba.conf modifie" -ForegroundColor Green
} else {
    Write-Host "[ERREUR] Impossible de trouver pg_hba.conf" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "ETAPE 2 : Redemarrage de PostgreSQL..." -ForegroundColor Cyan
Write-Host ""

# Redemarrer PostgreSQL
try {
    Write-Host "Arret de PostgreSQL..." -ForegroundColor Yellow
    Stop-Service $serviceName -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    Write-Host "Demarrage de PostgreSQL..." -ForegroundColor Yellow
    Start-Service $serviceName -ErrorAction Stop
    Start-Sleep -Seconds 3
    Write-Host "[OK] PostgreSQL redemarre" -ForegroundColor Green
} catch {
    Write-Host "[ATTENTION] Impossible de redemarrer automatiquement." -ForegroundColor Yellow
    Write-Host "Redemarrez manuellement avec :" -ForegroundColor Yellow
    Write-Host "  Stop-Service $serviceName" -ForegroundColor White
    Write-Host "  Start-Service $serviceName" -ForegroundColor White
}

Write-Host ""
Write-Host "ETAPE 3 : Test de connexion avec le mot de passe 'postgres'..." -ForegroundColor Cyan
Write-Host ""

# Tester avec le nouveau mot de passe
$env:PGPASSWORD = "postgres"
$testResult = & $psqlPath -U postgres -c "SELECT current_user, version();" 2>&1
Remove-Item Env:\PGPASSWORD

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Connexion reussie avec le mot de passe 'postgres' !" -ForegroundColor Green
} else {
    Write-Host "[ERREUR] La connexion avec le mot de passe a echoue." -ForegroundColor Red
    Write-Host "Resultat : $testResult" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Essayez de vous connecter manuellement pour verifier le mot de passe." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "ETAPE 4 : Creation de la base de donnees 'social_network'..." -ForegroundColor Cyan
Write-Host ""

# Creer la base de donnees
$env:PGPASSWORD = "postgres"
$createDbResult = & $psqlPath -U postgres -c "CREATE DATABASE social_network;" 2>&1
Remove-Item Env:\PGPASSWORD

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Base de donnees 'social_network' creee avec succes !" -ForegroundColor Green
} else {
    if ($createDbResult -match "already exists") {
        Write-Host "[INFO] La base de donnees 'social_network' existe deja." -ForegroundColor Yellow
    } else {
        Write-Host "[ERREUR] Impossible de creer la base de donnees : $createDbResult" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "ETAPE 5 : Verification de application.yml..." -ForegroundColor Cyan
Write-Host ""

$appYmlPath = "backend\src\main\resources\application.yml"
if (Test-Path $appYmlPath) {
    $appYmlContent = Get-Content $appYmlPath -Raw
    if ($appYmlContent -match "password:\s*postgres") {
        Write-Host "[OK] application.yml contient deja le mot de passe 'postgres'" -ForegroundColor Green
    } else {
        Write-Host "[ATTENTION] Verifiez que application.yml contient le bon mot de passe." -ForegroundColor Yellow
        Write-Host "Le fichier doit contenir : spring.datasource.password: postgres" -ForegroundColor White
    }
} else {
    Write-Host "[ATTENTION] application.yml non trouve." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== CONFIGURATION TERMINEE ===" -ForegroundColor Green
Write-Host ""
Write-Host "Resume :" -ForegroundColor Cyan
Write-Host "  - Mot de passe PostgreSQL : postgres" -ForegroundColor White
Write-Host "  - Base de donnees : social_network (creee)" -ForegroundColor White
Write-Host "  - pg_hba.conf : Securite reactivee (scram-sha-256)" -ForegroundColor White
Write-Host ""
Write-Host "Vous pouvez maintenant lancer le backend avec :" -ForegroundColor Cyan
Write-Host "  cd backend" -ForegroundColor White
Write-Host "  .\run-backend.ps1" -ForegroundColor White
Write-Host ""
