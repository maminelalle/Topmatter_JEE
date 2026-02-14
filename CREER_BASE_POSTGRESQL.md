# Commandes pour créer la base de données PostgreSQL

## Méthode 1 : Via psql (ligne de commande)

### Étape 1 : Trouver l'emplacement de PostgreSQL

PostgreSQL est généralement installé dans :
- `C:\Program Files\PostgreSQL\<version>\bin\`
- Par exemple : `C:\Program Files\PostgreSQL\15\bin\` ou `C:\Program Files\PostgreSQL\16\bin\`

### Étape 2 : Se connecter à PostgreSQL

Ouvrez PowerShell et exécutez (remplacez `<version>` par votre version, par exemple `15` ou `16`) :

```powershell
# Option A : Si PostgreSQL est dans le PATH
psql -U postgres

# Option B : Avec le chemin complet
& "C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres

# Option C : Si vous ne connaissez pas la version, cherchez-la
Get-ChildItem "C:\Program Files\PostgreSQL" -Directory
```

Vous serez invité à entrer le mot de passe de l'utilisateur `postgres`.

### Étape 3 : Créer la base de données

Une fois connecté à PostgreSQL, exécutez :

```sql
-- Vérifier les bases existantes
\l

-- Créer la base de données social_network
CREATE DATABASE social_network;

-- Vérifier qu'elle a été créée
\l

-- Se connecter à la nouvelle base
\c social_network

-- Quitter psql
\q
```

## Méthode 2 : Via pgAdmin (interface graphique)

1. **Ouvrez pgAdmin** (généralement dans le menu Démarrer)
2. **Connectez-vous** au serveur PostgreSQL (clic droit sur "Servers" → "Create" → "Server" si nécessaire)
3. **Développez** votre serveur PostgreSQL
4. **Clic droit** sur "Databases" → **"Create"** → **"Database..."**
5. **Nom** : `social_network`
6. **Cliquez sur "Save"**

## Méthode 3 : Script PowerShell complet

Créez un fichier `creer-base.ps1` avec ce contenu :

```powershell
# Script pour créer la base de données social_network

# Trouver PostgreSQL
$pgPaths = @(
    "C:\Program Files\PostgreSQL\16\bin\psql.exe",
    "C:\Program Files\PostgreSQL\15\bin\psql.exe",
    "C:\Program Files\PostgreSQL\14\bin\psql.exe",
    "C:\Program Files\PostgreSQL\13\bin\psql.exe"
)

$psqlPath = $null
foreach ($path in $pgPaths) {
    if (Test-Path $path) {
        $psqlPath = $path
        break
    }
}

if (-not $psqlPath) {
    Write-Host "PostgreSQL non trouvé. Veuillez installer PostgreSQL ou spécifier le chemin manuellement." -ForegroundColor Red
    exit 1
}

Write-Host "PostgreSQL trouvé : $psqlPath" -ForegroundColor Green
Write-Host "Création de la base de données social_network..." -ForegroundColor Cyan

# Demander le mot de passe
$password = Read-Host "Entrez le mot de passe de l'utilisateur 'postgres'" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

# Variable d'environnement pour le mot de passe
$env:PGPASSWORD = $passwordPlain

# Créer la base de données
& $psqlPath -U postgres -c "CREATE DATABASE social_network;" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "Base de données 'social_network' créée avec succès !" -ForegroundColor Green
} else {
    Write-Host "Erreur lors de la création. La base existe peut-être déjà." -ForegroundColor Yellow
    # Vérifier si elle existe
    & $psqlPath -U postgres -c "\l" | Select-String "social_network"
}

# Nettoyer
Remove-Item Env:\PGPASSWORD
```

## Méthode 4 : Commande SQL directe (si vous connaissez le mot de passe)

```powershell
# Remplacez <VERSION> par votre version PostgreSQL (15, 16, etc.)
# Remplacez <MOT_DE_PASSE> par votre mot de passe PostgreSQL

$env:PGPASSWORD = "<MOT_DE_PASSE>"
& "C:\Program Files\PostgreSQL\<VERSION>\bin\psql.exe" -U postgres -c "CREATE DATABASE social_network;"
Remove-Item Env:\PGPASSWORD
```

## Vérification

Pour vérifier que la base existe :

```powershell
# Se connecter et lister les bases
& "C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -c "\l"
```

Vous devriez voir `social_network` dans la liste.

## Si vous avez oublié le mot de passe PostgreSQL

1. **Via pgAdmin** : Clic droit sur le serveur → "Properties" → "Connection" pour voir/modifier
2. **Réinitialiser** : Modifiez le fichier `pg_hba.conf` (généralement dans `C:\Program Files\PostgreSQL\<version>\data\`) pour permettre les connexions sans mot de passe temporairement
3. **Ou** : Utilisez le compte Windows si PostgreSQL a été installé avec l'authentification Windows

## Après création de la base

Une fois la base créée, modifiez `backend/src/main/resources/application.yml` si nécessaire pour correspondre à vos identifiants PostgreSQL, puis relancez le backend.
