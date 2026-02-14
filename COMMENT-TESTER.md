# Comment tester (tests, Docker)

Guide pour vérifier que tout fonctionne : tests backend/frontend puis Docker.

---

## 1. Tester les tests backend

Ouvre un terminal à la racine du projet.

**Avec Maven installé :**
```powershell
cd backend
mvn test
```

**Avec le wrapper Windows :**
```powershell
cd backend
.\mvnw.cmd test
```

**Résultat attendu :** `BUILD SUCCESS` et des lignes du type :
- `AuthControllerTest` : 2 tests passés
- `ApplicationContextTest` : 1 test passé  

Si tu vois des erreurs, vérifie que Java 17 est bien utilisé (`java -version`).

---

## 2. Tester les tests frontend

Dans un nouveau terminal :

```powershell
cd frontend
npm install
npm run test
```

**Résultat attendu :** Karma lance ChromeHeadless, puis quelque chose comme :
- `AppComponent should create the app` – OK
- `AppComponent should have title "frontend"` – OK  
- `TOTAL: 2 SUCCESS`

Si un message demande d’installer Chrome/ChromeHeadless, c’est normal sous Windows ; les tests peuvent aussi être lancés dans le navigateur (la fenêtre Karma s’ouvre).

---

## 3. Tester Docker (le plus important)

Il faut avoir **Docker Desktop** (ou Docker + Docker Compose) installé et démarré.

### Étape 1 : Aller à la racine du projet

```powershell
cd C:\Users\lalle\Desktop\JEE_EXAM_P
```
(ou la vraie racine de ton projet)

### Étape 2 : Lancer toute la stack

```powershell
docker-compose up -d --build
```

- `--build` : construit les images (backend + frontend) la première fois.
- `-d` : lance en arrière-plan.

La première fois, ça peut prendre plusieurs minutes (téléchargement des images, build Maven, build Angular).

### Étape 3 : Vérifier que les conteneurs tournent

```powershell
docker-compose ps
```

Tu dois voir 3 services **Up** : `postgres`, `backend`, `frontend`.

### Étape 4 : Tester dans le navigateur

1. **Frontend (interface)**  
   Ouvre : **http://localhost**  
   Tu dois voir la page de connexion du réseau social.

2. **Backend (API)**  
   Ouvre : **http://localhost:8080**  
   Sans token tu peux avoir une page blanche ou une erreur 401 ; c’est normal.  
   Pour vérifier que l’API répond : **http://localhost:8080/auth/login** en POST (avec un outil type Postman) ou simplement que la page frontend se charge et que tu peux t’inscrire / te connecter.

3. **Fonctionnement de l’app**  
   - Créer un compte (Inscription).
   - Se connecter.
   - Vérifier que tu vois la timeline, les groupes, les messages, etc.  
   Si tout ça marche, Docker est bon.

### Étape 5 : Voir les logs (si un service ne marche pas)

```powershell
# Logs de tout
docker-compose logs -f

# Un seul service
docker-compose logs -f backend
docker-compose logs -f frontend
```

`Ctrl+C` pour arrêter l’affichage des logs.

### Étape 6 : Arrêter Docker

```powershell
docker-compose down
```

Pour tout supprimer y compris les données Postgres :

```powershell
docker-compose down -v
```

---

## 4. Résumé rapide

| À tester              | Commande / action                          | Succès si…                          |
|-----------------------|--------------------------------------------|-------------------------------------|
| Tests backend         | `cd backend` puis `mvn test`               | BUILD SUCCESS, tests passés         |
| Tests frontend        | `cd frontend` puis `npm run test`          | 2 tests OK (Karma)                  |
| Docker (stack complète) | `docker-compose up -d --build`          | 3 conteneurs Up                     |
| Site en Docker        | Ouvrir http://localhost                    | Page de connexion + inscription OK |
| API en Docker         | Ouvrir http://localhost:8080              | Réponse (même 401 sans token)      |

Si Docker build échoue (backend ou frontend), envoie le message d’erreur du terminal pour qu’on cible le problème.
