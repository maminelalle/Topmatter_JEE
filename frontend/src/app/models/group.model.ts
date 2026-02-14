import { User } from './user.model';

export interface Group {
  id: number;
  name: string;
  description?: string;
  createdBy: User;
  createdAt: string;
  members: User[];
  memberCount: number;
}
