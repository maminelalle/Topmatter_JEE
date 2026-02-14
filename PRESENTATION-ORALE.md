# Présentation orale – Mini réseau social (JEE)

**Projet :** JEE_EXAM_P  
**Équipe :** 5 personnes  
**Public :** Professeurs  
**Objectif :** Présenter le projet (contexte, architecture, démo, techniques, déploiement) en répartissant la parole entre les 5 membres.

---

## Répartition des rôles (5 personnes)

Chaque personne prend en charge **une partie** de la présentation + une courte démo si indiqué. Prévoir environ **2–3 minutes par personne** (total ~12–15 min), puis questions.

| # | Rôle | Contenu à présenter | Qui |
|---|------|---------------------|-----|
| **1** | Intro & contexte | Sujet du projet, objectifs, fonctionnalités principales | Personne 1 |
| **2** | Architecture & stack technique | Backend / Frontend / BDD, technologies, structure des dossiers | Personne 2 |
| **3** | Base de données & API | Tables, relations, sécurité (JWT), principaux endpoints | Personne 3 |
| **4** | Démo & fonctionnalités | Démo live : inscription, post, commentaire, amis, messages, groupes | Personne 4 |
| **5** | Qualité & déploiement | Tests, Docker, CI/CD, conclusion | Personne 5 |

---

## Script détaillé par personne

### Personne 1 – Introduction et contexte (2–3 min)

**À dire :**

- Bonjour, nous sommes le groupe [X]. Nous allons vous présenter notre projet de **mini réseau social** réalisé dans le cadre du cours JEE.
- **Sujet :** Une application web type réseau social avec un backend Java (Spring Boot) et un frontend Angular.
- **Objectifs :** Permettre aux utilisateurs de s’inscrire, se connecter, publier des posts (texte et image), commenter, liker, gérer des amis, échanger en messages privés et dans des groupes avec un chat de groupe.
- **Fonctionnalités principales :**
  - Authentification (inscription / connexion sécurisée par token JWT).
  - Fil d’actualité avec publications et photos.
  - Commentaires et likes sur les posts.
  - Système d’amis (demandes, acceptation, refus).
  - Messagerie privée entre amis.
  - Groupes : création, ajout de membres, publications et chat de groupe.
  - Notifications (like, commentaire, ami, message).
- Nous allons enchaîner avec l’architecture technique puis une démo et enfin les aspects qualité et déploiement.

**Transition :** “Pour détailler comment tout cela est construit, [Personne 2] va vous présenter l’architecture et la stack technique.”

---

### Personne 2 – Architecture et stack technique (2–3 min)

**À dire :**

- L’application est découpée en **trois parties** : un backend, un frontend et une base de données.
- **Backend :** Développé en **Java 17** avec **Spring Boot 3.2**. Il expose une **API REST** (JSON) et gère l’authentification par **JWT**, la persistance avec **Spring Data JPA** et les **WebSockets** (STOMP) pour les notifications et le temps réel. Les principaux packages sont : `controller` (endpoints REST), `service` (logique métier), `repository` (accès données), `model` (entités JPA), `security` (JWT, filtres), `config` (sécurité, CORS, WebSocket).
- **Frontend :** Développé en **Angular 16** (TypeScript). C’est une **SPA** : une seule page HTML, la navigation se fait côté client. On utilise le **router** pour les différentes vues (accueil, amis, messages, groupes), un **guard** pour protéger les routes (redirection si non connecté), et un **intercepteur HTTP** pour ajouter le token JWT à chaque requête. Les appels API sont centralisés dans un service, et le style est géré avec **Tailwind CSS**.
- **Base de données :** **PostgreSQL** en développement et en production. Les tables sont gérées par **Hibernate** (JPA) avec `ddl-auto: update`. Pour les tests backend, nous utilisons **H2** en mémoire avec un profil Spring dédié.

**Transition :** “[Personne 3] va détailler la base de données et l’API, ainsi que la sécurité.”

---

### Personne 3 – Base de données et API (2–3 min)

**À dire :**

- **Base de données :** Nous avons une dizaine de tables principales. Les plus importantes sont : **users** (comptes, rôles), **posts** (contenu, image, auteur, visibilité), **comments** (liés à un post et éventuellement à un parent pour les réponses), **likes** (un like par utilisateur et par post), **friends** (liens d’amitié avec statut : en attente, accepté, refusé), **messages** (messages privés entre deux utilisateurs), **notifications** (types : like, commentaire, ami, message), **app_groups**, **group_members** et **group_messages** pour les groupes et le chat de groupe. Les relations sont en ManyToOne / OneToMany classiques (utilisateur → posts, post → commentaires, etc.).
- **Sécurité :** L’API est protégée par **JWT**. Après login ou register, le serveur renvoie un token ; le frontend le stocke et l’envoie dans l’en-tête `Authorization` à chaque requête. Les seuls chemins publics sont `/auth/**` (login, register) et `/uploads/**` (images). Un filtre Spring lit le token, le valide et charge l’utilisateur dans le contexte de sécurité.
- **API :** Les endpoints principaux sont regroupés par ressource : `/auth`, `/users`, `/posts` (dont upload d’image), `/comments`, `/likes`, `/friends`, `/messages`, `/groups` (et sous-chemins pour membres, posts, messages), `/notifications`. Le détail est dans le rapport technique.

