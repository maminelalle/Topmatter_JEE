# Script pour lancer le frontend Angular

Write-Host "=== Lancement du frontend Angular ===" -ForegroundColor Cyan
Write-Host ""

# Verifier que nous sommes dans le bon repertoire
if (-not (Test-Path "frontend\package.json")) {
    Write-Host "[ERREUR] Le dossier frontend n'existe pas ou package.json est introuvable." -ForegroundColor Red
    Write-Host "Assurez-vous d'etre dans le repertoire racine du projet." -ForegroundColor Yellow
    exit 1
}

# Aller dans le dossier frontend
Set-Location frontend

Write-Host "ETAPE 1 : Verification de Node.js et npm..." -ForegroundColor Cyan
Write-Host ""

# Verifier Node.js
try {
    $nodeVersion = node --version
    Write-Host "[OK] Node.js installe : $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERREUR] Node.js n'est pas installe ou n'est pas dans le PATH." -ForegroundColor Red
    Write-Host "Installez Node.js depuis https://nodejs.org/" -ForegroundColor Yellow
    exit 1
}

# Verifier npm
try {
    $npmVersion = npm --version
    Write-Host "[OK] npm installe : $npmVersion" -ForegroundColor Green
} catch {
    Write-Host "[ERREUR] npm n'est pas installe." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "ETAPE 2 : Installation des dependances npm..." -ForegroundColor Cyan
Write-Host ""

# Verifier si node_modules existe
if (-not (Test-Path "node_modules")) {
    Write-Host "Installation des dependances (cela peut prendre quelques minutes)..." -ForegroundColor Yellow
    npm install
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERREUR] L'installation des dependances a echoue." -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Dependances installees" -ForegroundColor Green
} else {
    Write-Host "[INFO] Les dependances semblent deja installees." -ForegroundColor Yellow
    Write-Host "Pour reinstaller, supprimez le dossier node_modules et relancez ce script." -ForegroundColor White
}

Write-Host ""
Write-Host "ETAPE 3 : Lancement du serveur de developpement Angular..." -ForegroundColor Cyan
Write-Host ""
Write-Host "Le serveur va demarrer sur http://localhost:4200" -ForegroundColor Green
Write-Host "Appuyez sur Ctrl+C pour arreter le serveur." -ForegroundColor Yellow
Write-Host ""

# Lancer le serveur Angular
npm start
