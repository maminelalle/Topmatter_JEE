import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Post } from '../../models/post.model';
import { Group } from '../../models/group.model';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  posts: Post[] = [];
  newPostContent = '';
  newPostImageUrl = '';
  newPostVisibility: 'PUBLIC' | 'GROUP' | 'PRIVATE' = 'PUBLIC';
  newPostGroupId: number | null = null;
  myGroups: Group[] = [];
  loading = true;
  uploadingImage = false;

  constructor(private api: ApiService, public auth: AuthService) {}

  get fullImageUrl(): string {
    if (!this.newPostImageUrl) return '';
    return this.newPostImageUrl.startsWith('http') ? this.newPostImageUrl : environment.apiUrl + this.newPostImageUrl;
  }

  ngOnInit(): void {
    this.loadFeed();
    this.api.getGroups().subscribe((list) => (this.myGroups = list));
  }

  loadFeed(): void {
    this.loading = true;
    this.api.getTimeline(0, 30).subscribe({
      next: (list) => {
        this.posts = list || [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 401) alert('Session expirée. Reconnectez-vous pour voir les publications.');
      },
    });
  }

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      alert('Veuillez choisir une image (JPEG, PNG, GIF ou WebP).');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      alert('L\'image ne doit pas dépasser 5 Mo.');
      return;
    }
    input.value = '';
    this.uploadingImage = true;
    this.api.uploadPostImage(file).subscribe({
      next: (res) => {
        this.newPostImageUrl = res.imageUrl;
        this.uploadingImage = false;
      },
      error: (err) => {
        this.uploadingImage = false;
        alert(err.error?.message || 'Erreur lors de l\'upload de la photo.');
      },
    });
  }

  removePhoto(): void {
    this.newPostImageUrl = '';
  }

  createPost(): void {
    const content = this.newPostContent?.trim();
    if (!content) return;
    const groupId = this.newPostVisibility === 'GROUP' ? this.newPostGroupId ?? undefined : undefined;
    this.api.createPost(content, this.newPostImageUrl || undefined, this.newPostVisibility, groupId).subscribe({
      next: (post) => {
        this.posts = [post, ...this.posts];
        this.newPostContent = '';
        this.newPostImageUrl = '';
        this.newPostVisibility = 'PUBLIC';
        this.newPostGroupId = null;
      },
      error: (err) => {
        const status = err.status;
        let msg = err.error?.message || err.message || 'Erreur lors de la publication.';
        if (status === 401) msg = 'Session expirée. Reconnectez-vous.';
        if (status === 403) msg = 'Accès refusé. Reconnectez-vous.';
        alert(msg);
        this.loadFeed();
      },
    });
  }

  deletePost(id: number): void {
    if (!confirm('Supprimer cette publication ?')) return;
    this.api.deletePost(id).subscribe({
      next: () => (this.posts = this.posts.filter((p) => p.id !== id)),
    });
  }

  toggleLike(post: Post): void {
    this.api.toggleLike(post.id).subscribe({
      next: () => {
        post.likedByCurrentUser = !post.likedByCurrentUser;
        post.likeCount += post.likedByCurrentUser ? 1 : -1;
      },
    });
  }
}