**Transition :** “Pour voir l’application en action, [Personne 4] va faire une démonstration.”

---

### Personne 4 – Démo et fonctionnalités (3–4 min)

**À faire (démo live) :**

- Ouvrir l’application (http://localhost ou celle configurée).
- **Inscription / Connexion :** Montrer la page d’auth, créer un compte ou se connecter.
- **Fil d’actualité :** Afficher la home, créer un post avec du texte et éventuellement une image (upload).
- **Commentaires et likes :** Ajouter un commentaire sur un post, liker / unliker.
- **Amis :** Aller sur la page Amis, rechercher un utilisateur (ou en afficher un), envoyer une demande d’ami. Si possible, accepter une demande (avec un second compte ou un compte déjà préparé).
- **Messages :** Ouvrir Messages, choisir une conversation (ou en démarrer une), envoyer un message.
- **Groupes :** Ouvrir Groupes, créer un groupe, ajouter un membre, afficher le fil du groupe et le chat de groupe, envoyer un message dans le groupe.

**À dire (en parallèle de la démo) :**

- “On se connecte ici… Le fil d’actualité affiche les posts des amis et les miens. Je peux publier du texte et une image.”
- “Les commentaires sont enregistrés en base et affichés sous chaque post ; le like est un simple toggle.”
- “La gestion d’amis permet d’envoyer des demandes et de les accepter ou refuser.”
- “Les messages privés sont persistés ; on peut voir l’historique et envoyer de nouveaux messages.”
- “Les groupes permettent de regrouper des amis et d’avoir un fil et un chat dédiés.”

**Transition :** “Enfin, [Personne 5] va parler des tests, du déploiement Docker et de la CI/CD.”

---

### Personne 5 – Qualité, Docker, CI/CD et conclusion (2–3 min)

**À dire :**

- **Tests :** Côté backend, nous avons des tests avec **JUnit 5** et **Spring Boot Test** : un test de chargement du contexte (ApplicationContextTest) et des tests sur le contrôleur d’auth (AuthControllerTest) pour vérifier que l’inscription et le login renvoient bien un token. Le profil de test utilise **H2** pour ne pas dépendre de PostgreSQL. Côté frontend, nous avons configuré **Karma** et **Jasmine** avec un test minimal sur le composant racine. Les commandes sont `mvn test` dans le backend et `npm run test` dans le frontend.
- **Docker :** Nous avons conteneurisé l’application pour une démo ou un déploiement simple. Un **docker-compose** à la racine lance trois services : **PostgreSQL**, le **backend** (JAR Spring Boot) et le **frontend** (Angular build servi par nginx). Une seule commande, `docker-compose up -d --build`, permet de lancer toute la stack ; l’application est alors accessible sur http://localhost et l’API sur le port 8080.
- **CI/CD :** Nous avons un pipeline **GitHub Actions** qui, à chaque push (ou pull request), exécute les tests backend et frontend, puis build les images Docker. Cela permet de valider que le projet compile et que les tests passent avant toute fusion.
- **Conclusion :** Nous avons réalisé un mini réseau social complet (auth, posts, commentaires, likes, amis, messages, groupes, notifications, photos), avec une architecture claire (backend Spring Boot, frontend Angular, PostgreSQL), des tests, du Docker et une CI pour la qualité. Nous restons disponibles pour vos questions.

---

## Ordre de passage recommandé

1. **Personne 1** – Intro & contexte  
2. **Personne 2** – Architecture & stack  
3. **Personne 3** – BDD & API  
4. **Personne 4** – Démo  
5. **Personne 5** – Tests, Docker, CI/CD, conclusion  

---

## Conseils pour la présentation

- **Préparer la démo :** Lancer l’app (ou Docker) avant le passage ; avoir 2 comptes si vous voulez montrer acceptation d’ami et conversation.
- **Prévoir un plan B :** Si la démo échoue (réseau, machine), utiliser des captures d’écran ou le rapport technique pour décrire les écrans.
- **Répartition du temps :** Chronométrer chaque partie pour rester dans les 2–4 min par personne.
- **Questions possibles :** Qui a fait quoi (backend/frontend/BDD) ; pourquoi JWT ; différence WebSocket vs REST ; comment sont gérés les droits (suppression de post, accès aux messages), etc. Prévoir des réponses courtes en équipe.

---

## Résumé une page (à distribuer ou afficher)

- **Projet :** Mini réseau social (JEE).
- **Stack :** Backend Spring Boot 3.2 (Java 17), Frontend Angular 16, PostgreSQL.
- **Fonctionnalités :** Auth JWT, posts (texte + image), commentaires, likes, amis, messages privés, groupes + chat de groupe, notifications.
- **Qualité :** Tests backend (JUnit, H2) et frontend (Karma/Jasmine), Docker Compose, CI GitHub Actions.
- **Docs :** `RAPPORT-TECHNIQUE.md` (rapport complet), `PRESENTATION-ORALE.md` (ce fichier).

Vous pouvez utiliser ce fichier comme **trame de présentation** et le découper en 5 parties pour les 5 membres de l’équipe. Chacun adapte les phrases à son style tout en gardant le fond technique décrit ici.
