import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';
import { AuthService } from '../services/auth.service';
import { Group } from '../models/group.model';
import { GroupMessage } from '../models/group-message.model';
import { Post } from '../models/post.model';
import { User } from '../models/user.model';

@Component({
  selector: 'app-groups',
  templateUrl: './groups.component.html',
  styleUrls: ['./groups.component.css'],
})
export class GroupsComponent implements OnInit, OnDestroy {
  groups: Group[] = [];
  selectedGroup: Group | null = null;
  groupPosts: Post[] = [];
  groupMessages: GroupMessage[] = [];
  groupChatInput = '';
  friends: User[] = [];
  addMemberUserId: number | null = null;
  showCreateModal = false;
  newGroupName = '';
  newGroupDescription = '';
  loading = true;
  private refreshMessagesInterval: ReturnType<typeof setInterval> | null = null;

  constructor(private api: ApiService, private router: Router, public auth: AuthService) {}

  ngOnInit(): void {
    this.loadGroups();
  }

  loadGroups(): void {
    this.loading = true;
    this.api.getGroups().subscribe({
      next: (list) => {
        this.groups = list;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  selectGroup(g: Group): void {
    this.selectedGroup = g;
    this.groupPosts = [];
    this.groupMessages = [];
    this.api.getGroupPosts(g.id, 0, 50).subscribe((list) => (this.groupPosts = list));
    this.loadGroupMessages();
    this.loadFriends();
    if (this.refreshMessagesInterval) clearInterval(this.refreshMessagesInterval);
    this.refreshMessagesInterval = setInterval(() => this.loadGroupMessages(), 3000);
  }

  backToList(): void {
    if (this.refreshMessagesInterval) {
      clearInterval(this.refreshMessagesInterval);
      this.refreshMessagesInterval = null;
    }
    this.selectedGroup = null;
    this.groupPosts = [];
    this.groupMessages = [];
  }

  ngOnDestroy(): void {
    if (this.refreshMessagesInterval) clearInterval(this.refreshMessagesInterval);
  }

  loadFriends(): void {
    this.api.getFriends().subscribe((list) => (this.friends = list || []));
  }

  loadGroupMessages(): void {
    if (!this.selectedGroup) return;
    this.api.getGroupMessages(this.selectedGroup.id, 0, 100).subscribe({
      next: (list) => (this.groupMessages = (list || []).reverse()),
    });
  }

  sendGroupMessage(): void {
    const content = this.groupChatInput?.trim();
    if (!content || !this.selectedGroup) return;
    this.api.sendGroupMessage(this.selectedGroup.id, content).subscribe({
      next: (msg) => {
        this.groupMessages = [...this.groupMessages, msg];
        this.groupChatInput = '';
      },
      error: (err) => alert(err.error?.message || 'Erreur envoi'),
    });
  }

  isMyMessage(msg: GroupMessage): boolean {
    return this.auth.currentUser?.id === msg.sender?.id;
  }

  openCreateModal(): void {
    this.showCreateModal = true;
    this.newGroupName = '';
    this.newGroupDescription = '';
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  createGroup(): void {
    const name = this.newGroupName?.trim();
    if (!name) return;
    this.api.createGroup(name, this.newGroupDescription || undefined).subscribe({
      next: () => {
        this.closeCreateModal();
        this.loadGroups();
      },
      error: (err) => alert(err.error?.message || 'Erreur'),
    });
  }

  leaveGroup(): void {
    if (!this.selectedGroup || !confirm('Quitter ce groupe ?')) return;
    this.api.leaveGroup(this.selectedGroup.id).subscribe({
      next: () => {
        this.backToList();
        this.loadGroups();
      },
      error: (err) => alert(err.error?.message || 'Erreur'),
    });
  }

  addMember(): void {
    if (!this.selectedGroup || this.addMemberUserId == null) return;
    this.api.addGroupMember(this.selectedGroup.id, this.addMemberUserId).subscribe({
      next: () => {
        this.addMemberUserId = null;
        this.loadFriends();
        this.api.getGroup(this.selectedGroup!.id).subscribe((g) => (this.selectedGroup = g));
      },
      error: (err) => alert(err.error?.message || 'Erreur'),
    });
  }

  friendsNotInGroup(): User[] {
    if (!this.selectedGroup?.members) return this.friends;
    const memberIds = new Set(this.selectedGroup.members.map((m) => m.id));
    return this.friends.filter((f) => !memberIds.has(f.id));
  }

  refreshGroupPosts(): void {
    if (this.selectedGroup)
      this.api.getGroupPosts(this.selectedGroup.id, 0, 50).subscribe((list) => (this.groupPosts = list));
  }
}
