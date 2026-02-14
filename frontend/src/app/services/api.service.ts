import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../models/user.model';
import { Post } from '../models/post.model';
import { Comment } from '../models/comment.model';
import { Message } from '../models/message.model';
import { Notification } from '../models/notification.model';
import { Group } from '../models/group.model';
import { GroupMessage } from '../models/group-message.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getMe(): Observable<User> {
    return this.http.get<User>(`${this.base}/users/me`);
  }

  getUser(id: number): Observable<User> {
    return this.http.get<User>(`${this.base}/users/${id}`);
  }

  searchUsers(q: string): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/users/search`, { params: { q } });
  }

  setOnline(online: boolean): Observable<void> {
    return this.http.patch<void>(`${this.base}/users/me/online`, null, { params: { online: String(online) } });
  }

  getTimeline(page = 0, size = 20): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/posts/timeline`, { params: { page: String(page), size: String(size) } });
  }

  searchPosts(q: string, page = 0, size = 20): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/posts/search`, { params: { q, page: String(page), size: String(size) } });
  }

  getPost(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.base}/posts/${id}`);
  }

  uploadPostImage(file: File): Observable<{ imageUrl: string }> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<{ imageUrl: string }>(`${this.base}/posts/upload-image`, form);
  }

  createPost(content: string, imageUrl?: string, visibility?: string, groupId?: number): Observable<Post> {
    return this.http.post<Post>(`${this.base}/posts`, {
      content,
      imageUrl: imageUrl || null,
      visibility: visibility || 'PUBLIC',
      groupId: groupId ?? null,
    });
  }

  updatePost(id: number, content: string, imageUrl?: string, visibility?: string, groupId?: number): Observable<Post> {
    return this.http.put<Post>(`${this.base}/posts/${id}`, {
      content,
      imageUrl: imageUrl || null,
      visibility: visibility || 'PUBLIC',
      groupId: groupId ?? null,
    });
  }

  getGroups(): Observable<Group[]> {
    return this.http.get<Group[]>(`${this.base}/groups`);
  }

  getGroup(id: number): Observable<Group> {
    return this.http.get<Group>(`${this.base}/groups/${id}`);
  }

  createGroup(name: string, description?: string): Observable<Group> {
    return this.http.post<Group>(`${this.base}/groups`, { name, description: description || '' });
  }

  getGroupPosts(groupId: number, page = 0, size = 20): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.base}/groups/${groupId}/posts`, {
      params: { page: String(page), size: String(size) },
    });
  }

  addGroupMember(groupId: number, userId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/groups/${groupId}/members`, { userId });
  }

  leaveGroup(groupId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/groups/${groupId}/leave`, {});
  }

  getGroupMessages(groupId: number, page = 0, size = 100): Observable<GroupMessage[]> {
    return this.http.get<GroupMessage[]>(`${this.base}/groups/${groupId}/messages`, {
      params: { page: String(page), size: String(size) },
    });
  }

  sendGroupMessage(groupId: number, content: string): Observable<GroupMessage> {
    return this.http.post<GroupMessage>(`${this.base}/groups/${groupId}/messages`, { content });
  }

  deletePost(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/posts/${id}`);
  }

  getComments(postId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.base}/comments/post/${postId}`);
  }

  addComment(postId: number, content: string, parentId?: number): Observable<Comment> {
    return this.http.post<Comment>(`${this.base}/comments`, { postId, content, parentId: parentId || null });
  }

  deleteComment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/comments/${id}`);
  }

  toggleLike(postId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/likes/post/${postId}/toggle`, {});
  }

  getUsers(page = 0, size = 100): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/users/list`, {
      params: { page: String(page), size: String(size) },
    });
  }

  /** Tous les utilisateurs depuis la BDD (exclure l'utilisateur courant côté front si besoin). */
  getUsersAll(page = 0, size = 100): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/users/list-all`, {
      params: { page: String(page), size: String(size) },
    });
  }

  getFriends(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/friends`);
  }

  getFriendRequests(): Observable<{ id: number; user: User; status: string }[]> {
    return this.http.get<{ id: number; user: User; status: string }[]>(`${this.base}/friends/requests`);
  }

  getFriendRequestsSent(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/friends/requests/sent`);
  }

  sendFriendRequest(friendId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/friends/request`, { friendId });
  }

  cancelFriendRequest(friendId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/friends/requests/sent/${friendId}`);
  }

  acceptFriendRequest(requestId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/friends/requests/${requestId}/accept`, {});
  }

  rejectFriendRequest(requestId: number): Observable<void> {
    return this.http.post<void>(`${this.base}/friends/requests/${requestId}/reject`, {});
  }

  unfriend(friendId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/friends/${friendId}`);
  }

  getConversations(): Observable<User[]> {
    return this.http.get<User[]>(`${this.base}/messages/conversations`);
  }

  getConversation(otherUserId: number, page = 0, size = 50): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.base}/messages/conversation/${otherUserId}`, {
      params: { page: String(page), size: String(size) },
    });
  }

  sendMessage(receiverId: number, content: string): Observable<Message> {
    return this.http.post<Message>(`${this.base}/messages`, { receiverId, content });
  }

  getNotifications(page = 0, size = 30): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.base}/notifications`, { params: { page: String(page), size: String(size) } });
  }

  getUnreadNotificationCount(): Observable<number> {
    return this.http.get<number>(`${this.base}/notifications/unread-count`);
  }

  markNotificationRead(id: number): Observable<void> {
    return this.http.patch<void>(`${this.base}/notifications/${id}/read`, {});
  }

  markAllNotificationsRead(): Observable<void> {
    return this.http.patch<void>(`${this.base}/notifications/read-all`, {});
  }
}
