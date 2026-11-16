import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { createSignInSchema, type SignInValidationMessages, type SignInValues } from "@/features/auth/sign-in-schema";

export function useSignInForm(messages: SignInValidationMessages) {
  return useForm<SignInValues>({
    resolver: zodResolver(createSignInSchema(messages)),
    defaultValues: { username: "", password: "" },
  });
}
