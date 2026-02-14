# Script pour vérifier la configuration PostgreSQL
Write-Host "Vérification de PostgreSQL..." -ForegroundColor Cyan

# Vérifier si PostgreSQL est installé et démarré
$pgService = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue
if ($pgService) {
    Write-Host "`nService PostgreSQL trouvé :" -ForegroundColor Green
    $pgService | Format-Table Name, Status, DisplayName
} else {
    Write-Host "`nATTENTION: Service PostgreSQL non trouvé." -ForegroundColor Yellow
    Write-Host "Assurez-vous que PostgreSQL est installé et démarré." -ForegroundColor Yellow
}

# Essayer de se connecter avec psql si disponible
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if ($psqlPath) {
    Write-Host "`nTentative de connexion à PostgreSQL..." -ForegroundColor Cyan
    Write-Host "Si vous êtes invité à entrer un mot de passe, entrez celui de l'utilisateur 'postgres'" -ForegroundColor Yellow
    Write-Host "Pour annuler, appuyez sur Ctrl+C`n" -ForegroundColor Yellow
} else {
    Write-Host "`npsql n'est pas dans le PATH." -ForegroundColor Yellow
    Write-Host "Pour tester la connexion, utilisez pgAdmin ou modifiez directement application.yml" -ForegroundColor Yellow
}

Write-Host "`nConfiguration actuelle dans application.yml :" -ForegroundColor Cyan
Write-Host "  URL: jdbc:postgresql://localhost:5432/social_network" -ForegroundColor White
Write-Host "  Username: postgres" -ForegroundColor White
Write-Host "  Password: postgres" -ForegroundColor White

Write-Host "`nPour modifier ces paramètres, éditez : backend/src/main/resources/application.yml" -ForegroundColor Yellow
Write-Host "`nAssurez-vous que :" -ForegroundColor Cyan
Write-Host "  1. PostgreSQL est démarré" -ForegroundColor White
Write-Host "  2. La base 'social_network' existe (CREATE DATABASE social_network;)" -ForegroundColor White
Write-Host "  3. L'utilisateur 'postgres' existe avec le bon mot de passe" -ForegroundColor White
