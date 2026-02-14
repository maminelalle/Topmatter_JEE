# Docker, CI/CD et Tests

Ce document décrit les éléments ajoutés pour la valeur pédagogique (Docker, pipeline CI/CD, tests) **sans modifier le comportement de l’application**.

---

## 1. Tests

### Backend (Spring Boot)

- **Profil de test** : `application-test.yml` utilise H2 en mémoire (pas besoin de PostgreSQL pour les tests).
- **Tests unitaires** : `AuthControllerTest` (WebMvcTest) — vérifie que les endpoints `/auth/register` et `/auth/login` renvoient bien un token.
- **Test d’intégration** : `ApplicationContextTest` — vérifie que le contexte Spring se charge avec le profil `test`.

**Lancer les tests :**

```bash
cd backend
mvn test
```

Ou avec le wrapper Windows : `.\mvnw.cmd test`

### Frontend (Angular)

- **Karma + Jasmine** : `karma.conf.js` et `tsconfig.spec.json` configurés.
- **Test minimal** : `app.component.spec.ts` — vérifie que le composant racine se crée et a le bon titre.

**Lancer les tests :**

```bash
cd frontend
npm run test
```

Les tests s’exécutent en mode headless (ChromeHeadless), sans ouvrir de navigateur.

---

## 2. Docker

### Images

- **Backend** : `backend/Dockerfile` — build Maven (JAR) puis image JRE 17 pour exécuter l’application.
- **Frontend** : `frontend/Dockerfile` — build Angular (production) puis nginx pour servir les fichiers statiques.

### Lancer toute la stack

À la racine du projet :

```bash
docker-compose up -d
```

- **PostgreSQL** : port 5432 (base `social_network`).
- **Backend** : http://localhost:8080
- **Frontend** : http://localhost:80 (ou http://localhost)

Le frontend est construit avec l’URL d’API par défaut (`environment.prod.ts` ou build). En local avec Docker, l’API est sur `http://localhost:8080` ; si le front en prod pointe vers `/api`, il faudra éventuellement un reverse proxy (non fourni ici pour rester simple).

### Build manuel des images

```bash
docker build -t social-network-backend:latest ./backend
docker build -t social-network-frontend:latest ./frontend
```

---

## 3. Pipeline CI/CD (GitHub Actions)

Fichier : `.github/workflows/ci.yml`

**Déclenchement :** à chaque push ou pull request sur les branches `main` ou `master`.

**Jobs :**

1. **Backend Tests** : JDK 17, Maven, exécution de `mvn test` dans `backend/`.
2. **Frontend Tests** : Node.js 20, `npm ci` puis `npm run test` (Karma headless) dans `frontend/`.
3. **Docker Build** (uniquement sur push) : build des images Docker backend et frontend après succès des tests.

Aucun déploiement automatique n’est configuré ; le pipeline sert à valider les tests et la construction des images.

---

## 4. Résumé pour le correcteur

| Élément   | Emplacement / Commande |
|----------|-------------------------|
| Tests backend | `backend/src/test/`, `mvn test` |
| Tests frontend | `frontend/src/**/*.spec.ts`, `npm run test` |
| Profil test (H2) | `backend/src/main/resources/application-test.yml` |
| Docker backend | `backend/Dockerfile` |
| Docker frontend | `frontend/Dockerfile` |
| Stack complète | `docker-compose.yml` à la racine |
| CI/CD | `.github/workflows/ci.yml` |

L’application continue de fonctionner comme avant en développement local (backend + frontend + PostgreSQL manuels) ; Docker et la CI sont des ajouts optionnels pour démo et évaluation.
