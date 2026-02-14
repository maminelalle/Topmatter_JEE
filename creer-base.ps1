# Script pour creer la base de donnees social_network

Write-Host "=== Creation de la base de donnees PostgreSQL ===" -ForegroundColor Cyan
Write-Host ""

# Trouver PostgreSQL
$pgVersions = @(17, 16, 15, 14, 13, 12)
$psqlPath = $null

foreach ($version in $pgVersions) {
    $testPath = "C:\Program Files\PostgreSQL\$version\bin\psql.exe"
    if (Test-Path $testPath) {
        $psqlPath = $testPath
        Write-Host "PostgreSQL trouve : Version $version" -ForegroundColor Green
        break
    }
}

# Si pas trouve, chercher dans Program Files (x86)
if (-not $psqlPath) {
    foreach ($version in $pgVersions) {
        $testPath = "C:\Program Files (x86)\PostgreSQL\$version\bin\psql.exe"
        if (Test-Path $testPath) {
            $psqlPath = $testPath
            Write-Host "PostgreSQL trouve : Version $version" -ForegroundColor Green
            break
        }
    }
}

if (-not $psqlPath) {
    Write-Host ""
    Write-Host "ERREUR : PostgreSQL non trouve dans les emplacements standards." -ForegroundColor Red
    Write-Host ""
    Write-Host "Veuillez specifier le chemin manuellement :" -ForegroundColor Yellow
    Write-Host "  Exemple : & 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -U postgres -c 'CREATE DATABASE social_network;'" -ForegroundColor White
    Write-Host ""
    Write-Host "Ou utilisez pgAdmin pour creer la base de donnees manuellement." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Verification de l'existence de la base de donnees..." -ForegroundColor Cyan

# Demander le mot de passe
$password = Read-Host "Entrez le mot de passe de l'utilisateur 'postgres'" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

# Variable d'environnement pour le mot de passe
$env:PGPASSWORD = $passwordPlain

# Verifier si la base existe deja
$checkResult = & $psqlPath -U postgres -t -c "SELECT 1 FROM pg_database WHERE datname='social_network';" 2>&1
$exists = $checkResult -match "1"

if ($exists) {
    Write-Host ""
    Write-Host "La base de donnees 'social_network' existe deja !" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Voulez-vous la supprimer et la recreer ? (O/N)" -ForegroundColor Yellow
    $response = Read-Host
    if ($response -eq "O" -or $response -eq "o") {
        Write-Host "Suppression de l'ancienne base..." -ForegroundColor Cyan
        & $psqlPath -U postgres -c "DROP DATABASE social_network;" 2>&1 | Out-Null
        Write-Host "Creation de la nouvelle base..." -ForegroundColor Cyan
        & $psqlPath -U postgres -c "CREATE DATABASE social_network;" 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "[OK] Base de donnees 'social_network' creee avec succes !" -ForegroundColor Green
        } else {
            Write-Host ""
            Write-Host "[ERREUR] Erreur lors de la creation." -ForegroundColor Red
        }
    } else {
        Write-Host "Operation annulee." -ForegroundColor Yellow
    }
} else {
    Write-Host "Creation de la base de donnees..." -ForegroundColor Cyan
    & $psqlPath -U postgres -c "CREATE DATABASE social_network;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "[OK] Base de donnees 'social_network' creee avec succes !" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "[ERREUR] Erreur lors de la creation." -ForegroundColor Red
        Write-Host "Verifiez que PostgreSQL est demarre et que le mot de passe est correct." -ForegroundColor Yellow
    }
}

# Nettoyer
Remove-Item Env:\PGPASSWORD

Write-Host ""
Write-Host "Vous pouvez maintenant lancer le backend avec :" -ForegroundColor Cyan
Write-Host "  cd backend" -ForegroundColor White
Write-Host "  .\run-backend.ps1" -ForegroundColor White
Write-Host ""
