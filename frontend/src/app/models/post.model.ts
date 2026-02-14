import { User } from './user.model';

export interface Post {
  id: number;
  content: string;
  imageUrl?: string;
  author: User;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  commentCount: number;
  likedByCurrentUser: boolean;
  visibility?: 'PUBLIC' | 'GROUP' | 'PRIVATE';
  groupId?: number;
  groupName?: string;
}
