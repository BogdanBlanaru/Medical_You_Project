import { Role } from "./role.enum";


export interface Doctor {
  id?: number;                // optional on the front-end
  name: string;
  email: string;
  password: string;
  specialization?: string;
  hospital?: string;
  hospitalAddress?: string;
  rating?: number;
  yearsOfExperience?: number;
  education?: string;
  officeHours?: string;
  contactNumber?: string;
  role: Role;                 // 'DOCTOR'
}