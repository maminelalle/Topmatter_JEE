import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Post } from '../../models/post.model';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-post-card',
  templateUrl: './post-card.component.html',
  styleUrls: ['./post-card.component.css'],
})
export class PostCardComponent {
  @Input() post!: Post;
  @Output() deleted = new EventEmitter<number>();
  @Output() liked = new EventEmitter<void>();
  showComments = false;

  constructor(public auth: AuthService) {}

  get isAuthor(): boolean {
    return this.auth.currentUser?.id === this.post?.author?.id;
  }

  get postImageSrc(): string {
    const url = this.post?.imageUrl;
    if (!url) return '';
    return url.startsWith('http') ? url : environment.apiUrl + url;
  }

  onDelete(): void {
    this.deleted.emit(this.post.id);
  }

  onLike(): void {
    this.liked.emit();
  }
}
