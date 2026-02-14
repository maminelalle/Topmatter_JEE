import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';
import { AuthService } from '../services/auth.service';
import { User } from '../models/user.model';

@Component({
  selector: 'app-friends',
  templateUrl: './friends.component.html',
  styleUrls: ['./friends.component.css'],
})
export class FriendsComponent implements OnInit {
  allUsers: User[] = [];
  friends: User[] = [];
  requests: { id: number; user: User; status: string }[] = [];
  sentRequests: User[] = [];
  loading = true;
  activeTab: 'all' | 'friends' | 'requests' = 'all';
  loadError = '';

  constructor(
    private api: ApiService,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.loading = true;
    this.loadError = '';
    const currentUserId = this.auth.currentUser?.id;

    this.api.getUsersAll(0, 200).subscribe({
      next: (list) => {
        this.allUsers = this.excludeCurrentUser(list || [], currentUserId);
      },
      error: (err) => {
        this.loadError = err.error?.message || 'Impossible de charger la liste des utilisateurs. Vérifiez que vous êtes connecté.';
        this.loading = false;
      },
    });
    this.api.getFriends().subscribe({
      next: (list) => (this.friends = list),
    });
    this.loadRequests();
    this.api.getFriendRequestsSent().subscribe({
      next: (list) => (this.sentRequests = list),
      complete: () => (this.loading = false),
    });
  }

  loadRequests(): void {
    this.api.getFriendRequests().subscribe({
      next: (list) => (this.requests = list || []),
      error: () => (this.requests = []),
    });
  }

  private excludeCurrentUser(users: User[], currentUserId: number | undefined): User[] {
    if (currentUserId == null) return users;
    return users.filter((u) => u.id !== currentUserId);
  }

  friendIds(): Set<number> {
    return new Set(this.friends.map((f) => f.id));
  }

  sentRequestIds(): Set<number> {
    return new Set(this.sentRequests.map((u) => u.id));
  }

  isFriend(userId: number): boolean {
    return this.friendIds().has(userId);
  }

  isSentRequest(userId: number): boolean {
    return this.sentRequestIds().has(userId);
  }

  addFriend(userId: number): void {
    this.api.sendFriendRequest(userId).subscribe({
      next: () => this.loadAll(),
      error: (err) => {
        const status = err.status;
        let msg = err.error?.message || err.error?.error || err.message || 'Erreur lors de l\'envoi de la demande.';
        if (status === 401) msg = 'Session expirée ou non connecté. Reconnectez-vous.';
        if (status === 403) msg = 'Accès refusé. Reconnectez-vous et réessayez.';
        alert(msg);
      },
    });
  }

  cancelRequest(friendId: number): void {
    this.api.cancelFriendRequest(friendId).subscribe({
      next: () => this.loadAll(),
      error: (err) => alert(err.error?.message || 'Erreur'),
    });
  }

  unfriend(friendId: number): void {
    if (!confirm('Retirer cet ami ?')) return;
    this.api.unfriend(friendId).subscribe({
      next: () => this.loadAll(),
    });
  }

  acceptRequest(requestId: number): void {
    this.api.acceptFriendRequest(requestId).subscribe({
      next: () => {
        this.loadAll();
        this.activeTab = 'friends';
      },
    });
  }

  rejectRequest(requestId: number): void {
    this.api.rejectFriendRequest(requestId).subscribe({
      next: () => this.loadAll(),
    });
  }

  message(userId: number): void {
    this.router.navigate(['/messages/chat', userId]);
  }
}
