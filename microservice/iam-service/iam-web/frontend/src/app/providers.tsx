import { MutationCache, QueryCache, QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useEffect, useState, type ReactNode } from "react";
import axios from "axios";

type ToastDetail = { message: string; tone: "error" | "success" };
type ToastItem = ToastDetail & { id: number };
const TOAST_EVENT = "northstar:toast";
let nextToastId = 0;

export function notifyToast(message: string, tone: ToastDetail["tone"] = "error") {
  window.dispatchEvent(new CustomEvent<ToastDetail>(TOAST_EVENT, { detail: { message, tone } }));
}

function errorToast(error: unknown, context: string): string {
  const status = axios.isAxiosError(error) ? error.response?.status : undefined;
  const body = axios.isAxiosError(error) ? error.response?.data : undefined;
  if (body && typeof body === "object" && !(body instanceof Blob)) {
    const problem = body as { detail?: unknown; message?: unknown; errors?: Record<string, unknown> };
    const details = problem.errors
      ? Object.entries(problem.errors).map(([field, message]) => `${field}: ${String(message)}`).join(" · ")
      : "";
    if (typeof problem.detail === "string" && problem.detail.trim()) return problem.detail;
    if (typeof problem.message === "string" && problem.message.trim()) return problem.message;
    if (details) return details;
  }
  if (status === 401 && context === "login") return "Username/email or password was not accepted.";
  if (status === 401) return "Your session has expired. Please sign in again.";
  if (status === 403) return "You do not have permission to access this resource.";
  if (status === 404) return `The requested ${context} could not be found.`;
  if (context === "users" || context === "roles" || context === "permissions") {
    if (status) return `Failed to load ${context} (HTTP ${status}). Please try again.`;
    if (axios.isAxiosError(error) && error.request) return `Could not reach the API while loading ${context}. Check the service connection and try again.`;
    return `Failed to load ${context}. Please try again.`;
  }
  if (context === "change-password") return "Password change failed. Check your current password and password requirements.";
  return "The request failed. Please try again.";
}

function queryContext(queryKey: readonly unknown[]): string {
  const key = String(queryKey[0] ?? "");
  if (key.includes("user")) return "users";
  if (key.includes("role")) return "roles";
  if (key.includes("permission")) return "permissions";
  if (key.includes("audit")) return "audit logs";
  if (key.includes("activity")) return "account activity";
  return "data";
}

function ToastViewport() {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  useEffect(() => {
    const onToast = (event: Event) => {
      const detail = (event as CustomEvent<ToastDetail>).detail;
      setToasts(current => {
        if (current.some(toast => toast.message === detail.message)) return current;
        const item = { ...detail, id: ++nextToastId };
        window.setTimeout(() => setToasts(items => items.filter(toast => toast.id !== item.id)), 5000);
        return [...current, item].slice(-3);
      });
    };
    window.addEventListener(TOAST_EVENT, onToast);
    return () => window.removeEventListener(TOAST_EVENT, onToast);
  }, []);

  return <div aria-label="Notifications" className="fixed right-4 top-4 z-[100] flex w-[min(24rem,calc(100vw-2rem))] flex-col gap-2" aria-live="polite">
    {toasts.map(toast => <div key={toast.id} role={toast.tone === "error" ? "alert" : "status"} className={`rounded-lg border px-4 py-3 text-sm shadow-lg ${toast.tone === "error" ? "border-rose-200 bg-white text-rose-800" : "border-emerald-200 bg-white text-emerald-800"}`}>
      <div className="flex items-start justify-between gap-3"><span>{toast.message}</span><button aria-label="Dismiss notification" className="text-slate-400 hover:text-slate-700" onClick={() => setToasts(items => items.filter(item => item.id !== toast.id))}>×</button></div>
    </div>)}
  </div>;
}

export function Providers({ children }: { children: ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        queryCache: new QueryCache({
          onError: (error, query) => notifyToast(errorToast(error, queryContext(query.queryKey))),
        }),
        mutationCache: new MutationCache({
          onError: (error, _variables, _context, mutation) => notifyToast(errorToast(error, String(mutation.options.mutationKey?.[0] ?? "data"))),
        }),
        defaultOptions: {
          queries: {
            staleTime: 30_000,
            retry: 1,
            refetchOnWindowFocus: false,
          },
        },
      }),
  );

  return <QueryClientProvider client={queryClient}><ToastViewport />{children}</QueryClientProvider>;
}
