import { z } from "zod";

export interface SignInValidationMessages {
  usernameRequired: string;
  passwordRequired: string;
}

export function createSignInSchema(messages: SignInValidationMessages) {
  return z.object({
    username: z.string().trim().min(1, messages.usernameRequired),
    password: z.string().min(1, messages.passwordRequired),
  });
}

export type SignInValues = z.infer<ReturnType<typeof createSignInSchema>>;
