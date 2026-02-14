# Rapport technique – Mini réseau social (JEE)

**Projet :** JEE_EXAM_P  
**Application :** Réseau social (posts, amis, messages privés, groupes, commentaires, likes, notifications, photos)  
**Stack :** Backend Spring Boot 3.2 + Frontend Angular 16 + PostgreSQL  

---

## 1. Vue d’ensemble du projet

L’application est un mini réseau social permettant :
- **Authentification** : inscription, connexion (JWT)
- **Publications** : créer, modifier, supprimer des posts avec texte et image
- **Commentaires** : commenter les posts (avec réponses)
- **Likes** : aimer un post (toggle)
- **Amis** : demandes d’amis, accepter/refuser, liste d’amis
- **Messages privés** : conversations 1-à-1, persistance en base + WebSocket pour notifications
- **Groupes** : créer des groupes, ajouter des membres, publications de groupe, chat de groupe
- **Notifications** : like, commentaire, demande d’ami, message (en base + push WebSocket)
- **Photos** : upload d’images sur les publications (stockage local, max 5 Mo)

L’architecture est **client–serveur** : un backend REST (Spring Boot) et un frontend SPA (Angular) qui communiquent en HTTP/JSON ; les notifications et le chat utilisent **WebSocket** (STOMP over SockJS).

---

## 2. Outils et technologies utilisés

### 2.1 Backend (Java / Spring)

| Technologie | Version | Rôle |
|-------------|---------|------|
| Java | 17 | Langage |
| Spring Boot | 3.2.0 | Framework applicatif |
| Spring Web | - | REST API |
| Spring Data JPA | - | Persistance, repositories |
| Spring Security | - | Authentification, autorisation, JWT |
| Spring WebSocket | - | STOMP, notifications temps réel |
| PostgreSQL (driver) | - | Base de données |
| H2 | - | Base de test (profil `test`) |
| JJWT | 0.12.3 | Génération et validation des tokens JWT |
| Lombok | 1.18.38 | Réduction du boilerplate (getters, builders, etc.) |
| Maven | 3.9+ | Build, dépendances |

### 2.2 Frontend (Angular)

| Technologie | Version | Rôle |
|-------------|---------|------|
| Angular | 16.2 | Framework SPA |
| Angular Router | - | Routes, gardes |
| Angular Forms | - | Formulaires (login, register, posts, etc.) |
| RxJS | 7.8 | Programmation réactive, appels API |
| HttpClient | - | Appels REST |
| Tailwind CSS | 3.4 | Styles (utility-first) |
| TypeScript | 5.1 | Langage |
| Karma + Jasmine | - | Tests unitaires (frontend) |

### 2.3 Base de données et déploiement

| Outil | Rôle |
|-------|------|
| PostgreSQL 15 | Base de données principale (données utilisateurs, posts, messages, etc.) |
| H2 | Base en mémoire pour les tests backend (profil `test`) |
| Docker / Docker Compose | Conteneurisation backend, frontend, PostgreSQL |
| GitHub Actions | CI/CD : tests backend/frontend, build des images Docker |

---

## 3. Architecture et rôles des fichiers

### 3.1 Arborescence backend (`backend/`)

```
backend/
├── pom.xml                          # Dépendances Maven, plugins (surefire, compiler)
├── Dockerfile                        # Image Docker : build Maven → JAR → JRE 17 Alpine
├── src/main/java/com/app/
│   ├── SocialNetworkApplication.java # Point d’entrée Spring Boot
│   ├── config/                      # Configuration
│   ├── controller/                  # Contrôleurs REST
│   ├── dto/                         # Objets de transfert (requêtes / réponses)
│   ├── model/                      # Entités JPA
│   ├── repository/                 # Spring Data JPA repositories
│   ├── service/                     # Logique métier
│   ├── security/                    # JWT, UserDetails, filtre d’authentification
│   ├── mapper/                      # Conversion entité ↔ DTO
│   └── websocket/                   # Services et écouteurs WebSocket
└── src/main/resources/
    ├── application.yml              # Config globale (DB, JWT, upload, logging)
    ├── application-dev.yml          # Profil dev (optionnel)
    └── application-test.yml        # Profil test (H2, pas de WebSocket)
```

