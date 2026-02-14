# Dépannage - Liste utilisateurs et publications

## Ce qui a été mis en place

1. **Liste des utilisateurs (Amis)**
   - L’app utilise **`GET /users/list-all`** (tous les utilisateurs en BDD). L’API est à la racine : **`http://localhost:8080`** (sans `/api`).
   - **Important :** Ces URLs exigent l’en-tête **`Authorization: Bearer <token>`**. Si vous ouvrez `http://localhost:8080/users/list-all` dans le navigateur sans token → **401**. C’est Angular (connecté) qui appelle avec le token.

2. **Publications (timeline)**
   - Le fil affiche **toutes les publications publiques** (visibilité PUBLIC ou non définie) pour **tout le monde**.
   - Les données sont conservées : `ddl-auto: update` (aucune suppression des tables au redémarrage).

## Si ça ne marche toujours pas

### 1. Vérifier que le backend tourne
- Dans un terminal : `cd backend` puis `.\run-backend.ps1`
- Vous devez voir "Started SocialNetworkApplication" sans erreur.
- PostgreSQL doit être démarré et la base `social_network` existante.

### 2. Vérifier le token (connexion)
- Ouvrez les **Outils de développement** (F12) → onglet **Application** (ou Stockage) → **Local Storage** → `http://localhost:4200`
- Vérifiez que `token` et `user` sont présents.
- Si absents : déconnectez-vous puis reconnectez-vous (ou créez un compte).

### 3. Tester l’API à la main
Dans un terminal PowerShell, récupérez le token dans le navigateur (F12 → Application → Local Storage → `token`), puis :

```powershell
$token = "COLLEZ_VOTRE_TOKEN_ICI"
# Test 1 : liste des utilisateurs
Invoke-RestMethod -Uri "http://localhost:8080/users/list-all?page=0&size=100" -Headers @{ Authorization = "Bearer $token" }
# Test 2 : envoyer une demande d'ami (remplacer 2 par l'id d'un autre user)
Invoke-RestMethod -Uri "http://localhost:8080/friends/request" -Method POST -Headers @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" } -Body '{"friendId":2}'
# Test 3 : créer une publication
Invoke-RestMethod -Uri "http://localhost:8080/posts" -Method POST -Headers @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" } -Body '{"content":"Test publication","visibility":"PUBLIC"}'
```

- Si vous voyez une liste (test 1) ou pas d’erreur (tests 2–3) : le backend et le token fonctionnent. Le souci vient du frontend (CORS, URL, ou token non envoyé).
- Si **401** : le serveur renvoie maintenant « Non authentifié » en JSON. Reconnectez-vous pour obtenir un nouveau token.
- Si **403** : après les corrections, 403 ne devrait plus apparaître pour « pas connecté » ; en cas de 403, noter l’URL exacte et le corps de la réponse.
- Si 404/500 : noter le message d’erreur.

### 4. Vérifier les requêtes dans le navigateur
- F12 → onglet **Réseau (Network)** → recharger la page Amis.
- Cliquer sur la requête vers `users/list` ou `users/all`.
- Regarder **Statut** (200, 401, 404, 500) et l’onglet **Réponse** (message d’erreur éventuel).

### 5. Redémarrer proprement
1. Arrêter le backend (Ctrl+C dans le terminal).
2. Arrêter le frontend (Ctrl+C).
3. Relancer le backend : `cd backend` puis `.\run-backend.ps1`.
4. Relancer le frontend : `cd frontend` puis `npm start`.
5. Se reconnecter sur l’app (page Connexion).
6. Retester la page Amis et l’accueil (publications).

### 6. Vérifier la base de données
- Dans pgAdmin ou psql : base `social_network`, tables `users` et `posts`.
- Vérifier qu’il y a bien plusieurs lignes dans `users` et des lignes dans `posts` (pour les publications).

---

En cas d’erreur persistante, noter :
- le **code HTTP** (ex. 401, 500),
- le **message** renvoyé par l’API (onglet Réponse ou console),
- et éventuellement une capture d’écran de l’onglet Réseau.
