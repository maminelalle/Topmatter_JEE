import { User } from './user.model';

export interface Comment {
  id: number;
  content: string;
  user: User;
  postId: number;
  parentId?: number;
  createdAt: string;
  replies?: Comment[];
}
