# Mini réseau social - JEE Exam

Application full-stack : **Angular 16** (frontend) + **Spring Boot 3** (backend) avec JWT, WebSocket, PostgreSQL.

## Prérequis

- **Node.js** 18+ et **npm**
- **Java 17+** (Maven n'est pas nécessaire, le projet inclut le Maven Wrapper)
- **PostgreSQL** (base `social_network`, utilisateur/mot de passe : `postgres`/`postgres` par défaut)

## Base de données

Créer la base et l’utilisateur si besoin :

```sql
CREATE DATABASE social_network;
-- Utilisateur postgres / postgres par défaut (voir application.yml)
```

Modifier `backend/src/main/resources/application.yml` si votre configuration PostgreSQL est différente.

## Lancer le backend (Spring Boot)

### Option 1 : Avec le Maven Wrapper (recommandé - pas besoin de Maven installé)

**Windows (PowerShell) :**
```powershell
cd backend
.\run-backend.ps1
```

**Windows (CMD) :**
```cmd
cd backend
run-backend.bat
```

**Ou directement avec le wrapper :**
```cmd
cd backend
.\mvnw.cmd spring-boot:run
```

### Option 2 : Avec Maven installé (si vous avez Maven dans votre PATH)

```bash
cd backend
mvn spring-boot:run
```

> **Note** : Le Maven Wrapper téléchargera automatiquement Maven lors de la première utilisation si nécessaire.

API : **http://localhost:8080/api**

- **Inscription** : `POST /api/auth/register`  
  Body : `{ "username", "email", "password" }`
- **Connexion** : `POST /api/auth/login`  
  Body : `{ "email", "password" }`  
  Réponse : `{ "token", "id", "username", "email", "role" }`

Toutes les autres routes (sauf `/auth/**` et `/ws/**`) nécessitent l’en-tête :  
`Authorization: Bearer <token>`.

## Lancer le frontend (Angular)

```bash
cd frontend
npm install
npm start
```

Ouvrir **http://localhost:4200**.

- Page **Auth** : inscription / connexion (formulaires côte à côte).
- Après connexion : **Fil d’actualité** (création de posts, likes, commentaires), **Amis** (demandes, liste, retirer, message), **Messages** (liste des amis, chat en temps réel), **Notifications** (dropdown dans la barre).

## Fonctionnalités

- **Auth** : Inscription / Connexion / Déconnexion, JWT, mots de passe hashés (BCrypt).
- **Publications** : Création, modification, suppression, fil d’actualité (timeline), image optionnelle.
- **Interactions** : Commentaires, likes, gestion d’amis (demandes, accepter/refuser, retirer).
- **Messagerie** : Envoi de messages, conversation par ami, WebSocket pour temps réel.
- **Notifications** : Likes, commentaires, demandes d’amis, messages ; WebSocket pour mise à jour en temps réel.
- **Recherche** : Utilisateurs et publications (API prêtes, à brancher sur la barre de recherche).
- **UI** : Tailwind CSS, palette bleu/gris/blanc, typo Poppins, design responsive, chat en thème sombre.

## Structure

- **Backend** : `backend/` — Spring Boot 3, Spring Security + JWT, Spring Data JPA, WebSocket (STOMP), PostgreSQL.
- **Frontend** : `frontend/` — Angular 16, Tailwind, modules Auth, Posts, Friends, Messages, Notifications.

## Variables d’environnement (optionnel)

- **Backend** : `JWT_SECRET` pour la clé de signature JWT (sinon valeur par défaut dans `application.yml`).
- **Frontend** : `environment.apiUrl` pour l’URL de l’API (défaut : `http://localhost:8080/api`).
