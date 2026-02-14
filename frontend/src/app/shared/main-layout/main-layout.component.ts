import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { User } from '../../models/user.model';
import { Notification } from '../../models/notification.model';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css'],
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  searchQuery = '';
  notifications: Notification[] = [];
  unreadCount = 0;
  showNotifications = false;
  showUserMenu = false;
  private notificationInterval: ReturnType<typeof setInterval> | null = null;

  constructor(
    private api: ApiService,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.api.getMe().subscribe((u) => (this.currentUser = u));
    this.api.setOnline(true).subscribe();
    this.loadNotifications();
    this.loadUnreadCount();
    this.notificationInterval = setInterval(() => {
      this.loadUnreadCount();
      if (this.showNotifications) this.loadNotifications();
    }, 15000);
  }

  ngOnDestroy(): void {
    if (this.notificationInterval) clearInterval(this.notificationInterval);
  }

  loadNotifications(): void {
    this.api.getNotifications(0, 20).subscribe((list) => (this.notifications = list));
  }

  loadUnreadCount(): void {
    this.api.getUnreadNotificationCount().subscribe((c) => (this.unreadCount = c));
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) this.loadNotifications();
  }

  goTo(path: string): void {
    this.router.navigate([path]);
    this.showNotifications = false;
  }

  isActive(path: string): boolean {
    const url = this.router.url;
    if (path === 'home') return url === '/' || url.startsWith('/home');
    return url.includes(path);
  }

  logout(): void {
    this.api.setOnline(false).subscribe({
      complete: () => {
        this.auth.logout();
        this.router.navigate(['/auth'], { replaceUrl: true });
      },
      error: () => {
        this.auth.logout();
        this.router.navigate(['/auth'], { replaceUrl: true });
      },
    });
  }

  @HostListener('document:click') closeUserMenuOnClick(): void {
    this.showUserMenu = false;
  }
}
