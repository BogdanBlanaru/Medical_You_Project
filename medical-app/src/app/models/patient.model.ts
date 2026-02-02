import { Role } from "./role.enum";

export interface Patient {
  id?: number;          // optional on the front-end
  name: string;
  email: string;
  password: string;
  role: Role;           // 'PATIENT'
}