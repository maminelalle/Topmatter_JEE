import { User } from './user.model';

export interface GroupMessage {
  id: number;
  content: string;
  groupId: number;
  sender: User;
  createdAt: string;
}