### 3.2 Rôle des principaux fichiers backend

| Fichier | Rôle |
|---------|------|
| **SocialNetworkApplication** | Classe principale `@SpringBootApplication` ; lance le contexte Spring. |
| **config/SecurityConfig** | Règles de sécurité : chemins publics (`/auth/**`, `/uploads/**`), authentification JWT sur le reste, CORS, désactivation CSRF pour API stateless. |
| **config/CorsConfig** | CORS : origines, méthodes, en-têtes autorisés pour le frontend. |
| **config/WebMvcConfig** | Enregistrement du dossier d’upload (`uploads`) comme ressource statique (URL `/uploads/**`). |
| **config/WebSocketConfig** | Configuration STOMP : broker `/topic`, destination `/app` ; enregistrement des endpoints WebSocket. |
| **config/UserIdAttributeInterceptor** | Intercepteur STOMP : récupère l’utilisateur depuis le JWT et l’attache à la session. |
| **config/GlobalExceptionHandler** | `@ControllerAdvice` : gestion centralisée des exceptions (validation, 404, etc.) et réponses HTTP cohérentes. |
| **config/StartupRunner** | Optionnel : tâche au démarrage (ex. création du dossier uploads si besoin). |
| **security/JwtUtil** | Génération et parsing des JWT (username, expiration, signature). |
| **security/JwtAuthenticationFilter** | Filtre HTTP : extrait le JWT de l’en-tête `Authorization`, valide le token et charge `UserDetails` dans le `SecurityContext`. |
| **security/UserPrincipal** | Implémentation de `UserDetails` (id, username, password, authorities). |
| **security/CustomUserDetailsService** | `UserDetailsService` : charge un `User` par username depuis la BDD pour la validation du mot de passe. |
| **controller/AuthController** | `POST /auth/register`, `POST /auth/login` ; délègue à `AuthService`, retourne un token JWT. |
| **controller/UserController** | `GET /users/me`, `GET /users/{id}`, `GET /users/search`, `GET /users/list`, `GET /users/list-all` ; mise à jour statut en ligne. |
| **controller/PostController** | CRUD posts : timeline, search, upload image, get/ create/ update/ delete. |
| **controller/CommentController** | `GET /comments/post/{postId}`, `POST /comments`, `DELETE /comments/{id}`. |
| **controller/LikeController** | `POST /likes/post/{postId}/toggle`. |
| **controller/FriendController** | Liste amis, demandes reçues/envoyées, envoyer/accepter/refuser/cancel/unfriend. |
| **controller/MessageController** | Conversations, messages d’une conversation, envoi message. |
| **controller/GroupController** | CRUD groupes, membres, posts du groupe, messages du groupe (chat groupe). |
| **controller/NotificationController** | Liste notifications, nombre non lues, marquer lues. |
| **service/AuthService** | Inscription (hash BCrypt), login (vérification mot de passe, génération JWT). |
| **service/UserService** | Logique utilisateur : recherche, profil, mise à jour en ligne. |
| **service/PostService** | Logique posts : timeline (amis + soi), recherche, création avec image, droits. |
| **service/CommentService** | Ajout/suppression commentaires, chargement par post. |
| **service/LikeService** | Toggle like (création/suppression), comptage. |
| **service/FriendService** | Demandes d’amis, acceptation, refus, liste amis. |
| **service/MessageService** | Envoi/réception messages privés, liste conversations. |
| **service/GroupService** | Création groupe, ajout/sortie membres, posts et messages de groupe. |
| **service/GroupMessageService** | Envoi et récupération des messages d’un groupe. |
| **service/FileStorageService** | Stockage des fichiers uploadés (images) : validation type/taille, nom unique, chemin `app.upload-dir`. |
| **service/NotificationService** | Création notifications (like, comment, friend, message), marquage lu. |
| **websocket/WebSocketNotificationService** | Envoi des notifications en temps réel via STOMP (`/topic/notifications/{userId}`). |
| **websocket/WebSocketMessageService** | Envoi des messages privés en temps réel (notification nouveau message). |
| **websocket/WebSocketEventListener** | Détection connexion/déconnexion WebSocket pour mise à jour statut en ligne. |
| **repository/*Repository** | Interfaces Spring Data JPA (CRUD + requêtes personnalisées) pour chaque entité. |
| **mapper/DtoMapper** | Conversion `Entity` ↔ `DTO` (User, Post, Comment, Message, Group, etc.). |

### 3.3 Arborescence frontend (`frontend/`)

```
frontend/
├── package.json, angular.json, tsconfig.json, karma.conf.js
├── Dockerfile                        # Build Angular → nginx (fichiers statiques)
├── nginx.conf                        # Serveur SPA + proxy /api et /ws vers backend
├── src/
│   ├── index.html
│   ├── main.ts
│   ├── environments/
│   │   ├── environment.ts            # apiUrl / wsUrl en dev (localhost:8080)
│   │   └── environment.prod.ts      # apiUrl / wsUrl en prod
│   └── app/
│       ├── app.module.ts
│       ├── app.component.ts
│       ├── app-routing.module.ts     # Routes + AuthGuard
│       ├── guards/auth.guard.ts      # Redirection vers /auth si non connecté
│       ├── interceptors/auth.interceptor.ts  # Ajout du JWT dans les requêtes HTTP
│       ├── services/
│       │   ├── api.service.ts        # Tous les appels REST
│       │   └── auth.service.ts       # Login, register, stockage token, déconnexion
│       ├── models/                   # Interfaces TypeScript (User, Post, Comment, etc.)
│       ├── auth/auth.component.ts    # Page login / register
│       ├── shared/main-layout/       # Layout avec menu (home, amis, messages, groupes)
│       ├── posts/home/               # Fil d’actualité, formulaire post, liste posts
│       ├── posts/post-card/          # Affichage d’un post (texte, image, likes, commentaires)
│       ├── posts/comment-list/       # Liste commentaires + formulaire ajout
│       ├── friends/friends.component # Liste amis, demandes, recherche, ajout
│       ├── messages/messages.component # Liste conversations
│       ├── messages/chat/            # Chat avec un ami (messages + envoi)
│       └── groups/groups.component   # Liste groupes, création, membres, chat groupe
```

### 3.4 Rôle des principaux fichiers frontend

| Fichier | Rôle |
|---------|------|
| **app-routing.module** | Routes : `/auth` (login/register), `/home`, `/friends`, `/messages`, `/messages/chat/:userId`, `/groups` ; `AuthGuard` sur les routes protégées. |
| **auth.guard** | Vérifie la présence du token (ex. `localStorage`) ; sinon redirige vers `/auth`. |
| **auth.interceptor** | Intercepte les requêtes HTTP et ajoute l’en-tête `Authorization: Bearer <token>`. |
| **auth.service** | `login()`, `register()`, `logout()`, stockage/suppression du token, état “connecté”. |
| **api.service** | Centralise tous les appels REST (users, posts, comments, likes, friends, messages, groups, notifications). |
| **auth.component** | Formulaire unique avec onglet login/register ; appelle AuthService puis redirige. |
| **main-layout** | Barre de navigation, lien déconnexion, affichage des enfants (router-outlet). |
| **home.component** | Fil d’actualité (timeline), formulaire de post (texte + image), liste des posts (post-card). |
| **post-card** | Affichage d’un post (auteur, contenu, image), bouton like, comment-list. |
| **comment-list** | Liste des commentaires d’un post, formulaire pour ajouter un commentaire. |
| **friends.component** | Liste amis, demandes reçues/envoyées, recherche utilisateurs, envoi/acceptation/refus. |
| **messages.component** | Liste des conversations (amis avec qui on a échangé). |
| **chat.component** | Messages avec un utilisateur donné ; envoi et affichage (rafraîchissement ou WebSocket si branché). |
| **groups.component** | Liste des groupes de l’utilisateur, création groupe, ajout membres, fil du groupe, chat de groupe. |

---

## 4. Base de données

### 4.1 Système de gestion

- **Production / développement :** PostgreSQL (base `social_network`, utilisateur `postgres`).
- **Tests backend :** H2 en mémoire (profil Spring `test`, `application-test.yml`).

Configuration type (extrait `application.yml`) :

- URL : `jdbc:postgresql://localhost:5432/social_network`
- Utilisateur / mot de passe : `postgres` / `postgres`
- JPA : `ddl-auto: update` (mise à jour du schéma au démarrage sans recréer les données).

### 4.2 Tables et entités

Les tables sont générées par Hibernate à partir des entités JPA (`ddl-auto: update`).

| Table | Entité | Description |
|-------|--------|-------------|
| **users** | User | Utilisateurs : id, username (unique), email (unique), password (BCrypt), avatar_url, bio, is_online, last_seen, role (USER/ADMIN), created_at. |
| **posts** | Post | Publications : id, content, image_url, author_id (FK users), visibility (PUBLIC/GROUP/PRIVATE), group_id (optionnel), created_at, updated_at. |
| **comments** | Comment | Commentaires : id, content, user_id (FK users), post_id (FK posts), parent_id (FK comments, pour réponses), created_at. |
| **likes** | Like | Likes : id, user_id (FK users), post_id (FK posts), created_at. Contrainte unique (user_id, post_id). |
| **friends** | Friend | Liens d’amitié : id, user_id, friend_id (FK users), status (PENDING/ACCEPTED/REJECTED), created_at. Contrainte unique (user_id, friend_id). |
| **messages** | Message | Messages privés : id, content, sender_id, receiver_id (FK users), is_read, created_at. |
| **notifications** | Notification | Notifications : id, type (LIKE/COMMENT/FRIEND_REQUEST/FRIEND_ACCEPTED/MESSAGE), message, user_id (FK users), actor_id, post_id, is_read, created_at. |
| **app_groups** | Group | Groupes : id, name, description, created_by_id (FK users), created_at. |
| **group_members** | GroupMember | Appartenance au groupe : id, group_id (FK app_groups), user_id (FK users), role (ADMIN/MEMBER), joined_at. Contrainte unique (group_id, user_id). |
| **group_messages** | GroupMessage | Messages de groupe : id, content, group_id (FK app_groups), sender_id (FK users), created_at. |

### 4.3 Relations principales (résumé)

- **User** : OneToMany vers Post, Comment, Like, Message (sent/received), Notification, Group (createdBy), GroupMember, GroupMessage.
- **Post** : ManyToOne User (author) ; OneToMany Comment, Like.
- **Comment** : ManyToOne User, Post, Comment (parent) ; OneToMany Comment (replies).
- **Like** : ManyToOne User, Post.
- **Friend** : ManyToOne User (user, friend).
- **Message** : ManyToOne User (sender, receiver).
- **Notification** : ManyToOne User.
- **Group** : ManyToOne User (createdBy) ; OneToMany GroupMember.
- **GroupMember** : ManyToOne Group, User.
- **GroupMessage** : ManyToOne Group, User (sender).

---

## 5. API REST (résumé)

Base URL : `http://localhost:8080` (ou celle configurée en prod). Toutes les routes sauf `/auth/**` et `/uploads/**` nécessitent un JWT dans l’en-tête `Authorization: Bearer <token>`.

| Méthode | Chemin | Description |
|---------|--------|-------------|
| POST | /auth/register | Inscription |
| POST | /auth/login | Connexion (retourne token) |
| GET | /users/me | Profil connecté |
| GET | /users/{id} | Profil d’un utilisateur |
| GET | /users/search?q= | Recherche utilisateurs |
| GET | /posts/timeline | Fil d’actualité (paginé) |
| GET | /posts/search?q= | Recherche de posts |
| POST | /posts/upload-image | Upload image (multipart) |
| GET/POST/PUT/DELETE | /posts, /posts/{id} | CRUD posts |
| GET | /comments/post/{postId} | Commentaires d’un post |
| POST | /comments | Ajouter commentaire |
| DELETE | /comments/{id} | Supprimer commentaire |
| POST | /likes/post/{postId}/toggle | Like / unlike |
| GET | /friends | Liste amis |
| GET | /friends/requests | Demandes reçues |
| POST | /friends/request | Envoyer demande (body: friendId) |
| POST | /friends/requests/{id}/accept | Accepter |
| POST | /friends/requests/{id}/reject | Refuser |
| DELETE | /friends/{friendId} | Retirer ami |
| GET | /messages/conversations | Liste conversations |
| GET | /messages/conversation/{otherUserId} | Messages avec un utilisateur |
| POST | /messages | Envoyer message (body: receiverId, content) |
| GET/POST | /groups, /groups/{id} | Liste / détail / création groupe |
| POST | /groups/{id}/members | Ajouter membre (body: userId) |
| GET | /groups/{id}/posts | Posts du groupe |
| GET | /groups/{id}/messages | Messages du groupe (chat) |
| POST | /groups/{id}/messages | Envoyer message groupe |
| GET | /notifications | Liste notifications |
| GET | /notifications/unread-count | Nombre non lues |
| PATCH | /notifications/{id}/read | Marquer lue |

---

## 6. Fonctionnement des principales fonctionnalités

### 6.1 Authentification

1. **Inscription** : le front envoie username, email, password à `POST /auth/register`. Le backend hash le mot de passe (BCrypt), crée l’utilisateur en BDD, génère un JWT et le renvoie.
2. **Connexion** : `POST /auth/login` avec username et password ; vérification via `UserDetailsService`, génération JWT.
3. Le front stocke le token (ex. `localStorage`) et l’envoie dans `Authorization` pour toutes les requêtes (via `AuthInterceptor`). Le `JwtAuthenticationFilter` valide le token et charge le `UserDetails` dans le `SecurityContext`.

### 6.2 Publications et commentaires

- **Timeline** : `PostService` récupère les posts des amis + les siens, triés par date, avec pagination.
- **Création** : formulaire (texte + optionnellement image). L’image est uploadée via `POST /posts/upload-image`, puis l’URL retournée est associée au post dans `POST /posts`.
- **Commentaires** : stockés en BDD ; après ajout, le front recharge la liste des commentaires (et optionnellement une notification est créée et envoyée en WebSocket).

### 6.3 Amis

- Demande : création d’un enregistrement `Friend` avec status PENDING.
- Acceptation / refus : mise à jour du statut ; en cas d’acceptation, l’autre utilisateur peut apparaître dans la liste d’amis et dans les conversations.

### 6.4 Messages privés et chat de groupe

- **Messages privés** : enregistrés en BDD via `MessageService` ; le front peut afficher l’historique et envoyer de nouveaux messages ; un service WebSocket peut notifier le destinataire en temps réel.
- **Chat de groupe** : `GroupMessage` stocké en BDD ; endpoints `GET/POST /groups/{id}/messages` ; le front affiche les messages et les rafraîchit (ex. polling ou WebSocket si branché).

### 6.5 Notifications

- Création en BDD (NotificationService) lors d’un like, commentaire, demande d’ami, acceptation, message.
- Envoi en temps réel via STOMP (`WebSocketNotificationService`) sur `/topic/notifications/{userId}` pour que le client mette à jour le badge ou la liste sans recharger.

### 6.6 Stockage des images

- **FileStorageService** : reçoit le fichier, vérifie type (JPEG, PNG, GIF, WebP) et taille (max 5 Mo), enregistre dans `app.upload-dir` (ex. `./uploads`) avec un nom unique.
- Les fichiers sont servis par Spring (ressource statique) sous `/uploads/**` ; en production Docker, le dossier est créé dans l’image avec les bons droits (voir Dockerfile backend).

---

## 7. Tests, Docker et CI/CD

### 7.1 Tests backend

- **Profil** : `application-test.yml` (H2, pas de WebSocket si désactivé en test).
- **ApplicationContextTest** : charge le contexte Spring avec le profil `test`.
- **AuthControllerTest** : `@WebMvcTest` sur `AuthController` ; mock de `AuthService`, `JwtUtil`, etc. ; vérification que `POST /auth/register` et `POST /auth/login` renvoient un token.

Commande : `mvn test` (ou `.\mvnw.cmd test`) dans `backend/`.

### 7.2 Tests frontend

- Karma + Jasmine ; test minimal dans `app.component.spec.ts`.
- Commande : `npm run test` dans `frontend/`.

### 7.3 Docker

- **docker-compose.yml** (racine) : services `postgres`, `backend`, `frontend`.
- **backend/Dockerfile** : stage build Maven (JAR), stage run JRE 17 Alpine ; création du répertoire `uploads` et droits pour `appuser`.
- **frontend/Dockerfile** : build Angular, puis nginx pour servir les fichiers statiques.
- **frontend/nginx.conf** : SPA + proxy vers l’API backend (selon config).

Commandes : `docker-compose up -d --build` pour lancer toute la stack ; frontend sur http://localhost, API sur http://localhost:8080.

### 7.4 CI/CD (GitHub Actions)

- Fichier : `.github/workflows/ci.yml`.
- Jobs : tests backend (Maven), tests frontend (npm), build des images Docker (sur push).
- Aucun déploiement automatique ; validation des tests et de la construction des images.

---

## 8. Configuration

### 8.1 Backend (application.yml)

- **Base de données** : URL, username, password PostgreSQL (ou variables d’environnement en Docker).
- **JPA** : `ddl-auto: update`, dialect PostgreSQL.
- **Multipart** : max 5 Mo pour les uploads.
- **JWT** : secret et expiration (ex. 24 h) ; optionnellement `JWT_SECRET` en env.
- **Upload** : `app.upload-dir` (défaut `./uploads`) ; en Docker, `/app/uploads` avec droits `appuser`.

### 8.2 Frontend (environments)

- **environment.ts** (dev) : `apiUrl` et `wsUrl` vers `http://localhost:8080`.
- **environment.prod.ts** : URLs de production (ex. même origine ou domaine de l’API).

---

## 9. Résumé pour le correcteur

| Élément | Emplacement / Commande |
|--------|-------------------------|
| Rapport technique | Ce fichier `RAPPORT-TECHNIQUE.md` |
| Présentation orale | `PRESENTATION-ORALE.md` |
| Backend | `backend/` (Spring Boot 3.2, Java 17) |
| Frontend | `frontend/` (Angular 16) |
| Base de données | PostgreSQL (tables générées par JPA) |
| Tests backend | `backend/src/test/`, `mvn test` |
| Tests frontend | `frontend/src/**/*.spec.ts`, `npm run test` |
| Docker | `docker-compose.yml`, `backend/Dockerfile`, `frontend/Dockerfile` |
| CI/CD | `.github/workflows/ci.yml` |

Ce rapport décrit les données, les rôles des fichiers, le fonctionnement des fonctionnalités, les outils utilisés et la base de données du projet. Pour la répartition orale entre les 5 membres du groupe, voir **PRESENTATION-ORALE.md**.
