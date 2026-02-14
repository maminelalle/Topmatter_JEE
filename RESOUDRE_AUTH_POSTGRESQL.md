# Résoudre le problème d'authentification PostgreSQL

## Problème
```
FATAL: authentification par mot de passe échouée pour l'utilisateur 'postgres'
```

Cela signifie que le mot de passe que vous avez entré ne correspond pas au mot de passe de l'utilisateur `postgres` dans PostgreSQL.

## Solutions

### Solution 1 : Réinitialiser le mot de passe PostgreSQL (Recommandé)

#### Méthode A : Via pgAdmin (Interface graphique)

1. **Ouvrez pgAdmin**
2. **Connectez-vous** au serveur PostgreSQL (si vous avez oublié le mot de passe, voir Méthode B)
3. **Développez** votre serveur → **Login/Group Roles**
4. **Clic droit** sur `postgres` → **Properties**
5. **Onglet "Definition"** → Modifiez le mot de passe
6. **Cliquez sur "Save"**

#### Méthode B : Réinitialiser via fichier de configuration

Si vous ne pouvez pas vous connecter à pgAdmin :

1. **Arrêtez le service PostgreSQL** :
   ```powershell
   Stop-Service postgresql-x64-17
   # Ou le nom exact de votre service :
   Get-Service postgresql*
   ```

2. **Modifiez le fichier `pg_hba.conf`** :
   - Emplacement : `C:\Program Files\PostgreSQL\17\data\pg_hba.conf`
   - Trouvez la ligne qui commence par :
     ```
     host    all             all             127.0.0.1/32            scram-sha-256
     ```
   - Remplacez `scram-sha-256` par `trust` :
     ```
     host    all             all             127.0.0.1/32            trust
     ```

3. **Redémarrez PostgreSQL** :
   ```powershell
   Start-Service postgresql-x64-17
   ```

4. **Connectez-vous sans mot de passe** et changez le mot de passe :
   ```powershell
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres
   ```
   Puis dans psql :
   ```sql
   ALTER USER postgres WITH PASSWORD 'postgres';
   ```

5. **Remettez `scram-sha-256` dans `pg_hba.conf`** et redémarrez PostgreSQL.

### Solution 2 : Utiliser un autre utilisateur PostgreSQL

Si vous avez un autre utilisateur avec un mot de passe connu :

1. **Modifiez `backend/src/main/resources/application.yml`** :
   ```yaml
   spring:
     datasource:
       username: votre_autre_utilisateur
       password: son_mot_de_passe
   ```

2. **Assurez-vous que cet utilisateur a les droits** :
   ```sql
   GRANT ALL PRIVILEGES ON DATABASE social_network TO votre_utilisateur;
   ```

### Solution 3 : Créer un nouvel utilisateur pour l'application

1. **Connectez-vous en tant qu'administrateur** (via pgAdmin ou un autre compte)
2. **Créez un nouvel utilisateur** :
   ```sql
   CREATE USER social_network_user WITH PASSWORD 'votre_mot_de_passe';
   ALTER USER social_network_user CREATEDB;
   ```

3. **Modifiez `application.yml`** :
   ```yaml
   spring:
     datasource:
       username: social_network_user
       password: votre_mot_de_passe
   ```

### Solution 4 : Vérifier le mot de passe actuel

Si vous pensez connaître le mot de passe mais qu'il ne fonctionne pas :

1. **Essayez de vous connecter directement** :
   ```powershell
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost
   ```

2. **Si cela fonctionne**, le problème vient peut-être du script. Essayez de créer la base manuellement :
   ```sql
   CREATE DATABASE social_network;
   ```

## Vérification

Une fois le mot de passe corrigé, testez la connexion :

```powershell
$env:PGPASSWORD = "postgres"  # Ou votre nouveau mot de passe
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -c "\l"
Remove-Item Env:\PGPASSWORD
```

Vous devriez voir la liste des bases de données sans erreur.

## Après résolution

1. **Relancez le script** :
   ```powershell
   .\creer-base.ps1
   ```

2. **Ou créez la base manuellement** :
   ```powershell
   $env:PGPASSWORD = "votre_mot_de_passe"
   & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -c "CREATE DATABASE social_network;"
   Remove-Item Env:\PGPASSWORD
   ```

3. **Vérifiez que `application.yml` utilise le bon mot de passe** :
   ```yaml
   spring:
     datasource:
       password: votre_mot_de_passe  # Doit correspondre au mot de passe PostgreSQL
   ```

4. **Lancez le backend** :
   ```powershell
   cd backend
   .\run-backend.ps1
   ```
