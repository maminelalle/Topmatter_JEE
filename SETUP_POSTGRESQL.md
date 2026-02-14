# Configuration PostgreSQL

## Problème d'authentification

Si vous voyez l'erreur : `FATAL: authentification par mot de passe échouée pour l'utilisateur 'postgres'`

Cela signifie que le mot de passe PostgreSQL dans `application.yml` ne correspond pas à votre configuration PostgreSQL.

## Solutions

### Option 1 : Modifier le mot de passe dans application.yml

1. Ouvrez `backend/src/main/resources/application.yml`
2. Modifiez les lignes suivantes avec vos identifiants PostgreSQL :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/social_network
    username: postgres          # Votre nom d'utilisateur PostgreSQL
    password: VOTRE_MOT_DE_PASSE  # Votre mot de passe PostgreSQL
```

### Option 2 : Créer/modifier l'utilisateur PostgreSQL

Si vous avez accès à PostgreSQL en ligne de commande :

```sql
-- Se connecter en tant que superutilisateur
psql -U postgres

-- Changer le mot de passe de l'utilisateur postgres
ALTER USER postgres WITH PASSWORD 'postgres';

-- Ou créer un nouvel utilisateur
CREATE USER postgres WITH PASSWORD 'postgres';
ALTER USER postgres CREATEDB;
```

### Option 3 : Créer la base de données

Assurez-vous que la base `social_network` existe :

```sql
-- Se connecter à PostgreSQL
psql -U postgres

-- Créer la base de données
CREATE DATABASE social_network;

-- Vérifier qu'elle existe
\l
```

### Option 4 : Utiliser pgAdmin (interface graphique)

1. Ouvrez pgAdmin
2. Connectez-vous à votre serveur PostgreSQL
3. Clic droit sur "Databases" → "Create" → "Database"
4. Nom : `social_network`
5. Sauvegardez

## Vérification

Pour vérifier que PostgreSQL fonctionne :

```powershell
# Vérifier si PostgreSQL est démarré (Windows)
Get-Service -Name postgresql*

# Ou essayer de se connecter
psql -U postgres -h localhost
```

Une fois la configuration corrigée, relancez le backend :

```powershell
cd backend
.\run-backend.ps1
```
