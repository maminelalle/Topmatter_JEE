# Script pour lancer le frontend Angular (depuis le dossier frontend)

Write-Host "=== Lancement du frontend Angular ===" -ForegroundColor Cyan
Write-Host ""

# Verifier Node.js
try {
    $nodeVersion = node --version
    Write-Host "[OK] Node.js : $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERREUR] Node.js n'est pas installe." -ForegroundColor Red
    Write-Host "Installez Node.js depuis https://nodejs.org/" -ForegroundColor Yellow
    exit 1
}

# Verifier npm
try {
    $npmVersion = npm --version
    Write-Host "[OK] npm : $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERREUR] npm n'est pas installe." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Installation des dependances (si necessaire)..." -ForegroundColor Cyan

# Installer les dependances si node_modules n'existe pas
if (-not (Test-Path "node_modules")) {
    Write-Host "Installation en cours (cela peut prendre quelques minutes)..." -ForegroundColor Yellow
    npm install
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERREUR] L'installation a echoue." -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Dependances installees" -ForegroundColor Green
} else {
    Write-Host "[INFO] Dependances deja installees." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Lancement du serveur Angular..." -ForegroundColor Cyan
Write-Host "Le serveur sera accessible sur : http://localhost:4200" -ForegroundColor Green
Write-Host "Appuyez sur Ctrl+C pour arreter le serveur." -ForegroundColor Yellow
Write-Host ""

# Lancer le serveur
npm start
