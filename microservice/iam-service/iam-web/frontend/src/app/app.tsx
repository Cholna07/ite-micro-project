import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import { BrowserRouter, Link, Navigate, Route, Routes, useLocation, useNavigate, useParams } from "react-router-dom";
import { useMutation, useQueries, useQuery, useQueryClient } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { FiActivity, FiAlertCircle, FiArrowUpRight, FiCheck, FiChevronRight, FiEdit2, FiEye, FiKey, FiLock, FiPlus, FiRefreshCw, FiSearch, FiShield, FiTrash2, FiUsers, FiX } from "react-icons/fi";
import { FaFacebookF, FaGithub, FaGoogle, FaTelegramPlane } from "react-icons/fa";
import { FiMonitor, FiMapPin, FiClock } from "react-icons/fi";
import { Button } from "@/components/ui/button";
import { AppLayout } from "@/components/layouts/app-layout";
import { apiClient, clearAccessToken, getAccessPermissions, hasAccessToken, storeAccessPermissions, storeAccessToken } from "@/lib/api-client";

type Locale = "en" | "km";
type Me = { uuid: string; username: string; email?: string; givenName?: string; familyName?: string; middleName?: string; nickname?: string; gender?: string; phoneNumber?: string; pictureUrl?: string; enabled: boolean; locked: boolean; createdAt?: string; updatedAt?: string };
type Role = { uuid: string; name: string; description?: string; enabled: boolean };
type Permission = { uuid: string; name: string; resource: string; action: string; description?: string; enabled: boolean; system?: boolean; createdAt?: string; updatedAt?: string };
type User = Me & { email?: string; phoneNumber?: string; middleName?: string; accountLocked?: boolean };
type Page<T> = { content: T[]; page?: { totalElements: number; totalPages: number; number: number; first: boolean; last: boolean } };
type AdminActivity = { id: string; userUuid: string; username: string; eventType: string; description: string; occurredAt: string; ipAddress?: string; userAgent?: string };
function apiErrorMessage(error: unknown, fallback: string) {
  if (!axios.isAxiosError(error)) return fallback;
  const data = error.response?.data as { detail?: string; message?: string } | undefined;
  return data?.detail || data?.message || fallback;
}
type SessionRecord = { id: string; credentialType: "JWT" | "HTTP_SESSION"; authenticationType: string; provider?: string; createdAt: string; lastActivityAt: string; expiresAt?: string; ipAddress?: string; userAgent?: string; current: boolean };
type ShellConfig = {
  locale: Locale; csrfToken: string; csrfParameter: string;
  permissions: string[];
  user: { username: string; email: string; givenName: string; familyName: string; pictureUrl: string };
  messages: Record<string, string>;
};

const copy = {
  en: { overview: "Overview", profile: "My profile", editProfile: "Edit profile", password: "Change password", linked: "Linked accounts", apps: "Connected applications", sessions: "Active sessions", history: "Login history", activity: "Account activity", deactivate: "Deactivate account", admin: "Administration", users: "Users", roles: "Roles", permissions: "Permissions", logout: "Sign out", language: "Language", welcome: "Identity administration", hello: "Welcome back", description: "Manage identities, access assignments, and account status.", login: "Sign in to IAM Console", username: "Username or email", passwordLabel: "Password", signIn: "Sign in", loading: "Loading…", search: "Search", create: "Create", refresh: "Refresh", name: "Name", email: "Email", status: "Status", actions: "Actions", active: "Active", disabled: "Disabled", locked: "Locked", enabled: "Enabled", descriptionLabel: "Description", resource: "Resource", action: "Action", save: "Save", cancel: "Cancel", delete: "Delete", edit: "Edit", noResults: "No records found.", error: "The request could not be completed.", usersCount: "Users", rolesCount: "Roles", permissionsCount: "Permissions", quickActions: "Quick actions", account: "Account", unsupported: "This feature has no corresponding backend API yet. Use the existing account page where available.", loadingError: "Unable to load data. Check your access and try again.", createUser: "Create user", createRole: "Create role", createPermission: "Create permission", givenName: "Given name", familyName: "Family name", phone: "Phone number", initialPassword: "Initial password", confirmDelete: "Delete this record?", enable: "Enable", disable: "Disable", lock: "Lock", unlock: "Unlock", userDetails: "User details", assignRole: "Assign role", assignPermission: "Grant permission", saveError: "Save failed", noAdmin: "You are signed in, but the API did not authorize this operation.", resetPassword: "Reset password", deleteUser: "Delete user", saveChanges: "Save changes", permissionRequired: "You need the {permission} permission to view this page." },
  km: { overview: "ទិដ្ឋភាពទូទៅ", profile: "ប្រវត្តិរូប", editProfile: "កែប្រវត្តិរូប", password: "ប្តូរពាក្យសម្ងាត់", linked: "គណនីភ្ជាប់", apps: "កម្មវិធីភ្ជាប់", sessions: "សម័យសកម្ម", history: "ប្រវត្តិចូល", activity: "សកម្មភាពគណនី", deactivate: "បិទគណនី", admin: "ការគ្រប់គ្រង", users: "អ្នកប្រើប្រាស់", roles: "តួនាទី", permissions: "សិទ្ធិ", logout: "ចាកចេញ", language: "ភាសា", welcome: "ការគ្រប់គ្រងអត្តសញ្ញាណ", hello: "សូមស្វាគមន៍", description: "គ្រប់គ្រងអត្តសញ្ញាណ ការផ្តល់សិទ្ធិ និងស្ថានភាពគណនី។", login: "ចូលទៅកាន់ IAM Console", username: "ឈ្មោះអ្នកប្រើ ឬអ៊ីមែល", passwordLabel: "ពាក្យសម្ងាត់", signIn: "ចូលប្រើ", loading: "កំពុងផ្ទុក…", search: "ស្វែងរក", create: "បង្កើត", refresh: "ផ្ទុកឡើងវិញ", name: "ឈ្មោះ", email: "អ៊ីមែល", status: "ស្ថានភាព", actions: "សកម្មភាព", active: "សកម្ម", disabled: "បានបិទ", locked: "បានចាក់សោ", enabled: "បានបើក", descriptionLabel: "ការពិពណ៌នា", resource: "ធនធាន", action: "សកម្មភាព", save: "រក្សាទុក", cancel: "បោះបង់", delete: "លុប", edit: "កែប្រែ", noResults: "រកមិនឃើញទិន្នន័យ។", error: "មិនអាចបំពេញសំណើបានទេ។", usersCount: "អ្នកប្រើប្រាស់", rolesCount: "តួនាទី", permissionsCount: "សិទ្ធិ", quickActions: "សកម្មភាពរហ័ស", account: "គណនី", unsupported: "មុខងារនេះមិនទាន់មាន API ខាងក្រោយទេ។", loadingError: "មិនអាចផ្ទុកទិន្នន័យបានទេ។ សូមពិនិត្យសិទ្ធិ។", createUser: "បង្កើតអ្នកប្រើ", createRole: "បង្កើតតួនាទី", createPermission: "បង្កើតសិទ្ធិ", givenName: "នាមខ្លួន", familyName: "នាមត្រកូល", phone: "លេខទូរស័ព្ទ", initialPassword: "ពាក្យសម្ងាត់ដំបូង", confirmDelete: "លុបទិន្នន័យនេះឬ?", enable: "បើក", disable: "បិទ", lock: "ចាក់សោ", unlock: "ដោះសោ", userDetails: "ព័ត៌មានអ្នកប្រើ", assignRole: "ផ្តល់តួនាទី", assignPermission: "ផ្តល់សិទ្ធិ", saveError: "រក្សាទុកមិនបានទេ", noAdmin: "អ្នកបានចូល ប៉ុន្តែ API មិនអនុញ្ញាតប្រតិបត្តិការនេះទេ។", resetPassword: "កំណត់ពាក្យសម្ងាត់ឡើងវិញ", deleteUser: "លុបអ្នកប្រើ", saveChanges: "រក្សាទុកការផ្លាស់ប្តូរ", permissionRequired: "អ្នកត្រូវការសិទ្ធិ {permission} ដើម្បីមើលទំព័រនេះ។" },
} as const;

function useLocale(): [Locale, (locale: Locale) => void] {
  const [locale, setLocale] = useState<Locale>(new URLSearchParams(window.location.search).get("lang") === "km" ? "km" : "en");
  useEffect(() => { const update = () => { const next = new URLSearchParams(window.location.search).get("lang") === "km" ? "km" : "en"; document.documentElement.lang = next; setLocale(next); }; update(); window.addEventListener("popstate", update); return () => window.removeEventListener("popstate", update); }, []);
  return [locale, (next) => { const u = new URL(window.location.href); u.searchParams.set("lang", next); window.history.replaceState({}, "", u); setLocale(next); document.documentElement.lang = next === "km" ? "km" : "en"; }];
}

export function App() {
  const [locale, setLocale] = useLocale();
  const t = (key: keyof typeof copy.en) => copy[locale][key] ?? key;
  const [loggedIn, setLoggedIn] = useState(hasAccessToken);
  const [accessPermissions, setAccessPermissions] = useState(getAccessPermissions);
  const queryClient = useQueryClient();
  const location = useLocation();
  const navigate = useNavigate();
  const publicRoute = location.pathname === "/login" || location.pathname === "/register";
  const me = useQuery({ queryKey: ["me"], queryFn: async () => {
    const response = await apiClient.get<Me>("/me");
    const header = response.headers["x-iam-permissions"];
    if (typeof header === "string") {
      const permissions = header.split(",").filter(Boolean);
      storeAccessPermissions(permissions);
      setAccessPermissions(permissions);
    }
    return response.data;
  }, enabled: loggedIn || !publicRoute, retry: false });
  useEffect(() => { if (me.data) setLoggedIn(true); }, [me.data]);
  useEffect(() => { if (loggedIn && me.isError && (me.error as AxiosError)?.response?.status === 401) { clearAccessToken(); setLoggedIn(false); } }, [loggedIn, me.isError, me.error]);

  const login = useMutation({ mutationFn: async (values: { usernameOrEmail: string; password: string }) => (await apiClient.post<{ accessToken: string; permissions?: string[] }>("/auth/login", values)).data, onSuccess: ({ accessToken, permissions = [] }) => { storeAccessToken(accessToken); storeAccessPermissions(permissions); setAccessPermissions(permissions); setLoggedIn(true); queryClient.invalidateQueries(); const from = (location.state as { from?: { pathname?: string; search?: string } } | null)?.from; navigate(from?.pathname ? `${from.pathname}${from.search ?? ""}` : "/", { replace: true }); } });
  if (!loggedIn && me.isLoading) return <div className="grid min-h-screen place-items-center bg-[#F8FAFC] text-sm text-slate-500">{t("loading")}</div>;
  if (!loggedIn) return <Routes>
    <Route path="/register" element={<Register locale={locale} onLocale={setLocale} onRegistered={() => navigate("/login?registered=1")} />} />
    <Route path="/login" element={<Login locale={locale} t={t} onLocale={setLocale} onSubmit={(values) => login.mutate(values)} busy={login.isPending} error={login.error ? t("error") : ""} />} />
    <Route path="*" element={<Navigate to="/login" replace state={{ from: location }} />} />
  </Routes>;
  if (me.isLoading) return <div className="grid min-h-screen place-items-center bg-[#F8FAFC] text-sm text-slate-500">{t("loading")}</div>;
  if (!me.data) return <main className="grid min-h-screen place-items-center bg-[#F8FAFC] p-6"><section className="max-w-md rounded-xl border bg-white p-7 text-center"><FiLock className="mx-auto text-[#534AB7]" size={24}/><h1 className="mt-3 font-semibold">{t("noAdmin")}</h1><button className="mt-5 text-sm text-rose-600" onClick={() => { clearAccessToken(); setLoggedIn(false); }}> {t("logout")}</button></section></main>;

  const langCopy = copy[locale];
  const shellConfig: ShellConfig = { locale, csrfToken: "", csrfParameter: "_csrf", user: { username: me.data.username, email: me.data.email ?? "", givenName: me.data.givenName ?? "", familyName: me.data.familyName ?? "", pictureUrl: me.data.pictureUrl ?? "" }, permissions: accessPermissions, messages: { "dashboard.overview": langCopy.overview, "dashboard.closeMenu": locale === "km" ? "បិទម៉ឺនុយ" : "Close menu", "dashboard.menu": locale === "km" ? "ម៉ឺនុយ" : "Menu", "dashboard.expandSidebar": locale === "km" ? "ពង្រីករបារចំហៀង" : "Expand sidebar", "dashboard.collapseSidebar": locale === "km" ? "បង្រួមរបារចំហៀង" : "Collapse sidebar", "dashboard.identityWorkspace": locale === "km" ? "កន្លែងធ្វើការអត្តសញ្ញាណ" : "Identity workspace", "dashboard.linkedAccounts": langCopy.linked, "dashboard.connectedApplications": langCopy.apps, "common.language": langCopy.language, "common.logout": langCopy.logout, "profile.edit": langCopy.editProfile, "profile.changePassword": langCopy.password } };
  const logout = async () => {
    try { await apiClient.delete("/me/sessions/current"); } catch { /* Continue local and servlet logout when no inventory-backed session is available. */ }
    try { await apiClient.post("/logout", {}, { baseURL: "" }); } catch { /* Still clear this browser's credentials if its server session has expired. */ }
    clearAccessToken(); queryClient.clear(); setLoggedIn(false); navigate("/login?logout=1");
  };
  const title = pathTitle(location.pathname, t);
  return <AppLayout config={shellConfig as never} title={title} onLogout={logout}>
    <Routes>
      <Route path="/" element={<Overview me={me.data} t={t} permissions={accessPermissions} locale={locale} />} />
      <Route path="/users" element={<UsersPage t={t} canRead={accessPermissions.includes("user.read")} canManage={accessPermissions.includes("user.manage")} />} />
      <Route path="/users/:uuid" element={<UserDetail t={t} canRead={accessPermissions.includes("user.read")} canManage={accessPermissions.includes("user.manage")} canAssignRoles={accessPermissions.includes("user.role.assign")} canReadRoles={accessPermissions.includes("role.read")} canAssignPermissions={accessPermissions.includes("user.permission.assign")} canReadPermissions={accessPermissions.includes("permission.read")} />} />
      <Route path="/roles" element={<RolesPage t={t} canRead={accessPermissions.includes("role.read")} canManage={accessPermissions.includes("role.manage")} />} />
      <Route path="/roles/:uuid" element={<RoleDetail t={t} canRead={accessPermissions.includes("role.read")} canAssignPermissions={accessPermissions.includes("role.permission.assign")} canReadPermissions={accessPermissions.includes("permission.read")} />} />
      <Route path="/permissions" element={<PermissionsPage t={t} locale={locale} canRead={accessPermissions.includes("permission.read")} canManage={accessPermissions.includes("permission.manage")} />} />
      <Route path="/account/profile" element={<AccountProfile me={me.data} locale={locale} />} />
      <Route path="/account/password" element={<PasswordPage locale={locale} />} />
      <Route path="/account/linked" element={<LinkedAccountsPage locale={locale} />} />
      <Route path="/account/apps" element={<UnavailableFeature locale={locale} feature="apps" />} />
      <Route path="/account/sessions" element={<AccountSessionsPage locale={locale} />} />
      <Route path="/account/history" element={<LoginHistoryPage locale={locale} />} />
      <Route path="/account/activity" element={<AccountActivityPage locale={locale} />} />
      <Route path="/account/deactivate" element={<AccountDeactivationPage user={me.data} locale={locale} onLogout={logout} />} />
      <Route path="/oauth2/consent" element={<OAuthConsentPage locale={locale} />} />
      <Route path="/profile/edit" element={<Navigate to="/account/profile" replace />} />
      <Route path="/profile/password" element={<Navigate to="/account/password" replace />} />
      <Route path="/profile/accounts" element={<Navigate to="/account/linked" replace />} />
      <Route path="/profile/apps" element={<Navigate to="/account/apps" replace />} />
      <Route path="/clients" element={<OAuthClientAdminPage locale={locale} canRead={accessPermissions.includes("client.read")} canManage={accessPermissions.includes("client.manage")} canRegister={accessPermissions.includes("client.create")} />} />
      <Route path="/clients/register" element={<OAuthClientRegistration locale={locale} canRegister={accessPermissions.includes("client.create")} />} />
      <Route path="/admin/sessions" element={<AdminSessionsPage locale={locale} canRevoke={accessPermissions.includes("session.revoke")} />} />
      <Route path="/admin/history" element={<LoginHistoryPage admin locale={locale} />} />
      <Route path="/audit-logs" element={<UnavailableFeature locale={locale} feature="auditLogs" />} />
      <Route path="/system-config" element={<UnavailableFeature locale={locale} feature="systemConfig" />} />
      <Route path="/account/*" element={<UnavailableFeature locale={locale} feature="activity" />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  </AppLayout>;
}

function pathTitle(path: string, t: (key: keyof typeof copy.en) => string) {
  if (path.startsWith("/roles/")) return t("roles");
  if (path.startsWith("/users/")) return t("userDetails");
  if (path.startsWith("/users")) return t("users");
  if (path.startsWith("/roles")) return t("roles");
  if (path.startsWith("/permissions")) return t("permissions");
  if (path.startsWith("/clients")) return localeField("OAuth clients","កម្មវិធី OAuth");
  if (path.startsWith("/admin/sessions")) return t("sessions");
  if (path.startsWith("/admin/history")) return t("history");
  if (path.startsWith("/audit-logs")) return localeField("Audit logs","កំណត់ហេតុសវនកម្ម");
  if (path.startsWith("/system-config")) return localeField("System configuration","ការកំណត់ប្រព័ន្ធ");
  if (path.startsWith("/account/linked")) return t("linked");
  if (path.startsWith("/account/apps")) return t("apps");
  if (path.startsWith("/account/sessions")) return t("sessions");
  if (path.startsWith("/account/history")) return t("history");
  if (path.startsWith("/account/activity")) return t("activity");
  if (path.startsWith("/account/deactivate")) return t("deactivate");
  if (path.startsWith("/account")) return t("account");
  return t("overview");
}

function Login({ locale, t, onLocale, onSubmit, busy, error }: { locale: Locale; t: (key: keyof typeof copy.en) => string; onLocale: (v: Locale) => void; onSubmit: (v: { usernameOrEmail: string; password: string }) => void; busy: boolean; error: string }) {
  const sessionAuth = new URLSearchParams(window.location.search).get("flow") === "oauth";
  const sessionLoginError = sessionAuth && new URLSearchParams(window.location.search).has("error");
  const [enabledProviders, setEnabledProviders] = useState<string[]>([]);
  useEffect(() => {
    fetch("/login", { headers: { "X-Requested-With": "XMLHttpRequest" }, cache: "no-store" })
      .then(response => setEnabledProviders((response.headers.get("X-Northstar-OAuth-Providers") ?? "").split(",").filter(Boolean)))
      .catch(() => setEnabledProviders([]));
  }, []);
  const statusMessage = new URLSearchParams(window.location.search).has("logout") ? (locale === "km" ? "បានចាកចេញដោយជោគជ័យ។" : "You have signed out.") : new URLSearchParams(window.location.search).has("registered") ? (locale === "km" ? "បានបង្កើតគណនី។ សូមចូលប្រើ។" : "Account created. Please sign in.") : "";
  const providers = [
    { id: "google", label: "Google", icon: <FaGoogle aria-hidden="true" /> },
    { id: "github", label: "GitHub", icon: <FaGithub aria-hidden="true" /> },
    { id: "facebook", label: "Facebook", icon: <FaFacebookF aria-hidden="true" /> },
    { id: "telegram", label: "Telegram", icon: <FaTelegramPlane aria-hidden="true" /> },
  ];
  return <main className="grid min-h-screen bg-[#F8FAFC] lg:grid-cols-[minmax(380px,0.85fr)_1.15fr]">
    <aside className="hidden flex-col justify-between bg-[#14213D] p-10 text-white lg:flex"><Link className="text-lg font-semibold tracking-tight" to="/login">✦ Northstar IAM</Link><div><p className="text-xs font-semibold uppercase tracking-[.2em] text-indigo-200">{t("welcome")}</p><h1 className="mt-4 max-w-md text-4xl font-semibold leading-tight">{locale === "km" ? "អត្តសញ្ញាណគ្រប់រូប ចូលប្រើបានដោយសុវត្ថិភាព។" : "Secure access for every identity."}</h1><p className="mt-4 max-w-sm text-sm leading-6 text-slate-300">{locale === "km" ? "កន្លែងដែលអាចទុកចិត្តបានសម្រាប់គ្រប់គ្រងអ្នកប្រើ តួនាទី និងសិទ្ធិ។" : "One trusted place to manage users, roles, and permissions."}</p></div><p className="text-xs text-slate-400">Northstar IAM · {locale === "km" ? "កន្លែងធ្វើការអត្តសញ្ញាណ" : "Identity workspace"}</p></aside>
    <section className="flex items-center justify-center p-5 sm:p-10"><div className="w-full max-w-md"><div className="mb-8 flex items-center justify-between lg:justify-end"><span className="font-semibold lg:hidden">✦ Northstar IAM</span><select aria-label={t("language")} className="rounded-lg border border-[#E5E7EB] bg-white px-3 py-2 text-sm" value={locale} onChange={(e) => onLocale(e.target.value as Locale)}><option value="en">English</option><option value="km">ខ្មែរ</option></select></div><h2 className="text-2xl font-semibold tracking-tight">{t("login")}</h2><p className="mt-2 text-sm text-slate-500">{t("description")}</p>{statusMessage && <p className="mt-4 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800" role="status">{statusMessage}</p>}<form action={sessionAuth ? "/login" : undefined} method={sessionAuth ? "post" : undefined} className="mt-7 space-y-4" onSubmit={sessionAuth ? () => clearAccessToken() : (e) => { e.preventDefault(); const f = new FormData(e.currentTarget); onSubmit({ usernameOrEmail: String(f.get("usernameOrEmail") ?? ""), password: String(f.get("password") ?? "") }); }}>
      {sessionAuth && <input name="_csrf" type="hidden" value={readCookie("XSRF-TOKEN")} />}
      <Field label={t("username")} name={sessionAuth ? "username" : "usernameOrEmail"} required/><Field label={t("passwordLabel")} name="password" type="password" required/><Button className="h-11 w-full rounded-lg bg-[#534AB7] text-sm font-semibold text-white hover:bg-[#43389B] disabled:opacity-60" disabled={!sessionAuth && busy}>{busy ? t("loading") : t("signIn")}</Button>{(error || sessionLoginError) && <p role="alert" className="text-sm text-rose-700">{error || (locale === "km" ? "ឈ្មោះអ្នកប្រើ ឬពាក្យសម្ងាត់មិនត្រឹមត្រូវទេ។" : "The username or password was not accepted.")}</p>}</form>
      {enabledProviders.length > 0 ? <div className="mt-6"><p className="mb-3 text-center text-xs text-slate-500">{locale === "km" ? "ឬបន្តដោយប្រើគណនីសង្គម" : "Or continue with a social account"}</p><div className="grid grid-cols-2 gap-2">{providers.filter(provider => enabledProviders.includes(provider.id)).map(provider => <a className="flex h-10 items-center justify-center gap-2 rounded-lg border border-[#E5E7EB] bg-white text-sm font-medium text-slate-700 hover:bg-slate-50" href={`/oauth2/authorization/${provider.id}`} key={provider.id} onClick={() => clearAccessToken()}>{provider.icon}{provider.label}</a>)}</div></div> : <p className="mt-6 text-center text-xs text-slate-500">{locale === "km" ? "មិនមានគណនីសង្គមដែលបានកំណត់រចនាសម្ព័ន្ធទេ។" : "Social sign-in is not configured."}</p>}
      <p className="mt-5 text-center text-xs text-slate-500">{locale === "km" ? "មិនទាន់មានគណនី?" : "New to Northstar IAM?"} <Link className="font-medium text-[#534AB7] hover:underline" to="/register">{locale === "km" ? "ចុះឈ្មោះ" : "Create an account"}</Link></p></div></section>
  </main>;
}

function Register({ locale, onLocale, onRegistered }: { locale: Locale; onLocale: (locale: Locale) => void; onRegistered: () => void }) {
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const km = locale === "km";
  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setBusy(true);
    const form = new FormData(event.currentTarget);
    const password = String(form.get("password") ?? "");
    const confirmPassword = String(form.get("confirmPassword") ?? "");
    if (password !== confirmPassword) {
      setError(km ? "ពាក្យសម្ងាត់មិនត្រូវគ្នាទេ។" : "Passwords do not match.");
      setBusy(false);
      return;
    }
    try {
      await apiClient.post("/auth/register", {
        username: String(form.get("username") ?? ""),
        email: String(form.get("email") ?? ""),
        password,
        confirmPassword,
        givenName: String(form.get("givenName") ?? ""),
        familyName: String(form.get("familyName") ?? ""),
        locale,
      });
      onRegistered();
    } catch (cause) {
      const response = (cause as AxiosError<{ message?: string }>).response;
      setError(response?.data?.message ?? (km ? "មិនអាចបង្កើតគណនីបានទេ។ សូមពិនិត្យព័ត៌មាន។" : "Registration failed. Check the details and try again."));
    } finally {
      setBusy(false);
    }
  };
  return <main className="grid min-h-screen place-items-center bg-[#F8FAFC] p-5">
    <section className="w-full max-w-lg rounded-2xl border border-[#E5E7EB] bg-white p-6 shadow-sm sm:p-9">
      <header className="mb-7 flex items-center justify-between gap-4"><Link className="font-semibold text-[#534AB7]" to="/login">✦ Northstar IAM</Link><select aria-label={km ? "ភាសា" : "Language"} className="rounded-lg border border-[#E5E7EB] bg-white px-3 py-2 text-sm" value={locale} onChange={event => onLocale(event.target.value as Locale)}><option value="en">English</option><option value="km">ខ្មែរ</option></select></header>
      <h1 className="text-2xl font-semibold tracking-tight">{km ? "បង្កើតគណនី" : "Create your account"}</h1>
      <p className="mt-2 text-sm text-slate-500">{km ? "បំពេញព័ត៌មានដើម្បីចុះឈ្មោះ Northstar IAM។" : "Register for a Northstar IAM account."}</p>
      <form className="mt-6 space-y-4" onSubmit={submit}>
        <Field label={km ? "ឈ្មោះអ្នកប្រើ" : "Username"} name="username" required />
        <Field label={km ? "អ៊ីមែល" : "Email"} name="email" type="email" required />
        <div className="grid gap-4 sm:grid-cols-2"><Field label={km ? "នាមខ្លួន" : "Given name"} name="givenName" /><Field label={km ? "នាមត្រកូល" : "Family name"} name="familyName" /></div>
        <Field label={km ? "ពាក្យសម្ងាត់" : "Password"} name="password" type="password" required />
        <Field label={km ? "បញ្ជាក់ពាក្យសម្ងាត់" : "Confirm password"} name="confirmPassword" type="password" required />
        {error && <p className="text-sm text-rose-700" role="alert">{error}</p>}
        <Button className="h-11 w-full rounded-lg bg-[#534AB7] text-sm font-semibold text-white hover:bg-[#43389B] disabled:opacity-60" disabled={busy}>{busy ? (km ? "កំពុងបង្កើត…" : "Creating account…") : (km ? "ចុះឈ្មោះ" : "Create account")}</Button>
      </form>
      <p className="mt-5 text-center text-sm text-slate-500">{km ? "មានគណនីរួចហើយ?" : "Already registered?"} <Link className="font-semibold text-[#534AB7] hover:underline" to="/login">{km ? "ចូលប្រើ" : "Sign in"}</Link></p>
    </section>
  </main>;
}
function Field({ label, name, type = "text", required = false, defaultValue = "" }: { label: string; name: string; type?: string; required?: boolean; defaultValue?: string }) { return <label className="block text-sm font-medium text-slate-700">{label}<input className="mt-1.5 h-10 w-full rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm outline-none focus:border-[#534AB7] focus:ring-2 focus:ring-[#EEECFF]" name={name} type={type} required={required} defaultValue={defaultValue}/></label>; }

function OAuthConsentPage({ locale }: { locale: Locale }) {
  const params = new URLSearchParams(window.location.search);
  const clientId = params.get("client_id") ?? "";
  const clientName = params.get("client_name") || clientId;
  const state = params.get("state") ?? "";
  const scopes = params.getAll("scope");
  const csrfToken = readCookie("XSRF-TOKEN");
  const km = locale === "km";
  return <section className="mx-auto max-w-2xl rounded-xl border border-[#E5E7EB] bg-white p-6 sm:p-8">
    <p className="text-xs font-semibold uppercase tracking-[.14em] text-[#534AB7]">Northstar IAM · OAuth 2.0</p>
    <h1 className="mt-2 text-xl font-semibold">{km ? "ការអនុញ្ញាត OAuth" : "Authorize application"}</h1>
    <p className="mt-2 text-sm text-slate-600">{km ? `${clientName} ស្នើសុំសិទ្ធិចូលប្រើគណនីរបស់អ្នក។` : `${clientName} is requesting access to your account.`}</p>
    <p className="mt-5 text-xs text-slate-500">{km ? `Client ID: ${clientId}` : `Client ID: ${clientId}`}</p>
    <h2 className="mt-6 text-sm font-semibold">{km ? "សិទ្ធិដែលបានស្នើសុំ" : "Requested permissions"}</h2>
    {scopes.length ? <ul className="mt-3 space-y-2">{scopes.map(scope => <li className="rounded-lg bg-slate-50 px-3 py-2 text-sm text-slate-700" key={scope}>{scope}</li>)}</ul> : <p className="mt-2 text-sm text-slate-500">{km ? "មិនមានសិទ្ធិបានបញ្ជាក់។" : "No scopes were specified."}</p>}
    {!csrfToken && <p className="mt-4 text-sm text-rose-700" role="alert">{km ? "សញ្ញាសុវត្ថិភាពមិនមានទេ។ សូមផ្ទុកទំព័រឡើងវិញ។" : "The security token is unavailable. Refresh this page and try again."}</p>}
    <form action="/oauth2/authorize" className="mt-6 flex flex-wrap gap-3" method="post">
      <input name="_csrf" type="hidden" value={csrfToken} />
      <input name="client_id" type="hidden" value={clientId} />
      <input name="state" type="hidden" value={state} />
      {scopes.map(scope => <input key={scope} name="scope" type="hidden" value={scope} />)}
      <Button className="h-10 rounded-lg bg-[#534AB7] px-4 text-sm font-semibold text-white hover:bg-[#43389B] disabled:opacity-50" disabled={!csrfToken} name="user_oauth_approval" type="submit" value="true">{km ? "អនុញ្ញាត" : "Allow"}</Button>
      <Button className="h-10 rounded-lg border border-[#E5E7EB] bg-white px-4 text-sm font-semibold text-slate-700 hover:bg-slate-50 disabled:opacity-50" disabled={!csrfToken} name="user_oauth_approval" type="submit" value="false">{km ? "បដិសេធ" : "Deny"}</Button>
    </form>
  </section>;
}

function readCookie(name: string): string {
  return document.cookie.split(";").map(part => part.trim()).find(part => part.startsWith(`${name}=`))?.slice(name.length + 1) ?? "";
}

function Overview({ me, t, permissions, locale }: { me: Me; t: (key: keyof typeof copy.en) => string; permissions: string[]; locale: Locale }) {
  const can = (permission: string) => permissions.includes(permission);
  const users = useQuery({ queryKey: ["users", "overview-count"], queryFn: async () => (await apiClient.get<Page<User>>("/users", { params: { deleted: false, page: 0, size: 1 } })).data, enabled: can("user.read") });
  const activeUsers = useQuery({ queryKey: ["users", "overview-active-count"], queryFn: async () => (await apiClient.get<Page<User>>("/users", { params: { enabled: true, locked: false, deleted: false, page: 0, size: 1 } })).data, enabled: can("user.read") });
  const roles = useQuery({ queryKey: ["roles", "overview-count"], queryFn: async () => (await apiClient.get<Page<Role>>("/roles", { params: { page: 0, size: 1 } })).data, enabled: can("role.read") });
  const permissionList = useQuery({ queryKey: ["permissions", "overview-count"], queryFn: async () => (await apiClient.get<Page<Permission>>("/permissions", { params: { page: 0, size: 1 } })).data, enabled: can("permission.read") });
  const clients = useQuery({ queryKey: ["clients", "overview-count"], queryFn: async () => (await apiClient.get<Page<ManagedClient>>("/admin/clients", { params: { page: 0, size: 1, sortBy: "clientName", sortDirection: "ASC" } })).data, enabled: can("client.read") });
  const activities = useQuery({ queryKey: ["admin-account-activities", "overview"], queryFn: async () => (await apiClient.get<Page<AdminActivity>>("/admin/account-activities", { params: { page: 0, size: 5 } })).data, enabled: can("activity.read") });
  const name = [me.givenName, me.familyName].filter(Boolean).join(" ") || me.username;
  const denied = locale === "km" ? "គ្មានសិទ្ធិមើល" : "Access required";
  const loading = locale === "km" ? "កំពុងផ្ទុក…" : "Loading…";
  const date = (value: string) => new Intl.DateTimeFormat(locale === "km" ? "km-KH" : "en-US", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
  const eventTitle = (event: AdminActivity) => {
    const labels: Record<string, [string, string]> = {
      PASSWORD_CHANGED: ["Password changed", "បានប្តូរពាក្យសម្ងាត់"], PROFILE_UPDATED: ["Profile updated", "បានធ្វើបច្ចុប្បន្នភាពប្រវត្តិរូប"],
      EMAIL_CHANGED: ["Email changed", "បានប្តូរអ៊ីមែល"], SOCIAL_ACCOUNT_LINKED: ["Social account linked", "បានភ្ជាប់គណនីសង្គម"],
      SOCIAL_ACCOUNT_UNLINKED: ["Social account unlinked", "បានផ្តាច់គណនីសង្គម"], CURRENT_SESSION_REVOKED: ["Current session revoked", "បានដកហូតសម័យបច្ចុប្បន្ន"],
      OTHER_SESSIONS_REVOKED: ["Other sessions revoked", "បានដកហូតសម័យផ្សេងទៀត"], ACCOUNT_DEACTIVATED: ["Account deactivated", "បានបិទគណនី"], ACCOUNT_LOCKED: ["Account locked", "បានចាក់សោគណនី"]
    };
    return labels[event.eventType]?.[locale === "km" ? 1 : 0] ?? event.description;
  };
  const metric = (title: string, value: number | undefined, isLoading: boolean, isError: boolean, permission: string, icon: ReactNode, href?: string) => <Metric title={title} value={value} icon={icon} href={href} loading={isLoading} error={isError} denied={!can(permission)} deniedLabel={denied} loadingLabel={loading} errorLabel={locale === "km" ? "មិនអាចផ្ទុកបាន" : "Could not load"} />;
  return <div className="space-y-6"><div><p className="text-xs font-semibold uppercase tracking-[.14em] text-[#534AB7]">{t("welcome")}</p><h1 className="mt-1 text-2xl font-semibold tracking-tight">{t("hello")}, {name}</h1><p className="mt-1 text-sm text-slate-500">{t("description")}</p></div>
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
      {metric(t("usersCount"), users.data?.page?.totalElements, users.isLoading, users.isError, "user.read", <FiUsers/>, "/users")}
      {metric(localeField("Active users", "អ្នកប្រើប្រាស់សកម្ម"), activeUsers.data?.page?.totalElements, activeUsers.isLoading, activeUsers.isError, "user.read", <FiCheck/>, "/users")}
      {metric(t("rolesCount"), roles.data?.page?.totalElements, roles.isLoading, roles.isError, "role.read", <FiShield/>, "/roles")}
      {metric(t("permissionsCount"), permissionList.data?.page?.totalElements, permissionList.isLoading, permissionList.isError, "permission.read", <FiKey/>, "/permissions")}
      {metric(localeField("OAuth clients", "OAuth Clients"), clients.data?.page?.totalElements, clients.isLoading, clients.isError, "client.read", <FiArrowUpRight/>, "/clients")}
      {metric(localeField("Recent activities", "សកម្មភាពថ្មីៗ"), activities.data?.page?.totalElements, activities.isLoading, activities.isError, "activity.read", <FiActivity/>, undefined)}
    </div>
    {permissions.some(value => ["user.read", "role.read", "permission.read"].includes(value)) && <section className="rounded-xl border border-[#E5E7EB] bg-white"><header className="border-b border-slate-100 px-5 py-4"><h2 className="font-semibold">{localeField("Quick actions", "សកម្មភាពរហ័ស")}</h2></header><div className="grid divide-y divide-slate-100 sm:grid-cols-3 sm:divide-x sm:divide-y-0">{can("user.read") && <QuickLink to="/users" icon={<FiUsers/>} label={t("users")}/ >}{can("role.read") && <QuickLink to="/roles" icon={<FiShield/>} label={t("roles")}/ >}{can("permission.read") && <QuickLink to="/permissions" icon={<FiKey/>} label={t("permissions")}/>}</div></section>}
    {can("activity.read") && <section className="rounded-xl border border-[#E5E7EB] bg-white"><header className="border-b border-slate-100 px-5 py-4"><h2 className="font-semibold">{localeField("Recent account activity", "សកម្មភាពគណនីថ្មីៗ")}</h2></header>{activities.isLoading ? <p className="p-5 text-sm text-slate-500">{loading}</p> : activities.isError ? <p role="alert" className="p-5 text-sm text-rose-700">{localeField("Unable to load recent activity.", "មិនអាចផ្ទុកសកម្មភាពថ្មីៗបានទេ។")}</p> : !activities.data?.content.length ? <p className="p-5 text-sm text-slate-500">{localeField("No account activities have been recorded yet.", "មិនទាន់មានសកម្មភាពគណនីទេ។")}</p> : <ul className="divide-y divide-slate-100">{activities.data.content.map(event => <li key={event.id} className="flex flex-wrap items-start justify-between gap-2 px-5 py-4"><div className="min-w-0"><p className="text-sm font-medium">{eventTitle(event)}</p><p className="mt-1 break-all text-xs text-slate-500">{event.username} ({event.userUuid}) · {event.description}</p>{(event.ipAddress || event.userAgent) && <p className="mt-1 break-words text-xs text-slate-500">{[event.ipAddress, event.userAgent].filter(Boolean).join(" · ")}</p>}</div><time className="shrink-0 text-xs text-slate-500" dateTime={event.occurredAt}>{date(event.occurredAt)}</time></li>)}</ul>}</section>}
    <section className="rounded-xl border border-indigo-100 bg-indigo-50/60 p-4 text-sm text-indigo-950"><strong>{me.username}</strong><span className="ml-2 text-indigo-800">{me.email ?? ""}</span><span className="float-right rounded-full bg-white px-2.5 py-1 text-xs">{me.locked ? t("locked") : me.enabled ? t("active") : t("disabled")}</span></section></div>;
}
function Metric({ title, value, icon, href, loading, error, denied, deniedLabel, loadingLabel, errorLabel }: { title: string; value?: number; icon: React.ReactNode; href?: string; loading: boolean; error: boolean; denied: boolean; deniedLabel: string; loadingLabel: string; errorLabel: string }) {
  const content = <><span className="grid size-10 place-items-center rounded-lg bg-[#EEECFF] text-[#534AB7]">{icon}</span><span className="min-w-0"><span className="block text-xs text-slate-500">{title}</span><span className="text-xl font-semibold">{denied ? deniedLabel : loading ? loadingLabel : error ? errorLabel : value ?? 0}</span></span>{href && <FiArrowUpRight className="ml-auto shrink-0 text-slate-400"/>}</>;
  const className = "flex items-center gap-4 rounded-xl border border-[#E5E7EB] bg-white p-5 transition hover:border-indigo-200 hover:bg-indigo-50/30";
  return href ? <Link aria-label={`${title}: ${denied ? deniedLabel : loading ? loadingLabel : error ? errorLabel : value ?? 0}`} className={className} to={href}>{content}</Link> : <div aria-label={`${title}: ${denied ? deniedLabel : loading ? loadingLabel : error ? errorLabel : value ?? 0}`} className={className}>{content}</div>;
}
function QuickLink({ to, icon, label }: { to: string; icon: React.ReactNode; label: string }) { return <Link className="flex items-center gap-3 px-5 py-4 text-sm font-medium text-slate-700 hover:bg-slate-50" to={to}><span className="text-[#534AB7]">{icon}</span>{label}<FiChevronRight className="ml-auto text-slate-400"/></Link>; }

function DataPage<T extends { uuid: string; name?: string; username?: string; email?: string; enabled: boolean }>({ kind, endpoint, t, locale = "en", canRead = true, canManage = false }: { kind: "users" | "roles" | "permissions"; endpoint: string; t: (key: keyof typeof copy.en) => string; locale?: Locale; canRead?: boolean; canManage?: boolean }) {
  const [search, setSearch] = useState(""); const [currentPage, setCurrentPage] = useState(0); const [statusFilter, setStatusFilter] = useState<"all" | "active" | "inactive">("all"); const [modal, setModal] = useState(false); const [selected, setSelected] = useState<T | null>(null); const [viewingPermission, setViewingPermission] = useState<string | null>(null); const [error, setError] = useState("");
  const queryClient = useQueryClient(); const page = useQuery({ queryKey: [kind, search, currentPage, statusFilter], queryFn: async () => (await apiClient.get<Page<T>>(endpoint, { params: { keyword: search || undefined, ...(kind === "users" ? { enabled: statusFilter === "all" ? undefined : statusFilter === "active" } : { active: statusFilter === "all" ? undefined : statusFilter === "active" }), page: currentPage, size: 20 } })).data, enabled: canRead });
  const remove = useMutation({ mutationFn: (uuid: string) => apiClient.delete(`${endpoint}/${uuid}`), onSuccess: () => queryClient.invalidateQueries({ queryKey: [kind] }), onError: () => setError(t("error")) });
  const toggle = useMutation({ mutationFn: ({ uuid, enabled }: { uuid: string; enabled: boolean }) => apiClient.put(`${endpoint}/${uuid}/${kind === "users" ? (enabled ? "disable" : "enable") : (enabled ? "deactivate" : "activate")}`), onSuccess: () => queryClient.invalidateQueries({ queryKey: [kind] }), onError: () => setError(t("error")) });
  if (!canRead) return <PermissionRequired t={t} permission={kind === "users" ? "user.read" : kind === "roles" ? "role.read" : "permission.read"}/>;
  const title = t(kind); const createTitle = t(kind === "users" ? "createUser" : kind === "roles" ? "createRole" : "createPermission");
  return <section className="space-y-4"><div className="flex flex-wrap items-center justify-between gap-3"><div><h1 className="text-xl font-semibold">{title}</h1><p className="mt-1 text-sm text-slate-500">{page.data?.page?.totalElements ?? "—"} {title.toLowerCase()}</p></div>{canManage && <button onClick={() => { setSelected(null); setError(""); setModal(true); }} className="inline-flex h-9 items-center gap-2 rounded-lg bg-[#534AB7] px-3.5 text-sm font-medium text-white hover:bg-[#43389B]"><FiPlus/>{t("create")}</button>}</div>
  <div className="flex flex-wrap gap-2"><div className="relative min-w-[200px] max-w-sm flex-1"><FiSearch className="absolute left-3 top-2.5 text-slate-400"/><input className="h-9 w-full rounded-lg border border-[#E5E7EB] pl-9 pr-3 text-sm" placeholder={t("search")} value={search} onChange={e => { setSearch(e.target.value); setCurrentPage(0); }}/></div><select aria-label={localeField("Filter by status", "ត្រងតាមស្ថានភាព")} className="h-9 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm text-slate-700" value={statusFilter} onChange={e => { setStatusFilter(e.target.value as "all" | "active" | "inactive"); setCurrentPage(0); }}><option value="all">{localeField("All statuses", "គ្រប់ស្ថានភាព")}</option><option value="active">{localeField("Active", "សកម្ម")}</option><option value="inactive">{localeField("Inactive", "អសកម្ម")}</option></select><button onClick={() => page.refetch()} className="grid size-9 place-items-center rounded-lg border border-[#E5E7EB] bg-white text-slate-600" title={t("refresh")}><FiRefreshCw/></button></div>
  {error && <p role="alert" className="rounded-lg bg-rose-50 p-3 text-sm text-rose-700">{error}</p>}
  {kind === "roles" && !canManage && <p role="status" className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">{localeField("Read-only access. role.manage is required to create, update, activate, deactivate, or delete roles.", "ការចូលប្រើសម្រាប់មើលតែប៉ុណ្ណោះ។ ត្រូវការសិទ្ធិ role.manage ដើម្បីបង្កើត កែប្រែ បើក បិទ ឬលុបតួនាទី។")}</p>}
  <div className="overflow-x-auto rounded-xl border border-[#E5E7EB] bg-white"><table className="w-full min-w-[700px] text-left text-sm"><thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500"><tr><th className="px-4 py-3">{t("name")}</th>{kind === "users" && <th className="px-4 py-3">{t("email")}</th>}{kind === "permissions" && <><th className="px-4 py-3">{t("resource")}</th><th className="px-4 py-3">{t("action")}</th></>}<th className="px-4 py-3">{t("status")}</th><th className="px-4 py-3 text-right">{t("actions")}</th></tr></thead><tbody className="divide-y divide-slate-100">{page.data?.content.map(item => { const label = item.name ?? item.username ?? "—"; const desc = "description" in item ? String(item.description ?? "") : item.uuid; const permission = item as unknown as Permission; const detailTo = kind === "users" ? `/users/${item.uuid}` : `/roles/${item.uuid}`; return <tr key={item.uuid} className="hover:bg-slate-50/70"><td className="px-4 py-3"><div className="font-medium text-slate-900">{kind !== "permissions" ? <Link className="hover:text-[#534AB7]" to={detailTo}>{label}</Link> : label}</div><div className="max-w-[360px] truncate text-xs text-slate-500">{desc}</div></td>{kind === "users" && <td className="px-4 py-3 text-slate-600">{item.email ?? "—"}</td>}{kind === "permissions" && <><td className="px-4 py-3">{permission.resource}</td><td className="px-4 py-3">{permission.action}</td></>}<td className="px-4 py-3"><span className={`rounded-full px-2.5 py-1 text-xs ${item.enabled ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-600"}`}>{item.enabled ? t("active") : t("disabled")}</span></td><td className="px-4 py-3"><div className="flex justify-end gap-1">{kind === "permissions" && <button title={localeField("View details", "មើលព័ត៌មានលម្អិត")} aria-label={localeField("View permission details", "មើលព័ត៌មានលម្អិតសិទ្ធិ")} className="grid size-8 place-items-center rounded-md text-slate-500 hover:bg-indigo-50 hover:text-[#534AB7]" onClick={() => setViewingPermission(item.uuid)}><FiEye/></button>}{canManage && <><button title={item.enabled ? t("disable") : t("enable")} className="grid size-8 place-items-center rounded-md text-slate-500 hover:bg-indigo-50 hover:text-[#534AB7]" onClick={() => toggle.mutate({ uuid: item.uuid, enabled: item.enabled })}><FiCheck/></button>{kind !== "users" && <button title={t("edit")} className="grid size-8 place-items-center rounded-md text-slate-500 hover:bg-slate-100" onClick={() => { setSelected(item); setModal(true); }}><FiEdit2/></button>}<button title={t("delete")} className="grid size-8 place-items-center rounded-md text-slate-500 hover:bg-rose-50 hover:text-rose-700" onClick={() => window.confirm(t("confirmDelete")) && remove.mutate(item.uuid)}><FiTrash2/></button></>}</div></td></tr>; })}</tbody></table>{page.isLoading && <p className="p-8 text-center text-sm text-slate-500">{t("loading")}</p>}{page.isError && <p className="p-8 text-center text-sm text-rose-700">{t("loadingError")}</p>}{page.data?.content.length === 0 && <p className="p-8 text-center text-sm text-slate-500">{t("noResults")}</p>}</div>
  {page.data?.page && page.data.page.totalPages > 1 && <nav aria-label={localeField("Table pages", "ទំព័រតារាង")} className="flex flex-wrap items-center justify-between gap-3"><p className="text-xs text-slate-500">{localeField("Page", "ទំព័រ")} {page.data.page.number + 1} {localeField("of", "ក្នុងចំណោម")} {page.data.page.totalPages}</p><div className="flex gap-2"><button disabled={page.data.page.first || page.isFetching} onClick={() => setCurrentPage(value => Math.max(0, value - 1))} className="h-9 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm text-slate-700 disabled:cursor-not-allowed disabled:opacity-50">{localeField("Previous", "មុន")}</button><button disabled={page.data.page.last || page.isFetching} onClick={() => setCurrentPage(value => value + 1)} className="h-9 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm text-slate-700 disabled:cursor-not-allowed disabled:opacity-50">{localeField("Next", "បន្ទាប់")}</button></div></nav>}
  {modal && <RecordDialog kind={kind} endpoint={endpoint} selected={selected as (Role | Permission | null)} title={selected ? t("edit") : createTitle} t={t} onClose={() => setModal(false)} onSaved={() => { setModal(false); queryClient.invalidateQueries({ queryKey: [kind] }); }} onError={setError}/>} {kind === "permissions" && viewingPermission && <PermissionDetailsDialog uuid={viewingPermission} locale={locale} onClose={() => setViewingPermission(null)}/>}</section>;
}

function PermissionDetailsDialog({ uuid, locale, onClose }: { uuid: string; locale: Locale; onClose: () => void }) {
  const isKm = locale === "km";
  const query = useQuery({ queryKey: ["permission", uuid], queryFn: async () => (await apiClient.get<Permission>(`/permissions/${uuid}`)).data, enabled: !!uuid });
  const label = (en: string, km: string) => isKm ? km : en;
  const date = (value?: string) => value ? new Intl.DateTimeFormat(isKm ? "km-KH" : "en-US", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : label("Not available", "មិនមានទិន្នន័យ");
  return <div className="fixed inset-0 z-[80] grid place-items-center bg-slate-950/30 p-4" role="presentation" onMouseDown={event => event.target === event.currentTarget && onClose()}><section role="dialog" aria-modal="true" aria-labelledby="permission-detail-title" className="w-full max-w-lg rounded-xl border border-[#E5E7EB] bg-white p-5 shadow-xl"><header className="flex items-center justify-between"><h2 id="permission-detail-title" className="font-semibold">{label("Permission details", "ព័ត៌មានលម្អិតសិទ្ធិ")}</h2><button type="button" aria-label={label("Close", "បិទ")} onClick={onClose}><FiX/></button></header>{query.isLoading ? <p className="py-8 text-center text-sm text-slate-500">{label("Loading permission…", "កំពុងផ្ទុកសិទ្ធិ…")}</p> : query.isError || !query.data ? <p role="alert" className="py-6 text-sm text-rose-700">{label("Unable to load permission details.", "មិនអាចផ្ទុកព័ត៌មានលម្អិតសិទ្ធិបានទេ។")}</p> : <dl className="mt-4 grid gap-4 sm:grid-cols-2">{([["Name", query.data.name], ["Resource", query.data.resource], ["Action", query.data.action], ["Status", query.data.enabled ? label("Active", "សកម្ម") : label("Inactive", "អសកម្ម")], ["System permission", query.data.system ? label("Yes", "បាទ/ចាស") : label("No", "ទេ")], ["Created", date(query.data.createdAt)], ["Updated", date(query.data.updatedAt)]] as [string, string][]) .map(([key, value]) => <div key={key}><dt className="text-xs text-slate-500">{label(key, ({ Name: "ឈ្មោះ", Resource: "ធនធាន", Action: "សកម្មភាព", Status: "ស្ថានភាព", "System permission": "សិទ្ធិប្រព័ន្ធ", Created: "បានបង្កើត", Updated: "បានធ្វើបច្ចុប្បន្នភាព" } as Record<string, string>)[key])}</dt><dd className="mt-1 break-words text-sm font-medium">{value}</dd></div>)}<div className="sm:col-span-2"><dt className="text-xs text-slate-500">{label("Description", "ការពិពណ៌នា")}</dt><dd className="mt-1 whitespace-pre-wrap text-sm">{query.data.description || "—"}</dd></div></dl>}</section></div>;
}
function PermissionRequired({ t, permission }: { t: (key: keyof typeof copy.en) => string; permission: string }) {
  return <div role="alert" className="max-w-xl rounded-xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-950">{t("permissionRequired").replace("{permission}", permission)}</div>;
}
function UsersPage({ t, canRead, canManage }: { t: (key: keyof typeof copy.en) => string; canRead: boolean; canManage: boolean }) {
  if (!canRead) return <PermissionRequired t={t} permission="user.read"/>;
  return <DataPage<User> kind="users" endpoint="/users" t={t} canManage={canManage}/>;
}
function RolesPage({ t, canRead, canManage }: { t: (key: keyof typeof copy.en) => string; canRead: boolean; canManage: boolean }) { return <DataPage<Role> kind="roles" endpoint="/roles" t={t} canRead={canRead} canManage={canManage}/>; }
function PermissionsPage({ t, locale, canRead, canManage }: { t: (key: keyof typeof copy.en) => string; locale: Locale; canRead: boolean; canManage: boolean }) { return <DataPage<Permission> kind="permissions" endpoint="/permissions" t={t} locale={locale} canRead={canRead} canManage={canManage}/>; }

type LinkedIdentity = { uuid: string; provider: string; email?: string; emailVerified: boolean; displayName?: string; linkedAt?: string; lastLoginAt?: string };
function LinkedAccountsPage({ locale }: { locale: Locale }) {
  const isKm = locale === "km";
  const queryClient = useQueryClient();
  const location = useLocation();
  const identities = useQuery({ queryKey: ["me-identities"], queryFn: async () => (await apiClient.get<LinkedIdentity[]>("/me/identities")).data });
  const [message, setMessage] = useState("");
  const link = useMutation({ mutationFn: async (provider: string) => (await apiClient.post<{ authorizationUrl: string }>(`/me/identities/link/${provider}`)).data, onSuccess: result => { setMessage(isKm ? "កំពុងតភ្ជាប់ទៅអ្នកផ្តល់សេវា…" : "Redirecting to the provider…"); window.location.assign(result.authorizationUrl); }, onError: error => setMessage(apiErrorMessage(error, isKm ? "មិនអាចចាប់ផ្តើមការភ្ជាប់បានទេ។" : "Could not start provider linking.")) });
  const unlink = useMutation({ mutationFn: (uuid: string) => apiClient.delete(`/me/identities/${uuid}`), onSuccess: () => { setMessage(isKm ? "បានផ្តាច់គណនី។" : "Account unlinked."); queryClient.invalidateQueries({ queryKey: ["me-identities"] }); }, onError: error => setMessage(apiErrorMessage(error, isKm ? "មិនអាចផ្តាច់គណនីបានទេ។" : "Could not unlink this account.")) });
  useEffect(() => {
    const result = new URLSearchParams(location.search).get("link");
    if (result === "success" || result === "failed") {
      setMessage(result === "success"
        ? (isKm ? "បានភ្ជាប់គណនីដោយជោគជ័យ។" : "Account linked successfully.")
        : (isKm ? "ការភ្ជាប់គណនីត្រូវបានបរាជ័យ។ សូមព្យាយាមម្តងទៀត។" : "Account linking failed. Please try again."));
      if (result === "success") void queryClient.invalidateQueries({ queryKey: ["me-identities"] });
      window.history.replaceState({}, "", location.pathname);
    }
  }, [isKm, location.pathname, location.search, queryClient]);
  const providers = [
    { id: "google", name: "Google", icon: <FaGoogle aria-hidden="true"/> },
    { id: "github", name: "GitHub", icon: <FaGithub aria-hidden="true"/> },
    { id: "facebook", name: "Facebook", icon: <FaFacebookF aria-hidden="true"/> },
    { id: "telegram", name: "Telegram", icon: <FaTelegramPlane aria-hidden="true"/> },
  ];
  const date = (value?: string) => value ? new Intl.DateTimeFormat(isKm ? "km-KH" : "en-US", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : (isKm ? "មិនមានទិន្នន័យ" : "Not available");
  return <section className="max-w-4xl space-y-5"><div><p className="text-xs font-semibold uppercase tracking-[.14em] text-[#534AB7]">{isKm ? "គណនី" : "ACCOUNT"}</p><h1 className="mt-1 text-xl font-semibold">{isKm ? "គណនីដែលបានភ្ជាប់" : "Linked accounts"}</h1><p className="mt-1 text-sm text-slate-500">{isKm ? "មើល និងគ្រប់គ្រងអត្តសញ្ញាណពីអ្នកផ្តល់សេវាខាងក្រៅ។" : "Review and manage external identities linked to your IAM account."}</p></div>{message && <p role={link.isError || unlink.isError ? "alert" : "status"} className="rounded-lg border border-slate-200 bg-white px-4 py-3 text-sm text-slate-700">{message}</p>}{identities.isLoading && <div className="rounded-xl border border-[#E5E7EB] bg-white p-5 text-sm text-slate-500">{isKm ? "កំពុងផ្ទុក…" : "Loading linked accounts…"}</div>}{identities.isError && <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">{isKm ? "មិនអាចផ្ទុកគណនីដែលបានភ្ជាប់បានទេ។" : "Unable to load linked accounts. Check your access and try again."}</div>}{identities.data && identities.data.length === 0 && <div className="rounded-xl border border-dashed border-[#CBD5E1] bg-white p-8 text-center"><div className="mx-auto grid size-10 place-items-center rounded-full bg-[#EEECFF] text-[#534AB7]"><FiUsers aria-hidden="true"/></div><h2 className="mt-3 text-sm font-semibold">{isKm ? "មិនទាន់មានគណនីភ្ជាប់" : "No linked accounts"}</h2><p className="mx-auto mt-1 max-w-md text-sm leading-6 text-slate-500">{isKm ? "មិនមានអត្តសញ្ញាណពីអ្នកផ្តល់សេវាខាងក្រៅត្រូវបានភ្ជាប់ទេ។" : "There are no external provider identities linked to this account."}</p></div>}{identities.data && identities.data.length > 0 && <div className="overflow-hidden rounded-xl border border-[#E5E7EB] bg-white"><div className="divide-y divide-slate-100">{identities.data.map(identity => <article className="flex flex-col gap-4 p-5 sm:flex-row sm:items-center sm:justify-between" key={identity.uuid}><div className="flex min-w-0 items-start gap-3"><span className="grid size-10 shrink-0 place-items-center rounded-lg bg-[#EEECFF] text-sm font-semibold text-[#534AB7]">{identity.provider.slice(0, 1)}</span><div className="min-w-0"><h2 className="text-sm font-semibold text-slate-900">{identity.displayName || identity.provider}</h2><p className="mt-0.5 truncate text-sm text-slate-600">{identity.email || identity.provider}</p><div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-slate-500"><span>{isKm ? "បានភ្ជាប់៖ " : "Linked: "}{date(identity.linkedAt)}</span>{identity.lastLoginAt && <span>{isKm ? "ចូលចុងក្រោយ៖ " : "Last used: "}{date(identity.lastLoginAt)}</span>}<span className={identity.emailVerified ? "text-emerald-700" : "text-amber-700"}>{identity.emailVerified ? (isKm ? "អ៊ីមែលបានផ្ទៀងផ្ទាត់" : "Email verified") : (isKm ? "មិនទាន់ផ្ទៀងផ្ទាត់អ៊ីមែល" : "Email not verified")}</span></div></div></div><button disabled={unlink.isPending} className="h-9 shrink-0 self-start rounded-lg border border-rose-200 px-3 text-sm font-medium text-rose-700 hover:bg-rose-50 disabled:opacity-50 sm:self-center" onClick={() => window.confirm(isKm ? "តើអ្នកចង់ផ្តាច់អត្តសញ្ញាណនេះមែនទេ?" : "Unlink this identity from your account?") && unlink.mutate(identity.uuid)} type="button">{isKm ? "ផ្តាច់គណនី" : "Unlink"}</button></article>)}</div></div>}<section className="rounded-xl border border-[#E5E7EB] bg-white p-5"><div><h2 className="text-sm font-semibold">{isKm ? "ភ្ជាប់អ្នកផ្តល់សេវា" : "Link another provider"}</h2><p className="mt-1 text-sm text-slate-500">{isKm ? "ចូលទៅកាន់អ្នកផ្តល់សេវា ដើម្បីផ្ទៀងផ្ទាត់ និងភ្ជាប់ទៅគណនីនេះ។" : "Authenticate with a provider to link it to this signed-in account."}</p></div><div className="mt-4 flex flex-wrap gap-2">{providers.map(provider => <button key={provider.id} type="button" disabled={link.isPending} onClick={() => link.mutate(provider.id)} className="inline-flex h-9 items-center gap-2 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm font-medium text-slate-700 hover:border-[#534AB7] hover:text-[#534AB7] disabled:opacity-50">{provider.icon}{link.isPending ? (isKm ? "កំពុងតភ្ជាប់…" : "Starting…") : provider.name}</button>)}</div></section></section>;
}

function AccountDeactivationPage({ user, locale, onLogout }: { user: Me; locale: Locale; onLogout: () => void }) {
  const isKm = locale === "km";
  const [error, setError] = useState("");
  const deactivate = useMutation({
    mutationFn: () => apiClient.post("/me/deactivate"),
    onSuccess: () => {
      window.alert(isKm ? "គណនីត្រូវបានបិទ។ អ្នកនឹងត្រូវចាកចេញពីប្រព័ន្ធ។" : "Your account has been disabled. You will now be signed out.");
      onLogout();
    },
    onError: () => setError(isKm ? "មិនអាចបិទគណនីបានទេ។ សូមទាក់ទងអ្នកគ្រប់គ្រង។" : "We could not disable your account. Contact an administrator for help."),
  });
  return <section className="max-w-2xl space-y-5">
    <div><p className="text-xs font-semibold uppercase tracking-[.14em] text-rose-700">{isKm ? "សុវត្ថិភាពគណនី" : "ACCOUNT SECURITY"}</p><h1 className="mt-1 text-xl font-semibold">{isKm ? "បិទគណនី" : "Deactivate account"}</h1><p className="mt-1 text-sm text-slate-500">{isKm ? "បិទគណនី IAM របស់អ្នកដោយប្រើសេវាកម្មបិទគណនីដែលមានស្រាប់។" : "Disable your IAM account using the existing account status operation."}</p></div>
    <div className="rounded-xl border border-rose-200 bg-white p-5"><div className="flex gap-3"><span className="grid size-9 shrink-0 place-items-center rounded-lg bg-rose-50 text-rose-700"><FiAlertCircle aria-hidden="true"/></span><div>
      <h2 className="text-sm font-semibold text-slate-900">{isKm ? "ការបិទមានប្រសិទ្ធភាពភ្លាមៗ" : "This takes effect immediately"}</h2>
      <p className="mt-1 text-sm leading-6 text-slate-600">{isKm ? "គណនីរបស់អ្នកនឹងត្រូវបានបិទ ហើយកម្មវិធីនឹងចាកចេញពីប្រព័ន្ធ។ អ្នកនឹងមិនអាចចូលប្រើរហូតដល់អ្នកគ្រប់គ្រងបើកគណនីឡើងវិញ។" : "Your account will be disabled and this browser will sign out. You will not be able to sign in again unless an administrator re-enables your account."}</p>
      <p className="mt-2 text-xs leading-5 text-emerald-800">{isKm ? "សម័យប្រើប្រាស់ទាំងអស់នឹងត្រូវបានដកហូត ហើយអ្នកនឹងត្រូវចូលគណនីឡើងវិញ ប្រសិនបើគណនីត្រូវបានបើកសារជាថ្មី។" : "All active sessions will be revoked. If an administrator re-enables your account, you will need to sign in again."}</p>
      <p className="mt-3 text-xs text-slate-500">{isKm ? "គណនី៖ " : "Account: "}<span className="font-medium text-slate-700">{user.username}</span></p>
      {error && <p role="alert" className="mt-3 text-sm text-rose-700">{error}</p>}
      <button disabled={!user.enabled || deactivate.isPending} className="mt-4 h-10 rounded-lg bg-rose-700 px-4 text-sm font-semibold text-white hover:bg-rose-800 disabled:cursor-not-allowed disabled:opacity-50" onClick={() => { if (window.confirm(isKm ? "តើអ្នកប្រាកដថាចង់បិទគណនីនេះមែនទេ?" : "Are you sure you want to disable your account?")) deactivate.mutate(); }} type="button">{deactivate.isPending ? (isKm ? "កំពុងបិទ…" : "Deactivating…") : user.enabled ? (isKm ? "បិទគណនីរបស់ខ្ញុំ" : "Deactivate my account") : (isKm ? "គណនីត្រូវបានបិទ" : "Account is disabled")}</button>
    </div></div></div>
    <p className="text-xs leading-5 text-slate-500">{isKm ? "API បច្ចុប្បន្នបិទគណនីភ្លាមៗ ហើយមិនមានដំណើរការស្នើសុំឱ្យអនុម័តទេ។" : "The current API disables the account immediately; it does not create an approval request."}</p>
  </section>;
}

function RoleDetail({ t, canRead, canAssignPermissions, canReadPermissions }: { t: (key: keyof typeof copy.en) => string; canRead: boolean; canAssignPermissions: boolean; canReadPermissions: boolean }) {
  const { uuid = "" } = useParams();
  const [permissionUuid, setPermissionUuid] = useState("");
  const [error, setError] = useState("");
  const queryClient = useQueryClient();
  const role = useQuery({ queryKey: ["role", uuid], queryFn: async () => (await apiClient.get<Role>(`/roles/${uuid}`)).data, enabled: !!uuid && canRead });
  const all = useQuery({ queryKey: ["permissions", "all"], queryFn: async () => (await apiClient.get<Page<Permission>>("/permissions", { params: { page: 0, size: 100 } })).data, enabled: canRead && canAssignPermissions && canReadPermissions });
  const assigned = useQuery({ queryKey: ["role-permission-assignments", uuid], queryFn: async () => (await apiClient.get<{ permissionUuid: string; permissionName: string; resource: string; action: string }[]>(`/roles/${uuid}/permissions`)).data, enabled: !!uuid && canRead });
  const save = useMutation({ mutationFn: () => apiClient.post(`/roles/${uuid}/permissions`, { permissionUuids: [permissionUuid] }), onSuccess: () => { setPermissionUuid(""); setError(localeField("Permission assigned.", "បានផ្តល់សិទ្ធិ។")); queryClient.invalidateQueries({ queryKey: ["role-permission-assignments", uuid] }); }, onError: () => setError(t("saveError")) });
  const revoke = useMutation({ mutationFn: (permissionId: string) => apiClient.delete(`/roles/${uuid}/permissions/${permissionId}`), onSuccess: () => { setError(localeField("Permission removed.", "បានដកសិទ្ធិចេញ។")); queryClient.invalidateQueries({ queryKey: ["role-permission-assignments", uuid] }); }, onError: () => setError(t("saveError")) });
  if (!canRead) return <PermissionRequired t={t} permission="role.read"/>;
  if (role.isLoading) return <p>{t("loading")}</p>;
  if (!role.data) return <p role="alert">{t("loadingError")}</p>;
  const available = (all.data?.content ?? []).filter(p => p.enabled && !assigned.data?.some(existing => existing.permissionUuid === p.uuid));
  const canAssign = canAssignPermissions && canReadPermissions;
  const missingPermissionAuthorities = [!canAssignPermissions ? "role.permission.assign" : null, !canReadPermissions ? "permission.read" : null].filter((permission): permission is string => permission !== null);
  return <div className="space-y-5">
    <div><Link className="text-xs text-[#534AB7]" to="/roles">← {t("roles")}</Link><h1 className="mt-2 text-xl font-semibold">{role.data.name}</h1><p className="text-sm text-slate-500">{role.data.description || "—"}</p></div>
    {error && <p role="status" className={`text-sm ${error === t("saveError") ? "text-rose-700" : "text-emerald-700"}`}>{error}</p>}
    <section className="rounded-xl border border-[#E5E7EB] bg-white">
      <header className="border-b border-slate-100 px-5 py-4"><h2 className="font-semibold">{localeField("Assigned permissions", "សិទ្ធិដែលបានផ្តល់")}</h2></header>
      {assigned.isLoading ? <p className="p-4 text-sm text-slate-500">{t("loading")}</p> : assigned.data?.length ? <div className="divide-y divide-slate-100">{assigned.data.map(permission => <div key={permission.permissionUuid} className="flex items-center justify-between gap-3 px-4 py-3"><div><p className="text-sm font-medium">{permission.permissionName}</p><p className="text-xs text-slate-500">{permission.resource}:{permission.action}</p></div>{canAssignPermissions && <button type="button" disabled={revoke.isPending} className="text-xs text-rose-700 hover:underline" onClick={() => window.confirm(localeField("Remove this permission from the role?", "តើដកសិទ្ធិនេះចេញពីតួនាទីឬ?")) && revoke.mutate(permission.permissionUuid)}>{localeField("Remove", "ដកចេញ")}</button>}</div>)}</div> : <p className="p-4 text-sm text-slate-500">{t("noResults")}</p>}
      {canAssign ? <div className="flex flex-col gap-2 border-t border-slate-100 p-4 sm:flex-row"><select className="h-10 min-w-0 flex-1 rounded-lg border border-[#E5E7EB] px-3 text-sm" value={permissionUuid} onChange={e => setPermissionUuid(e.target.value)}><option value="">{t("assignPermission")}</option>{available.map(permission => <option key={permission.uuid} value={permission.uuid}>{permission.name} · {permission.resource}:{permission.action}</option>)}</select><button disabled={!permissionUuid || save.isPending} className="h-10 rounded-lg bg-[#534AB7] px-4 text-sm font-medium text-white disabled:opacity-50" onClick={() => save.mutate()}>{save.isPending ? t("loading") : t("assignPermission")}</button></div> : <p className="border-t border-slate-100 p-4 text-xs text-slate-500">{localeField("Missing required permission(s): ", "ខ្វះសិទ្ធិដែលត្រូវការ៖ ")}{missingPermissionAuthorities.join(", ")}</p>}
      {(all.isError || assigned.isError) && <p role="alert" className="px-4 pb-4 text-sm text-rose-700">{t("loadingError")}</p>}
    </section>
  </div>;
}

function AccountProfile({ me, locale }: { me: Me; locale: Locale }) {
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ["me-profile", me.uuid], queryFn: async () => (await apiClient.get<User>("/me")).data });
  const [message, setMessage] = useState("");
  const update = useMutation({ mutationFn: (body: Record<string, string>) => apiClient.put("/me", body), onSuccess: () => { setMessage(locale === "km" ? "បានធ្វើបច្ចុប្បន្នភាពប្រវត្តិរូប។" : "Profile updated."); qc.invalidateQueries({ queryKey: ["me-profile", me.uuid] }); qc.invalidateQueries({ queryKey: ["me"] }); }, onError: (error: unknown) => { const data = axios.isAxiosError(error) ? error.response?.data as { detail?: string; errors?: Record<string, string> } | undefined : undefined; const validation = data?.errors ? Object.entries(data.errors).map(([field, detail]) => `${field}: ${detail}`).join(" · ") : undefined; setMessage(validation || data?.detail || (locale === "km" ? "មិនអាចរក្សាទុកបានទេ។" : "Profile update failed.")); } });
  const data: User = q.data ?? me;
  const fields: [string, keyof User, string][] = locale === "km"
    ? [["ឈ្មោះអ្នកប្រើ", "username", "text"], ["អ៊ីមែល", "email", "email"], ["នាមខ្លួន", "givenName", "text"], ["នាមត្រកូល", "familyName", "text"], ["នាមកណ្តាល", "middleName", "text"], ["ឈ្មោះហៅក្រៅ", "nickname", "text"], ["ភេទ", "gender", "text"], ["លេខទូរស័ព្ទ", "phoneNumber", "tel"], ["URL រូបភាពប្រវត្តិរូប", "pictureUrl", "url"]]
    : [["Username", "username", "text"], ["Email", "email", "email"], ["Given name", "givenName", "text"], ["Family name", "familyName", "text"], ["Middle name", "middleName", "text"], ["Nickname", "nickname", "text"], ["Gender", "gender", "text"], ["Phone number", "phoneNumber", "tel"], ["Profile picture URL", "pictureUrl", "url"]];
  const displayName = [data.givenName, data.familyName].filter(Boolean).join(" ") || data.username;
  return <section className="max-w-3xl rounded-xl border border-[#E5E7EB] bg-white">
    <header className="flex items-center gap-4 border-b border-slate-100 px-5 py-4">
      {data.pictureUrl ? <img alt={displayName} className="size-12 rounded-full border border-[#E5E7EB] object-cover" referrerPolicy="no-referrer" src={data.pictureUrl}/> : <span aria-hidden="true" className="grid size-12 place-items-center rounded-full bg-[#EEECFF] text-sm font-semibold text-[#534AB7]">{Array.from(displayName)[0]?.toLocaleUpperCase(locale === "km" ? "km-KH" : "en-US") ?? "N"}</span>}
      <div><h1 className="text-xl font-semibold">{locale === "km" ? "ប្រវត្តិរូបរបស់ខ្ញុំ" : "My profile"}</h1><p className="mt-1 text-sm text-slate-500">{locale === "km" ? "កែព័ត៌មានដែលអាចធ្វើបច្ចុប្បន្នភាពបាន។" : "Edit the profile fields supported by the IAM API."}</p></div>
    </header>
    {q.isLoading ? <p className="p-5 text-sm text-slate-500">{locale === "km" ? "កំពុងផ្ទុក…" : "Loading…"}</p> : <form className="grid gap-4 p-5 sm:grid-cols-2" onSubmit={e => { e.preventDefault(); const fd = new FormData(e.currentTarget); update.mutate({ username: String(fd.get("username") ?? ""), email: String(fd.get("email") ?? ""), givenName: String(fd.get("givenName") ?? ""), familyName: String(fd.get("familyName") ?? ""), middleName: String(fd.get("middleName") ?? ""), nickname: String(fd.get("nickname") ?? ""), gender: String(fd.get("gender") ?? ""), phoneNumber: String(fd.get("phoneNumber") ?? ""), pictureUrl: String(fd.get("pictureUrl") ?? "") }); }}>
      {fields.map(([label, key, type]) => <label className="text-sm font-medium text-slate-700" key={key}>{label}<input name={key} type={type} required={key === "username"} maxLength={key === "username" ? 100 : key === "email" ? 150 : undefined} defaultValue={String(data[key] ?? "")} className="mt-1.5 h-10 w-full rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm outline-none focus:border-[#534AB7] focus:ring-2 focus:ring-[#EEECFF]"/></label>)}
      <p className="text-xs leading-5 text-slate-500 sm:col-span-2">{locale === "km" ? "API អនុញ្ញាតឱ្យរក្សាទុក URL រូបភាពប៉ុណ្ណោះ មិនមានការផ្ទុកឯកសាររូបភាពទេ។" : "The API stores a picture URL; image file uploads are not supported."}</p>
      <div className="flex flex-wrap items-center gap-3 sm:col-span-2"><button disabled={update.isPending || q.isError} className="h-10 rounded-lg bg-[#534AB7] px-4 text-sm font-medium text-white hover:bg-[#43389B] disabled:opacity-50">{locale === "km" ? "រក្សាទុក" : "Save changes"}</button>{message && <p role={update.isError ? "alert" : "status"} className={`text-sm ${update.isError ? "text-rose-700" : "text-slate-600"}`}>{message}</p>}{q.isError && <p role="alert" className="text-sm text-rose-700">{locale === "km" ? "មិនអាចផ្ទុកប្រវត្តិរូបបានទេ។" : "Could not load profile."}</p>}</div>
    </form>}
  </section>;
}

function PasswordPage({ locale }: { locale: Locale }) {
  const [message, setMessage] = useState("");
  const mutation = useMutation({ mutationFn: (values: { currentPassword: string; newPassword: string; confirmPassword: string }) => apiClient.put("/me/password", values), onSuccess: () => { setMessage(locale === "km" ? "បានប្តូរពាក្យសម្ងាត់។" : "Password changed successfully."); }, onError: () => setMessage(locale === "km" ? "មិនអាចប្តូរពាក្យសម្ងាត់បានទេ។" : "Password change failed. Verify your current password and try again.") });
  return <section className="max-w-xl rounded-xl border border-[#E5E7EB] bg-white p-5 sm:p-6"><h1 className="text-xl font-semibold">{locale === "km" ? "ប្តូរពាក្យសម្ងាត់" : "Change password"}</h1><p className="mt-1 text-sm text-slate-500">{locale === "km" ? "ត្រូវការពាក្យសម្ងាត់បច្ចុប្បន្ន។" : "Confirm your current password to set a new one."}</p><form className="mt-5 space-y-4" onSubmit={e=>{e.preventDefault(); const f=new FormData(e.currentTarget); const next=String(f.get("newPassword")); const confirm=String(f.get("confirmPassword")); if(next!==confirm){setMessage(locale==="km"?"ពាក្យសម្ងាត់មិនត្រូវគ្នា។":"New password and confirmation do not match.");return;} mutation.mutate({currentPassword:String(f.get("currentPassword")),newPassword:next,confirmPassword:confirm});}}><Field label={locale==="km"?"ពាក្យសម្ងាត់បច្ចុប្បន្ន":"Current password"} name="currentPassword" type="password" required/><Field label={locale==="km"?"ពាក្យសម្ងាត់ថ្មី":"New password"} name="newPassword" type="password" required/><Field label={locale==="km"?"បញ្ជាក់ពាក្យសម្ងាត់ថ្មី":"Confirm new password"} name="confirmPassword" type="password" required/><button disabled={mutation.isPending} className="h-10 rounded-lg bg-[#534AB7] px-4 text-sm font-medium text-white hover:bg-[#43389B] disabled:opacity-60">{locale==="km"?"រក្សាទុកពាក្យសម្ងាត់":"Update password"}</button>{message&&<p role="status" className="text-sm text-slate-600">{message}</p>}</form></section>;
}

type ManagedClient = { clientId: string; clientName: string; clientAuthenticationMethods: string[]; authorizationGrantTypes: string[]; redirectUris: string[]; postLogoutRedirectUris: string[]; scopes: string[]; active: boolean; requireAuthorizationConsent: boolean; requireProofKey: boolean; accessTokenTtlSeconds: number; refreshTokenTtlSeconds: number; reuseRefreshTokens: boolean; clientIdIssuedAt?: string };
type ManagedClientPage = { content: ManagedClient[]; page: { number: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean } };
type IssuedClientSecret = { client: ManagedClient; clientSecret: string };
const managedClientSchema = z.object({
  clientName: z.string().trim().min(1).max(200),
  clientAuthenticationMethods: z.array(z.enum(["client_secret_basic", "client_secret_post"])).min(1),
  authorizationGrantTypes: z.array(z.enum(["authorization_code", "refresh_token", "client_credentials"])).min(1),
  redirectUris: z.string(), postLogoutRedirectUris: z.string(), scopes: z.string().min(1),
  requireAuthorizationConsent: z.boolean(), requireProofKey: z.boolean(),
  accessTokenTtlSeconds: z.number().int().min(60).max(3600),
  refreshTokenTtlSeconds: z.number().int().min(300).max(31536000), reuseRefreshTokens: z.boolean(),
});
type ManagedClientForm = z.infer<typeof managedClientSchema>;
const emptyManagedClient: ManagedClientForm = { clientName: "", clientAuthenticationMethods: ["client_secret_basic"], authorizationGrantTypes: ["authorization_code", "refresh_token"], redirectUris: "", postLogoutRedirectUris: "", scopes: "openid profile email", requireAuthorizationConsent: true, requireProofKey: true, accessTokenTtlSeconds: 600, refreshTokenTtlSeconds: 2592000, reuseRefreshTokens: true };

function OAuthClientAdminPage({ locale, canRead, canManage, canRegister }: { locale: Locale; canRead: boolean; canManage: boolean; canRegister: boolean }) {
  const km = locale === "km"; const label = (en: string, kh: string) => km ? kh : en;
  const queryClient = useQueryClient(); const [search, setSearch] = useState(""); const [pageNumber, setPageNumber] = useState(0);
  const [selectedId, setSelectedId] = useState<string | null>(null); const [dialog, setDialog] = useState<"detail" | "create" | "edit" | null>(null);
  const [oneTimeSecret, setOneTimeSecret] = useState<IssuedClientSecret | null>(null); const [error, setError] = useState("");
  const form = useForm<ManagedClientForm>({ resolver: zodResolver(managedClientSchema), defaultValues: emptyManagedClient });
  const list = useQuery({ queryKey: ["oauth-admin-clients", search, pageNumber], queryFn: async () => (await apiClient.get<ManagedClientPage>("/admin/clients", { params: { keyword: search || undefined, page: pageNumber, size: 10, sortBy: "clientName", sortDirection: "ASC" } })).data, enabled: canRead });
  const detail = useQuery({ queryKey: ["oauth-admin-client", selectedId], queryFn: async () => (await apiClient.get<ManagedClient>(`/admin/clients/${encodeURIComponent(selectedId!)}`)).data, enabled: canRead && !!selectedId && (dialog === "detail" || dialog === "edit") });
  useEffect(() => {
    if (dialog === "edit" && detail.data && detail.data.clientId === selectedId) form.reset({
      clientName: detail.data.clientName, clientAuthenticationMethods: detail.data.clientAuthenticationMethods as ManagedClientForm["clientAuthenticationMethods"],
      authorizationGrantTypes: detail.data.authorizationGrantTypes as ManagedClientForm["authorizationGrantTypes"],
      redirectUris: detail.data.redirectUris.join("\n"), postLogoutRedirectUris: detail.data.postLogoutRedirectUris.join("\n"), scopes: detail.data.scopes.join(" "),
      requireAuthorizationConsent: detail.data.requireAuthorizationConsent, requireProofKey: detail.data.requireProofKey,
      accessTokenTtlSeconds: detail.data.accessTokenTtlSeconds, refreshTokenTtlSeconds: detail.data.refreshTokenTtlSeconds, reuseRefreshTokens: detail.data.reuseRefreshTokens,
    });
  }, [dialog, detail.data, selectedId, form]);
  const invalidate = () => { void queryClient.invalidateQueries({ queryKey: ["oauth-admin-clients"] }); if (selectedId) void queryClient.invalidateQueries({ queryKey: ["oauth-admin-client", selectedId] }); };
  const save = useMutation({ mutationFn: async (values: ManagedClientForm) => {
    const body = { ...values, redirectUris: lines(values.redirectUris), postLogoutRedirectUris: lines(values.postLogoutRedirectUris), scopes: values.scopes.split(/[\s,]+/).map(value => value.trim()).filter(Boolean) };
    return dialog === "edit" && selectedId ? (await apiClient.put<ManagedClient>(`/admin/clients/${encodeURIComponent(selectedId)}`, body)).data : (await apiClient.post<IssuedClientSecret>("/admin/clients", body)).data;
  }, onSuccess: (value: ManagedClient | IssuedClientSecret) => { setError(""); setDialog(null); if ("clientSecret" in value) setOneTimeSecret(value); invalidate(); }, onError: error => setError(apiErrorMessage(error, label("Unable to save client configuration.", "មិនអាចរក្សាទុកការកំណត់ Client បានទេ។"))) });
  const deactivate = useMutation({ mutationFn: (id: string) => apiClient.delete(`/admin/clients/${encodeURIComponent(id)}`), onSuccess: () => { setError(""); setDialog(null); invalidate(); }, onError: error => setError(apiErrorMessage(error, label("Could not deactivate client.", "មិនអាចបិទ Client បានទេ។"))) });
  const activate = useMutation({ mutationFn: (id: string) => apiClient.put(`/admin/clients/${encodeURIComponent(id)}/activate`), onSuccess: invalidate, onError: error => setError(apiErrorMessage(error, label("Could not activate client.", "មិនអាចបើក Client បានទេ។"))) });
  const rotate = useMutation({ mutationFn: async (id: string) => (await apiClient.post<IssuedClientSecret>(`/admin/clients/${encodeURIComponent(id)}/rotate-secret`)).data, onSuccess: value => { setOneTimeSecret(value); invalidate(); }, onError: error => setError(apiErrorMessage(error, label("Could not rotate client secret.", "មិនអាចប្តូរ Secret របស់ Client បានទេ។"))) });
  const openCreate = () => { setError(""); setSelectedId(null); form.reset(emptyManagedClient); setDialog("create"); };
  const openDetail = (id: string) => { setSelectedId(id); setDialog("detail"); };
  const openEdit = (id: string) => { setError(""); setSelectedId(id); setDialog("edit"); };
  const closeDialog = () => { setDialog(null); setSelectedId(null); setError(""); };
  const copySecret = async () => { if (!oneTimeSecret) return; try { await navigator.clipboard.writeText(oneTimeSecret.clientSecret); } catch { setError(label("Clipboard access was denied. Select and copy the secret manually.", "មិនអាចចូលប្រើ Clipboard បានទេ។ សូមជ្រើសរើស និងចម្លង Secret ដោយដៃ។")); } };
  if (!canRead) return <section role="alert" className="max-w-2xl rounded-xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-950">{label("The client inventory requires client.read. Client management actions require client.manage.", "ការមើលបញ្ជី Client ត្រូវការ client.read។ សកម្មភាពគ្រប់គ្រងត្រូវការ client.manage។")}</section>;
  return <section className="space-y-5">
    <header className="flex flex-wrap items-start justify-between gap-3"><div><p className="text-xs font-semibold uppercase tracking-[.14em] text-[#534AB7]">OAuth2 / OpenID Connect</p><h1 className="mt-1 text-xl font-semibold">{label("OAuth clients", "OAuth Clients")}</h1><p className="mt-1 text-sm text-slate-500">{label("Manage registered clients and their authorization configuration.", "គ្រប់គ្រង Client ដែលបានចុះឈ្មោះ និងការកំណត់ Authorization។")}</p></div><div className="flex gap-2">{canRegister && <Link className="inline-flex h-9 items-center rounded-lg border border-[#E5E7EB] px-3 text-sm" to="/clients/register">{label("Dynamic registration", "ការចុះឈ្មោះ Dynamic")}</Link>}{canManage && <Button type="button" onClick={openCreate}>{label("Create client", "បង្កើត Client")}</Button>}</div></header>
    {!canManage && <p role="status" className="rounded-lg border border-amber-200 bg-amber-50 p-3 text-sm text-amber-900">{label("Read-only client access. client.manage is required to create, edit, rotate, activate, or deactivate clients.", "អាចមើល Client បានតែប៉ុណ្ណោះ។ ត្រូវការ client.manage ដើម្បីបង្កើត កែប្រែ ប្តូរ Secret បើក ឬបិទ Client។")}</p>}
    {error && <p role="alert" className="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800">{error}</p>}
    {oneTimeSecret && <section role="status" className="rounded-xl border border-emerald-200 bg-white p-5"><div className="flex flex-wrap items-center justify-between gap-2"><h2 className="font-semibold text-emerald-900">{label("Save this client secret now", "រក្សាទុក Client Secret ឥឡូវនេះ")}</h2><button type="button" className="rounded-lg border px-3 py-1.5 text-sm" onClick={() => setOneTimeSecret(null)}>{label("Dismiss", "បិទ")}</button></div><p className="mt-1 text-sm text-slate-600">{label("This secret is shown only once. It cannot be retrieved later.", "Secret នេះបង្ហាញតែម្តងប៉ុណ្ណោះ ហើយមិនអាចទាញយកម្តងទៀតបានទេ។")}</p><p className="mt-3 text-xs text-slate-500">{label("Client ID", "លេខសម្គាល់ Client")}: <code>{oneTimeSecret.client.clientId}</code></p><div className="mt-2 flex gap-2"><code className="min-w-0 flex-1 break-all rounded-lg bg-slate-50 p-3 text-sm">{oneTimeSecret.clientSecret}</code><Button type="button" variant="outline" onClick={copySecret}>{label("Copy secret", "ចម្លង Secret")}</Button></div></section>}
    <div className="flex gap-2"><input value={search} onChange={event => { setSearch(event.target.value); setPageNumber(0); }} placeholder={label("Search client ID or name", "ស្វែងរកលេខសម្គាល់ ឬឈ្មោះ Client")} className="h-10 min-w-0 flex-1 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm"/><Button type="button" variant="outline" disabled={list.isFetching} onClick={() => void list.refetch()}>{label("Refresh", "ផ្ទុកឡើងវិញ")}</Button></div>
    {list.isLoading ? <div className="rounded-xl border bg-white p-8 text-center text-sm text-slate-500">{label("Loading clients…", "កំពុងផ្ទុក Client…")}</div> : list.isError ? <p role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">{label("Unable to load clients. Check client.read access and try again.", "មិនអាចផ្ទុក Client បានទេ។ សូមពិនិត្យសិទ្ធិ client.read ហើយព្យាយាមម្ដងទៀត។")}</p> : list.data?.content.length === 0 ? <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center text-sm text-slate-500">{label("No registered clients found.", "រកមិនឃើញ Client ដែលបានចុះឈ្មោះទេ។")}</div> : <div className="overflow-x-auto rounded-xl border border-[#E5E7EB] bg-white"><table className="w-full min-w-[760px] text-left text-sm"><thead className="bg-slate-50 text-xs uppercase text-slate-500"><tr><th className="px-4 py-3">{label("Client", "Client")}</th><th className="px-4 py-3">{label("Authentication", "ការផ្ទៀងផ្ទាត់")}</th><th className="px-4 py-3">{label("Grants", "Grant")}</th><th className="px-4 py-3">{label("Status", "ស្ថានភាព")}</th><th className="px-4 py-3 text-right">{label("Actions", "សកម្មភាព")}</th></tr></thead><tbody className="divide-y divide-slate-100">{list.data?.content.map(client => <tr key={client.clientId}><td className="px-4 py-3"><button type="button" className="font-medium text-[#534AB7] hover:underline" onClick={() => openDetail(client.clientId)}>{client.clientName}</button><p className="mt-1 break-all font-mono text-xs text-slate-500">{client.clientId}</p></td><td className="px-4 py-3 text-xs">{client.clientAuthenticationMethods.join(", ")}</td><td className="px-4 py-3 text-xs">{client.authorizationGrantTypes.join(", ")}</td><td className="px-4 py-3"><span className={`rounded-full px-2 py-1 text-xs ${client.active ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-600"}`}>{client.active ? label("Active", "សកម្ម") : label("Inactive", "អសកម្ម")}</span></td><td className="px-4 py-3"><div className="flex justify-end gap-1">{canManage && <><button type="button" className="rounded-md px-2 py-1 text-xs hover:bg-slate-100" onClick={() => openEdit(client.clientId)}>{label("Edit", "កែប្រែ")}</button>{client.active ? <><button type="button" className="rounded-md px-2 py-1 text-xs hover:bg-slate-100" onClick={() => { setSelectedId(client.clientId); rotate.mutate(client.clientId); }}>{label("Rotate secret", "ប្តូរ Secret")}</button><button type="button" className="rounded-md px-2 py-1 text-xs text-rose-700 hover:bg-rose-50" onClick={() => window.confirm(label("Deactivate this client? New client authentication and authorization requests will be rejected.", "បិទ Client នេះឬ? ការផ្ទៀងផ្ទាត់ និងសំណើ Authorization ថ្មីនឹងត្រូវបដិសេធ។")) && deactivate.mutate(client.clientId)}>{label("Deactivate", "បិទ")}</button></> : <button type="button" className="rounded-md px-2 py-1 text-xs hover:bg-slate-100" onClick={() => activate.mutate(client.clientId)}>{label("Activate", "បើក")}</button>}</>}</div></td></tr>)}</tbody></table></div>}
    {list.data?.page && list.data.page.totalPages > 1 && <nav className="flex items-center justify-between gap-3"><p className="text-xs text-slate-500">{label("Page", "ទំព័រ")} {list.data.page.number + 1} {label("of", "ក្នុងចំណោម")} {list.data.page.totalPages} · {list.data.page.totalElements}</p><div className="flex gap-2"><Button type="button" variant="outline" disabled={list.data.page.first || list.isFetching} onClick={() => setPageNumber(value => Math.max(0, value - 1))}>{label("Previous", "មុន")}</Button><Button type="button" variant="outline" disabled={list.data.page.last || list.isFetching} onClick={() => setPageNumber(value => value + 1)}>{label("Next", "បន្ទាប់")}</Button></div></nav>}
    {dialog && <div className="fixed inset-0 z-[80] grid place-items-center overflow-y-auto bg-slate-950/30 p-4" role="presentation" onMouseDown={event => event.target === event.currentTarget && closeDialog()}><section role="dialog" aria-modal="true" className="my-auto w-full max-w-2xl rounded-xl border border-[#E5E7EB] bg-white p-5 shadow-xl"><header className="flex items-center justify-between gap-3"><h2 className="font-semibold">{dialog === "create" ? label("Create OAuth client", "បង្កើត OAuth Client") : dialog === "edit" ? label("Edit client", "កែប្រែ Client") : label("Client details", "ព័ត៌មានលម្អិត Client")}</h2><button type="button" aria-label={label("Close", "បិទ")} onClick={closeDialog}><FiX/></button></header>
      {dialog === "detail" && (detail.isLoading ? <p className="py-8 text-center text-sm text-slate-500">{label("Loading client details…", "កំពុងផ្ទុកព័ត៌មាន Client…")}</p> : detail.isError || !detail.data ? <p role="alert" className="py-5 text-sm text-rose-700">{label("Unable to load client details.", "មិនអាចផ្ទុកព័ត៌មាន Client បានទេ។")}</p> : <><dl className="mt-4 grid gap-3 sm:grid-cols-2">{[[label("Client ID", "លេខសម្គាល់ Client"), detail.data.clientId], [label("Name", "ឈ្មោះ"), detail.data.clientName], [label("Status", "ស្ថានភាព"), detail.data.active ? label("Active", "សកម្ម") : label("Inactive", "អសកម្ម")], [label("Authentication methods", "វិធីសាស្ត្រផ្ទៀងផ្ទាត់"), detail.data.clientAuthenticationMethods.join(", ")], [label("Grant types", "ប្រភេទ Grant"), detail.data.authorizationGrantTypes.join(", ")], [label("Scopes", "វិសាលភាព"), detail.data.scopes.join(", ")], [label("Authorization consent", "ការយល់ព្រម Authorization"), String(detail.data.requireAuthorizationConsent)], [label("PKCE required", "តម្រូវឱ្យប្រើ PKCE"), String(detail.data.requireProofKey)], [label("Access token lifetime (seconds)", "អាយុកាល Access Token (វិនាទី)"), String(detail.data.accessTokenTtlSeconds)], [label("Refresh token lifetime (seconds)", "អាយុកាល Refresh Token (វិនាទី)"), String(detail.data.refreshTokenTtlSeconds)], [label("Refresh token reuse", "ប្រើ Refresh Token ម្តងទៀត"), String(detail.data.reuseRefreshTokens)], [label("Created", "បានបង្កើត"), detail.data.clientIdIssuedAt ? new Date(detail.data.clientIdIssuedAt).toLocaleString(km ? "km-KH" : "en-US") : "—"]].map(([title, value]) => <div key={title}><dt className="text-xs text-slate-500">{title}</dt><dd className="mt-1 break-all text-sm font-medium">{value}</dd></div>)}<div className="sm:col-span-2"><dt className="text-xs text-slate-500">{label("Redirect URIs", "Redirect URI")}</dt><dd className="mt-1 space-y-1 break-all text-xs">{detail.data.redirectUris.map(uri => <p key={uri}>{uri}</p>)}</dd></div><div className="sm:col-span-2"><dt className="text-xs text-slate-500">{label("Post-logout redirect URIs", "Post-logout Redirect URI")}</dt><dd className="mt-1 space-y-1 break-all text-xs">{detail.data.postLogoutRedirectUris.map(uri => <p key={uri}>{uri}</p>)}</dd></div></dl><p className="mt-4 text-xs text-slate-500">{label("Stored secrets are never returned. Use Rotate secret to issue a new one-time secret.", "Secret ដែលរក្សាទុកមិនត្រូវបានបង្ហាញទេ។ ប្រើប្តូរ Secret ដើម្បីទទួល Secret ថ្មីដែលបង្ហាញតែម្តង។")}</p>{canManage && <Button className="mt-4" type="button" onClick={() => openEdit(detail.data!.clientId)}>{label("Edit configuration", "កែប្រែការកំណត់")}</Button>}</>)}
      {(dialog === "create" || dialog === "edit") && <>{dialog === "edit" && detail.isLoading ? <p className="py-6 text-sm text-slate-500">{label("Loading client…", "កំពុងផ្ទុក Client…")}</p> : <form className="mt-4 grid gap-4 sm:grid-cols-2" onSubmit={form.handleSubmit(values => save.mutate(values), () => setError(label("Check the highlighted configuration fields.", "សូមពិនិត្យព័ត៌មានការកំណត់ដែលបានបញ្ចូល។")))}>
        <label className="text-sm font-medium">{label("Client name", "ឈ្មោះ Client")}<input {...form.register("clientName")} className="mt-1 h-10 w-full rounded-lg border px-3"/><small className="text-rose-700">{form.formState.errors.clientName?.message}</small></label>
        <fieldset className="text-sm"><legend className="font-medium">{label("Authentication methods", "វិធីសាស្ត្រផ្ទៀងផ្ទាត់")}</legend>{(["client_secret_basic", "client_secret_post"] as const).map(method => <label className="mt-2 flex items-center gap-2" key={method}><input type="checkbox" value={method} {...form.register("clientAuthenticationMethods")}/>{method}</label>)}</fieldset>
        <fieldset className="text-sm sm:col-span-2"><legend className="font-medium">{label("Grant types", "ប្រភេទ Grant")}</legend><div className="mt-2 flex flex-wrap gap-4">{(["authorization_code", "refresh_token", "client_credentials"] as const).map(grant => <label className="flex items-center gap-2" key={grant}><input type="checkbox" value={grant} {...form.register("authorizationGrantTypes")}/>{grant}</label>)}</div></fieldset>
        <label className="text-sm font-medium sm:col-span-2">{label("Redirect URIs (one per line)", "Redirect URI (មួយក្នុងមួយបន្ទាត់)")}<textarea {...form.register("redirectUris")} rows={3} className="mt-1 w-full rounded-lg border px-3 py-2 font-mono text-xs" placeholder="https://example.org/oauth/callback"/><small className="block text-xs font-normal text-slate-500">{label("HTTPS is required; HTTP is accepted only for localhost/loopback.", "ត្រូវប្រើ HTTPS; HTTP អនុញ្ញាតតែ localhost/loopback ប៉ុណ្ណោះ។")}</small></label>
        <label className="text-sm font-medium sm:col-span-2">{label("Post-logout redirect URIs (one per line)", "Post-logout Redirect URI (មួយក្នុងមួយបន្ទាត់)")}<textarea {...form.register("postLogoutRedirectUris")} rows={2} className="mt-1 w-full rounded-lg border px-3 py-2 font-mono text-xs"/></label>
        <label className="text-sm font-medium sm:col-span-2">{label("Scopes (space or comma separated)", "Scope (បំបែកដោយដកឃ្លា ឬក្បៀស)")}<input {...form.register("scopes")} className="mt-1 h-10 w-full rounded-lg border px-3" placeholder="openid profile email"/><small className="text-xs font-normal text-slate-500">{label("Use OIDC scopes or active IAM permission names.", "ប្រើ OIDC scope ឬឈ្មោះសិទ្ធិ IAM ដែលសកម្ម។")}</small></label>
        <label className="flex items-center gap-2 text-sm"><input type="checkbox" {...form.register("requireAuthorizationConsent")}/>{label("Require authorization consent", "តម្រូវឱ្យយល់ព្រម Authorization")}</label><label className="flex items-center gap-2 text-sm"><input type="checkbox" {...form.register("requireProofKey")}/>{label("Require PKCE", "តម្រូវឱ្យប្រើ PKCE")}</label>
        <label className="text-sm font-medium">{label("Access token lifetime (seconds)", "អាយុកាល Access Token (វិនាទី)")}<input type="number" {...form.register("accessTokenTtlSeconds", { valueAsNumber: true })} className="mt-1 h-10 w-full rounded-lg border px-3"/><small className="text-rose-700">{form.formState.errors.accessTokenTtlSeconds?.message}</small></label><label className="text-sm font-medium">{label("Refresh token lifetime (seconds)", "អាយុកាល Refresh Token (វិនាទី)")}<input type="number" {...form.register("refreshTokenTtlSeconds", { valueAsNumber: true })} className="mt-1 h-10 w-full rounded-lg border px-3"/><small className="text-rose-700">{form.formState.errors.refreshTokenTtlSeconds?.message}</small></label>
        <label className="flex items-center gap-2 text-sm sm:col-span-2"><input type="checkbox" {...form.register("reuseRefreshTokens")}/>{label("Reuse refresh tokens (disable rotation)", "ប្រើ Refresh Token ម្តងទៀត (បិទការប្តូរ Token)")}</label>
        {error && <p role="alert" className="text-sm text-rose-700 sm:col-span-2">{error}</p>}<div className="flex justify-end gap-2 sm:col-span-2"><Button type="button" variant="outline" onClick={closeDialog}>{label("Cancel", "បោះបង់")}</Button><Button disabled={save.isPending} type="submit">{save.isPending ? label("Saving…", "កំពុងរក្សាទុក…") : label("Save", "រក្សាទុក")}</Button></div>
      </form>}</>}
    </section></div>}
  </section>;
}

function lines(value: string) { return value.split(/\r?\n/).map(item => item.trim()).filter(Boolean); }

const clientRegistrationSchema = z.object({ client_name: z.string().trim().min(1).max(200), redirect_uris: z.string().trim().min(1) });
type ClientRegistrationForm = z.infer<typeof clientRegistrationSchema>;
type ClientRegistrationResult = { client_id: string; client_secret: string; client_name: string; redirect_uris: string[]; registration_access_token: string; registration_client_uri: string };
function OAuthClientRegistration({ locale, canRegister }: { locale: Locale; canRegister: boolean }) {
  const isKm = locale === "km"; const [result, setResult] = useState<ClientRegistrationResult | null>(null); const [error, setError] = useState(""); const [lookupError, setLookupError] = useState(""); const [lookupResult, setLookupResult] = useState<{ client_id: string; client_name: string; redirect_uris: string[]; scope: string[] } | null>(null);
  const lookup = useMutation({ mutationFn: async ({ clientId, registrationToken }: { clientId: string; registrationToken: string }) => (await axios.get<{ client_id: string; client_name: string; redirect_uris: string[]; scope: string[] }>("/connect/register", { params: { client_id: clientId }, headers: { Authorization: `Bearer ${registrationToken}` } })).data, onSuccess: value => { setLookupResult(value); setLookupError(""); }, onError: () => { setLookupResult(null); setLookupError(isKm ? "រកមិនឃើញ Client ឬ Registration token មិនត្រឹមត្រូវទេ។" : "Client not found or registration access token is invalid."); } });
  const form = useForm<ClientRegistrationForm>({ resolver: zodResolver(clientRegistrationSchema), defaultValues: { client_name: "", redirect_uris: "" } });
  const registration = useMutation({ mutationFn: async (body: ClientRegistrationForm) => {
    const redirectUris = body.redirect_uris.split(/\r?\n/).map(uri => uri.trim()).filter(Boolean);
    redirectUris.forEach(uri => { new URL(uri); });
    return (await apiClient.post<ClientRegistrationResult>("/connect/register", {
      client_name: body.client_name,
      redirect_uris: redirectUris,
      grant_types: ["authorization_code", "refresh_token"],
      response_types: ["code"],
      scope: ["openid", "profile", "email"],
      token_endpoint_auth_method: "client_secret_basic",
    }, { baseURL: "" })).data;
  }, onSuccess: value => { setResult(value); setError(""); form.reset(); }, onError: () => setError(isKm ? "មិនអាចចុះឈ្មោះបានទេ។ សូមពិនិត្យសិទ្ធិ client.create និង redirect URI។" : "Registration failed. Check your client.create authority and redirect URIs.") });
  return <div className="max-w-3xl space-y-5"><div><p className="text-xs font-semibold uppercase tracking-[.14em] text-[#534AB7]">OAuth2 / OpenID Connect</p><h1 className="mt-1 text-xl font-semibold">{isKm ? "ចុះឈ្មោះ OAuth client" : "Register an OAuth client"}</h1><p className="mt-1 text-sm text-slate-500">{isKm ? "ចុះឈ្មោះ client តាម Dynamic Client Registration API ដែលមានស្រាប់។" : "Register a client through the existing Dynamic Client Registration API."}</p></div>
    {!canRegister && <section className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-950">{isKm ? "គណនីនេះមិនមានសិទ្ធិ client.create ទេ។ សូមទាក់ទងអ្នកគ្រប់គ្រង។" : "Your account does not have the client.create permission required by this endpoint."}</section>}
    {canRegister && (result ? <section className="rounded-xl border border-emerald-200 bg-white p-5"><div className="flex items-center justify-between gap-3"><h2 className="font-semibold text-emerald-900">{isKm ? "បានចុះឈ្មោះ client" : "Client registered"}</h2><button className="rounded-lg border px-3 py-1.5 text-sm" onClick={()=>setResult(null)}>{isKm ? "ចុះឈ្មោះមួយទៀត" : "Register another"}</button></div><p className="mt-2 text-sm text-slate-600">{isKm ? "ព័ត៌មានសម្ងាត់ត្រូវបានបង្ហាញតែម្តង។ រក្សាទុកនៅកន្លែងមានសុវត្ថិភាព។" : "The client secret and registration token are shown once. Store them securely."}</p><dl className="mt-4 space-y-3">{[["client_id",result.client_id],["client_secret",result.client_secret],["registration_access_token",result.registration_access_token],["registration_client_uri",result.registration_client_uri]].map(([k,v])=><div key={k} className="rounded-lg border border-slate-100 bg-slate-50 p-3"><dt className="text-xs font-medium text-slate-500">{k}</dt><dd className="mt-1 flex items-start justify-between gap-3"><code className="break-all text-xs text-slate-900">{v}</code><button className="shrink-0 text-xs text-[#534AB7]" onClick={()=>navigator.clipboard.writeText(v)}>{isKm ? "ចម្លង" : "Copy"}</button></dd></div>)}</dl></section> : <form className="space-y-4 rounded-xl border border-[#E5E7EB] bg-white p-5" onSubmit={form.handleSubmit(value=>registration.mutate(value),()=>setError(isKm?"បំពេញព័ត៌មានដែលត្រូវការ។":"Enter the required client details."))}><Field label={isKm ? "ឈ្មោះ client" : "Client name"} name="client_name" required/><label className="block text-sm font-medium text-slate-700">{isKm ? "Redirect URIs (មួយក្នុងមួយជួរ)" : "Redirect URIs (one per line)"}<textarea className="mt-1.5 min-h-28 w-full rounded-lg border border-[#E5E7EB] bg-white px-3 py-2 text-sm outline-none focus:border-[#534AB7] focus:ring-2 focus:ring-[#EEECFF]" {...form.register("redirect_uris")} placeholder="https://example.com/oauth/callback"/><span className="mt-1 block text-xs font-normal text-rose-700">{form.formState.errors.redirect_uris?.message}</span></label><div className="rounded-lg bg-slate-50 p-3 text-xs leading-5 text-slate-600">{isKm ? "Grant: Authorization Code + Refresh Token · Scopes: openid, profile, email · Client authentication: client_secret_basic" : "Grant types: Authorization Code + Refresh Token · Scopes: openid, profile, email · Client authentication: client_secret_basic"}</div>{error&&<p role="alert" className="text-sm text-rose-700">{error}</p>}<button disabled={registration.isPending} className="h-10 rounded-lg bg-[#534AB7] px-4 text-sm font-medium text-white hover:bg-[#43389B] disabled:opacity-50">{registration.isPending ? (isKm?"កំពុងចុះឈ្មោះ…":"Registering…") : (isKm?"ចុះឈ្មោះ client":"Register client")}</button></form>)}
    <section className="rounded-xl border border-[#E5E7EB] bg-white p-5"><h2 className="font-semibold">{isKm ? "មើល Client ដែលបានចុះឈ្មោះ" : "View a registered client"}</h2><p className="mt-1 text-sm text-slate-500">{isKm ? "ប្រើ Client ID និង Registration Access Token ដែលបានរក្សាទុក។ Token មិនត្រូវបានរក្សាទុកក្នុងកម្មវិធីទេ។" : "Use the client ID and registration access token you saved. The token is not persisted by this page."}</p><form className="mt-4 grid gap-3 sm:grid-cols-2" onSubmit={event => { event.preventDefault(); const values = new FormData(event.currentTarget); lookup.mutate({ clientId: String(values.get("clientId") ?? "").trim(), registrationToken: String(values.get("registrationToken") ?? "").trim() }); }}><Field label={isKm ? "លេខសម្គាល់ Client" : "Client ID"} name="clientId" required/><Field label={isKm ? "Token ចូលប្រើសម្រាប់ការចុះឈ្មោះ" : "Registration access token"} name="registrationToken" type="password" required/><div className="sm:col-span-2 flex flex-wrap items-center gap-3"><button disabled={lookup.isPending} className="h-9 rounded-lg border border-[#E5E7EB] px-3 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50">{lookup.isPending ? (isKm ? "កំពុងផ្ទុក…" : "Loading…") : (isKm ? "មើលព័ត៌មាន" : "View client")}</button>{lookupError && <p role="alert" className="text-sm text-rose-700">{lookupError}</p>}</div></form>{lookupResult && <dl className="mt-4 grid gap-3 rounded-lg bg-slate-50 p-4 text-sm sm:grid-cols-2"><div><dt className="text-xs text-slate-500">{isKm ? "ឈ្មោះ Client" : "Client name"}</dt><dd className="mt-1 font-medium">{lookupResult.client_name}</dd></div><div><dt className="text-xs text-slate-500">{isKm ? "លេខសម្គាល់ Client" : "Client ID"}</dt><dd className="mt-1 break-all font-mono text-xs">{lookupResult.client_id}</dd></div><div className="sm:col-span-2"><dt className="text-xs text-slate-500">{isKm ? "អាសយដ្ឋាន Redirect" : "Redirect URIs"}</dt><dd className="mt-1 space-y-1 break-all text-xs">{lookupResult.redirect_uris.map(uri => <div key={uri}>{uri}</div>)}</dd></div><div className="sm:col-span-2"><dt className="text-xs text-slate-500">{isKm ? "វិសាលភាព (Scopes)" : "Scopes"}</dt><dd className="mt-1 flex flex-wrap gap-1.5">{lookupResult.scope.map(scope => <span className="rounded-md border border-indigo-100 bg-indigo-50 px-2 py-1 text-xs text-indigo-800" key={scope}>{scope}</span>)}</dd></div></dl>}</section>
    <p className="text-xs leading-5 text-slate-500">{isKm ? "ការកែប្រែ ឬលុប client មិនមាន API នៅក្នុង backend បច្ចុប្បន្នទេ។" : "The backend currently does not expose client update or deletion APIs."}</p>
  </div>;
}

function RecordDialog({ kind, endpoint, selected, title, t, onClose, onSaved, onError }: { kind: "users" | "roles" | "permissions"; endpoint: string; selected: Role | Permission | null; title: string; t: (key: keyof typeof copy.en) => string; onClose: () => void; onSaved: () => void; onError: (v: string) => void }) {
  const mutation = useMutation({ mutationFn: async (body: Record<string, unknown>) => selected ? apiClient.put(`${endpoint}/${selected.uuid}`, body) : apiClient.post(endpoint, body), onSuccess: onSaved, onError: () => onError(t("saveError")) });
  return <div className="fixed inset-0 z-[80] grid place-items-center bg-slate-950/30 p-4" role="presentation" onMouseDown={e => e.target === e.currentTarget && onClose()}><form className="w-full max-w-lg space-y-4 rounded-xl border border-[#E5E7EB] bg-white p-5 shadow-xl" onSubmit={e => { e.preventDefault(); const fd = new FormData(e.currentTarget); const body: Record<string, unknown> = Object.fromEntries(fd.entries()); if (kind === "users") { body.password = fd.get("password"); } mutation.mutate(body); }}><div className="flex items-center justify-between"><h2 className="font-semibold">{title}</h2><button type="button" onClick={onClose}><FiX/></button></div>{kind === "users" ? <><Field label={t("username")} name="username" required/><Field label={t("email")} name="email" type="email" required/><Field label={t("initialPassword")} name="password" type="password" required/><Field label={t("givenName")} name="givenName"/><Field label={t("familyName")} name="familyName"/></> : kind === "roles" ? <><Field label={t("name")} name="name" required defaultValue={(selected as Role | null)?.name}/><Field label={t("descriptionLabel")} name="description" defaultValue={(selected as Role | null)?.description}/></> : <><Field label={t("name")} name="name" required defaultValue={(selected as Permission | null)?.name}/><Field label={t("resource")} name="resource" required defaultValue={(selected as Permission | null)?.resource}/><Field label={t("action")} name="action" required defaultValue={(selected as Permission | null)?.action}/><Field label={t("descriptionLabel")} name="description" defaultValue={(selected as Permission | null)?.description}/></>}<div className="flex justify-end gap-2 pt-2"><button type="button" onClick={onClose} className="h-9 rounded-lg border px-3 text-sm">{t("cancel")}</button><button disabled={mutation.isPending} className="h-9 rounded-lg bg-[#534AB7] px-4 text-sm font-medium text-white disabled:opacity-50">{mutation.isPending ? t("loading") : t("save")}</button></div></form></div>;
}

function UserDetail({ t, canRead, canManage, canAssignRoles, canReadRoles, canAssignPermissions, canReadPermissions }: { t: (key: keyof typeof copy.en) => string; canRead: boolean; canManage: boolean; canAssignRoles: boolean; canReadRoles: boolean; canAssignPermissions: boolean; canReadPermissions: boolean }) {
  const { uuid = "" } = useParams();
  const qc = useQueryClient();
  const navigate = useNavigate();
  const [error, setError] = useState("");
  const [editOpen, setEditOpen] = useState(false);
  const [resetOpen, setResetOpen] = useState(false);
  const [roleToAssign, setRoleToAssign] = useState("");
  const [permissionToGrant, setPermissionToGrant] = useState("");
  const [permissionEffect, setPermissionEffect] = useState<"GRANT" | "DENY">("GRANT");
  const user = useQuery({ queryKey: ["user", uuid], queryFn: async () => (await apiClient.get<User>(`/users/${uuid}`)).data, enabled: !!uuid && canRead });
  const authorities = useQuery({ queryKey: ["user-authorities", uuid], queryFn: async () => (await apiClient.get<{ roles: string[]; permissions: string[] }>(`/users/${uuid}/authorities`)).data, enabled: !!uuid && canRead });
  const roleAssignments = useQuery({ queryKey: ["user-role-assignments", uuid], queryFn: async () => (await apiClient.get<{ roleUuid: string; roleName: string; assignedAt: string; expiresAt?: string }[]>(`/users/${uuid}/roles/assignments`)).data, enabled: !!uuid && canRead });
  const directPermissionAssignments = useQuery({ queryKey: ["user-direct-permissions", uuid], queryFn: async () => (await apiClient.get<{ permissionUuid: string; permissionName: string; effect: "GRANT" | "DENY"; assignedAt: string; expiresAt?: string }[]>(`/users/${uuid}/permissions/direct`)).data, enabled: !!uuid && canRead });
  const availableRoles = useQuery({ queryKey: ["roles", "assignment-options"], queryFn: async () => (await apiClient.get<Page<Role>>("/roles", { params: { active: true, page: 0, size: 100 } })).data, enabled: !!uuid && canRead && canAssignRoles && canReadRoles });
  const availablePermissions = useQuery({ queryKey: ["permissions", "assignment-options"], queryFn: async () => (await apiClient.get<Page<Permission>>("/permissions", { params: { active: true, page: 0, size: 100 } })).data, enabled: !!uuid && canRead && canAssignPermissions && canReadPermissions });
  const inheritedPermissionQueries = useQueries({ queries: (roleAssignments.data ?? []).map(role => ({ queryKey: ["role-permission-assignments", role.roleUuid], queryFn: async () => (await apiClient.get<{ permissionUuid: string; permissionName: string; resource: string; action: string }[]>(`/roles/${role.roleUuid}/permissions`)).data, enabled: canRead && canReadRoles })) });
  const action = useMutation({
    mutationFn: async ({ path, method }: { path: string; method: "put" | "delete" }) => apiClient.request({ url: path, method }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["user", uuid] }); qc.invalidateQueries({ queryKey: ["user-authorities", uuid] }); qc.invalidateQueries({ queryKey: ["users"] }); },
    onError: () => setError(t("error")),
  });
  const update = useMutation({
    mutationFn: (values: Record<string, string>) => apiClient.put(`/users/${uuid}`, values),
    onSuccess: () => { setEditOpen(false); qc.invalidateQueries({ queryKey: ["user", uuid] }); qc.invalidateQueries({ queryKey: ["users"] }); },
    onError: () => setError(t("saveError")),
  });
  const reset = useMutation({
    mutationFn: (values: { newPassword: string; confirmPassword: string }) => apiClient.put(`/users/${uuid}/password/reset`, values),
    onSuccess: () => setResetOpen(false), onError: () => setError(t("saveError")),
  });
  const assignRole = useMutation({ mutationFn: () => apiClient.post(`/users/${uuid}/roles`, { roleUuids: [roleToAssign] }), onSuccess: () => { setRoleToAssign(""); qc.invalidateQueries({ queryKey: ["user-role-assignments", uuid] }); qc.invalidateQueries({ queryKey: ["user-authorities", uuid] }); }, onError: () => setError(t("saveError")) });
  const revokeRole = useMutation({ mutationFn: (roleUuid: string) => apiClient.delete(`/users/${uuid}/roles/${roleUuid}`), onSuccess: () => { qc.invalidateQueries({ queryKey: ["user-role-assignments", uuid] }); qc.invalidateQueries({ queryKey: ["user-authorities", uuid] }); }, onError: () => setError(t("saveError")) });
  const grantPermission = useMutation({ mutationFn: () => apiClient.post(`/users/${uuid}/permissions`, { permissionUuids: [permissionToGrant], effect: permissionEffect }), onSuccess: () => { setPermissionToGrant(""); qc.invalidateQueries({ queryKey: ["user-direct-permissions", uuid] }); qc.invalidateQueries({ queryKey: ["user-authorities", uuid] }); }, onError: () => setError(t("saveError")) });
  const revokePermission = useMutation({ mutationFn: (permissionUuid: string) => apiClient.delete(`/users/${uuid}/permissions/${permissionUuid}`), onSuccess: () => { qc.invalidateQueries({ queryKey: ["user-direct-permissions", uuid] }); qc.invalidateQueries({ queryKey: ["user-authorities", uuid] }); }, onError: () => setError(t("saveError")) });
  if (!canRead) return <PermissionRequired t={t} permission="user.read"/>;
  if (user.isLoading) return <p>{t("loading")}</p>;
  if (!user.data) return <p role="alert">{t("loadingError")}</p>;
  const value = user.data;
  const locked = value.accountLocked ?? value.locked;
  const setState = (operation: "enable" | "disable" | "lock" | "unlock") => action.mutate({ path: `/users/${uuid}/${operation}`, method: "put" });
  const inheritedPermissionSources = inheritedPermissionQueries.flatMap((query, index) =>
    (query.data ?? []).map(permission => ({ ...permission, roleUuid: roleAssignments.data?.[index]?.roleUuid ?? "", roleName: roleAssignments.data?.[index]?.roleName ?? "" })));
  const directDenials = new Set((directPermissionAssignments.data ?? []).filter(permission => permission.effect === "DENY").map(permission => permission.permissionUuid));
  return <div className="space-y-5">
    <div className="flex flex-wrap items-start justify-between gap-3">
      <div className="flex items-center gap-3"><span className="grid size-11 shrink-0 place-items-center overflow-hidden rounded-full bg-[#EEECFF] text-sm font-semibold text-[#534AB7]">{value.pictureUrl ? <img alt="" className="size-full object-cover" referrerPolicy="no-referrer" src={value.pictureUrl}/> : Array.from(value.username)[0]?.toLocaleUpperCase() ?? "N"}</span><div><Link className="text-xs text-[#534AB7]" to="/users">← {t("users")}</Link><h1 className="mt-1 text-xl font-semibold">{value.username}</h1><p className="text-sm text-slate-500">{value.email}</p></div></div>
      {canManage && <div className="flex flex-wrap gap-2">
        <button className="rounded-lg border px-3 py-2 text-sm" onClick={() => setEditOpen(true)}>{t("edit")}</button>
        <button className="rounded-lg border px-3 py-2 text-sm" onClick={() => setResetOpen(true)}>{t("resetPassword")}</button>
        <button className="rounded-lg border px-3 py-2 text-sm" onClick={() => setState(value.enabled ? "disable" : "enable")}>{value.enabled ? t("disable") : t("enable")}</button>
        <button className="rounded-lg border px-3 py-2 text-sm" onClick={() => setState(locked ? "unlock" : "lock")}>{locked ? t("unlock") : t("lock")}</button>
        <button className="rounded-lg border border-rose-200 px-3 py-2 text-sm text-rose-700" onClick={() => window.confirm(t("confirmDelete")) && action.mutate({ path: `/users/${uuid}`, method: "delete" }, { onSuccess: () => navigate("/users") })}>{t("deleteUser")}</button>
      </div>}
    </div>
    {error && <p role="alert" className="text-sm text-rose-700">{error}</p>}
    <div className="grid gap-4 lg:grid-cols-2">
      <section className="rounded-xl border border-[#E5E7EB] bg-white p-5"><h2 className="font-semibold">{t("userDetails")}</h2><dl className="mt-4 grid grid-cols-2 gap-4 text-sm">{[[t("username"), value.username], [t("email"), value.email], [t("givenName"), value.givenName], [t("familyName"), value.familyName], [t("phone"), value.phoneNumber], [t("status"), locked ? t("locked") : value.enabled ? t("active") : t("disabled")]].map(([label, content]) => <div key={label}><dt className="text-xs text-slate-500">{label}</dt><dd className="mt-1 font-medium">{content || "—"}</dd></div>)}</dl></section>
      <section className="rounded-xl border border-[#E5E7EB] bg-white p-5"><h2 className="font-semibold">{t("roles")}</h2><p className="mt-1 text-xs text-slate-500">{localeField("Assigned roles", "តួនាទីដែលបានផ្តល់")}</p><div className="mt-3 space-y-2">{roleAssignments.data?.length ? roleAssignments.data.map(role => <div key={role.roleUuid} className="flex items-center justify-between gap-2 rounded-lg border border-slate-100 px-3 py-2"><span className="text-sm">{role.roleName}</span>{canAssignRoles && <button type="button" aria-label={`${localeField("Revoke", "ដក")}: ${role.roleName}`} disabled={revokeRole.isPending} className="text-xs text-rose-700 hover:underline" onClick={() => window.confirm(localeField("Revoke this role?", "តើដកតួនាទីនេះឬ?")) && revokeRole.mutate(role.roleUuid)}>{localeField("Revoke", "ដក")}</button>}</div>) : <span className="text-sm text-slate-500">{roleAssignments.isLoading ? t("loading") : t("noResults")}</span>}</div>{canAssignRoles && canReadRoles ? <div className="mt-4 flex gap-2"><select className="h-9 min-w-0 flex-1 rounded-lg border border-[#E5E7EB] px-2 text-sm" value={roleToAssign} onChange={e => setRoleToAssign(e.target.value)}><option value="">{localeField("Choose a role", "ជ្រើសរើសតួនាទី")}</option>{(availableRoles.data?.content ?? []).filter(role => !roleAssignments.data?.some(assigned => assigned.roleUuid === role.uuid)).map(role => <option key={role.uuid} value={role.uuid}>{role.name}</option>)}</select><button type="button" disabled={!roleToAssign || assignRole.isPending} className="rounded-lg bg-[#534AB7] px-3 text-sm text-white disabled:opacity-50" onClick={() => assignRole.mutate()}>{localeField("Assign", "ផ្តល់")}</button></div> : <p className="mt-4 text-xs text-slate-500">{!canAssignRoles && !canReadRoles ? localeField("Role assignment requires both user.role.assign and role.read permissions.", "ការផ្តល់តួនាទីត្រូវការសិទ្ធិ user.role.assign និង role.read ទាំងពីរ។") : !canAssignRoles ? localeField("Your account does not have user.role.assign.", "គណនីរបស់អ្នកមិនមានសិទ្ធិ user.role.assign ទេ។") : localeField("Your account does not have role.read.", "គណនីរបស់អ្នកមិនមានសិទ្ធិ role.read ទេ។")}</p>}</section>
      <section className="rounded-xl border border-[#E5E7EB] bg-white p-5 lg:col-span-2"><h2 className="font-semibold">{t("permissions")}</h2><p className="mt-1 text-xs text-slate-500">{localeField("Effective permissions are returned by the backend after applying direct GRANT/DENY and role inheritance.", "សិទ្ធិមានប្រសិទ្ធភាពត្រូវបានផ្តល់ដោយ Backend ក្រោយអនុវត្ត GRANT/DENY ផ្ទាល់ និងសិទ្ធិតាមតួនាទី។")}</p><div className="mt-3 flex flex-wrap gap-2">{authorities.data?.permissions?.length ? authorities.data.permissions.map(permission => <span key={permission} className="rounded-md bg-indigo-50 px-2.5 py-1 text-xs text-indigo-800">{permission}</span>) : <span className="text-sm text-slate-500">{authorities.isLoading ? t("loading") : t("noResults")}</span>}</div><h3 className="mt-5 text-sm font-medium">{localeField("Direct assignments", "សិទ្ធិដែលផ្តល់ដោយផ្ទាល់")}</h3><div className="mt-2 space-y-2">{directPermissionAssignments.data?.length ? directPermissionAssignments.data.map(permission => <div key={`${permission.permissionUuid}-${permission.effect}`} className="flex items-center justify-between gap-2 rounded-lg border border-slate-100 px-3 py-2"><span className="text-sm">{permission.permissionName} <span className={`ml-1 rounded px-1.5 py-0.5 text-[10px] ${permission.effect === "GRANT" ? "bg-emerald-50 text-emerald-700" : "bg-rose-50 text-rose-700"}`}>{localeField(permission.effect === "GRANT" ? "Grant" : "Deny", permission.effect === "GRANT" ? "ផ្តល់" : "បដិសេធ")}</span></span>{canAssignPermissions && <button type="button" aria-label={`${localeField("Revoke", "ដក")}: ${permission.permissionName}`} disabled={revokePermission.isPending} className="text-xs text-rose-700 hover:underline" onClick={() => window.confirm(localeField("Revoke this direct permission?", "តើដកសិទ្ធិផ្ទាល់នេះឬ?")) && revokePermission.mutate(permission.permissionUuid)}>{localeField("Revoke", "ដក")}</button>}</div>) : <span className="text-sm text-slate-500">{directPermissionAssignments.isLoading ? t("loading") : t("noResults")}</span>}</div>{!canAssignPermissions ? <p className="mt-3 text-xs text-slate-500">{localeField("Your account does not have user.permission.assign.", "គណនីរបស់អ្នកមិនមានសិទ្ធិ user.permission.assign ទេ។")}</p> : !canReadPermissions ? <p className="mt-3 text-xs text-amber-700">{localeField("Permission assignment controls require both user.permission.assign and permission.read.", "ការគ្រប់គ្រងការផ្តល់សិទ្ធិត្រូវការទាំង user.permission.assign និង permission.read។")}</p> : <div className="mt-3 flex flex-col gap-2 sm:flex-row"><select className="h-9 min-w-0 flex-1 rounded-lg border border-[#E5E7EB] px-2 text-sm" value={permissionToGrant} onChange={e => setPermissionToGrant(e.target.value)}><option value="">{localeField("Choose a permission", "ជ្រើសរើសសិទ្ធិ")}</option>{(availablePermissions.data?.content ?? []).filter(permission => !directPermissionAssignments.data?.some(assigned => assigned.permissionUuid === permission.uuid)).map(permission => <option key={permission.uuid} value={permission.uuid}>{permission.name} · {permission.resource}:{permission.action}</option>)}</select><select aria-label={localeField("Permission effect", "ឥទ្ធិពលសិទ្ធិ")} className="h-9 rounded-lg border border-[#E5E7EB] px-2 text-sm" value={permissionEffect} onChange={e => setPermissionEffect(e.target.value as "GRANT" | "DENY")}><option value="GRANT">{localeField("Grant", "ផ្តល់")}</option><option value="DENY">{localeField("Deny", "បដិសេធ")}</option></select><button type="button" disabled={!permissionToGrant || grantPermission.isPending} className="rounded-lg bg-[#534AB7] px-3 text-sm text-white disabled:opacity-50" onClick={() => grantPermission.mutate()}>{localeField("Assign", "ផ្តល់")}</button></div>}<h3 className="mt-5 text-sm font-medium">{localeField("Inherited through roles", "ទទួលតាមតួនាទី")}</h3>{!canReadRoles ? <p className="mt-2 text-xs text-slate-500">{localeField("role.read is required to inspect inherited permission sources.", "ត្រូវការសិទ្ធិ role.read ដើម្បីពិនិត្យប្រភពសិទ្ធិតាមតួនាទី។")}</p> : inheritedPermissionQueries.some(query => query.isLoading) ? <p className="mt-2 text-sm text-slate-500">{t("loading")}</p> : inheritedPermissionQueries.some(query => query.isError) ? <p role="alert" className="mt-2 text-sm text-rose-700">{t("loadingError")}</p> : inheritedPermissionSources.length ? <div className="mt-2 space-y-2">{inheritedPermissionSources.map((permission, index) => <div key={`${permission.roleUuid}-${permission.permissionUuid}-${index}`} className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-slate-100 px-3 py-2"><span className="text-sm">{permission.permissionName}<span className="ml-2 text-xs text-slate-500">{localeField("from", "ពី")} {permission.roleName}</span></span>{directDenials.has(permission.permissionUuid) && <span className="rounded bg-rose-50 px-2 py-1 text-[10px] text-rose-700">{localeField("Overridden by direct DENY", "ត្រូវបានបដិសេធដោយ DENY ផ្ទាល់")}</span>}</div>)}</div> : <p className="mt-2 text-sm text-slate-500">{localeField("No active inherited permissions.", "មិនមានសិទ្ធិសកម្មដែលទទួលតាមតួនាទីទេ។")}</p>}</section>
    </div>
    {editOpen && <div className="fixed inset-0 z-[80] grid place-items-center bg-slate-950/30 p-4"><form className="w-full max-w-lg space-y-4 rounded-xl bg-white p-5" onSubmit={e => { e.preventDefault(); const f = new FormData(e.currentTarget); update.mutate(Object.fromEntries(["givenName", "familyName", "middleName", "nickname", "gender", "phoneNumber", "pictureUrl"].map(k => [k, String(f.get(k) ?? "")]))); }}><div className="flex justify-between"><h2 className="font-semibold">{t("edit")} · {value.username}</h2><button type="button" onClick={() => setEditOpen(false)}><FiX /></button></div><Field label={t("givenName")} name="givenName" defaultValue={value.givenName}/><Field label={t("familyName")} name="familyName" defaultValue={value.familyName}/><Field label={localeField("Middle name", "នាមកណ្តាល")} name="middleName" defaultValue={value.middleName}/><Field label={localeField("Nickname", "ឈ្មោះហៅក្រៅ")} name="nickname" defaultValue={value.nickname}/><Field label={localeField("Gender", "ភេទ")} name="gender" defaultValue={value.gender}/><Field label={t("phone")} name="phoneNumber" defaultValue={value.phoneNumber}/><Field label={localeField("Profile picture URL", "URL រូបភាពប្រវត្តិរូប")} name="pictureUrl" type="url" defaultValue={value.pictureUrl}/><button className="h-9 rounded-lg bg-[#534AB7] px-4 text-sm text-white">{t("saveChanges")}</button></form></div>}
    {resetOpen && <div className="fixed inset-0 z-[80] grid place-items-center bg-slate-950/30 p-4"><form className="w-full max-w-md space-y-4 rounded-xl bg-white p-5" onSubmit={e => { e.preventDefault(); const f = new FormData(e.currentTarget); const next = String(f.get("newPassword")); const confirm = String(f.get("confirmPassword")); if (next !== confirm) { setError(t("saveError")); return; } reset.mutate({ newPassword: next, confirmPassword: confirm }); }}><div className="flex justify-between"><h2 className="font-semibold">{t("resetPassword")}</h2><button type="button" onClick={() => setResetOpen(false)}><FiX /></button></div><Field label={t("passwordLabel")} name="newPassword" type="password" required/><Field label={localeField("Confirm password", "បញ្ជាក់ពាក្យសម្ងាត់")} name="confirmPassword" type="password" required/><button className="h-9 rounded-lg bg-[#534AB7] px-4 text-sm text-white">{t("save")}</button></form></div>}
  </div>;
}function localeField(en: string, km: string) { return new URLSearchParams(window.location.search).get("lang") === "km" ? km : en; }
type LoginEventRow = {
  id: string;
  userUuid: string | null;
  outcome: "SUCCESS" | "FAILURE";
  occurredAt: string;
  ipAddress: string | null;
  userAgent: string | null;
  failureCategory: string | null;
};

function LoginHistoryPage({ locale, admin = false }: { locale: Locale; admin?: boolean }) {
  const [pageNumber, setPageNumber] = useState(0);
  const [outcome, setOutcome] = useState<"ALL" | LoginEventRow["outcome"]>("ALL");
  const [userUuid, setUserUuid] = useState("");
  const page = useQuery({
    queryKey: ["login-history", admin, pageNumber, outcome, userUuid],
    queryFn: async () => (await apiClient.get<Page<LoginEventRow>>(admin ? "/admin/login-history" : "/me/login-history", {
      params: { page: pageNumber, size: 20, outcome: outcome === "ALL" ? undefined : outcome, ...(admin && userUuid.trim() ? { userUuid: userUuid.trim() } : {}) },
    })).data,
  });
  const isKm = locale === "km";
  const label = (en: string, km: string) => isKm ? km : en;
  const metadata = page.data?.page;

  return <section className="space-y-5">
    <header className="flex flex-wrap items-end justify-between gap-3">
      <div><p className="text-xs font-semibold uppercase tracking-[.14em] text-[#534AB7]">Northstar IAM</p><h1 className="mt-2 text-xl font-semibold">{label(admin ? "User login history" : "Login history", admin ? "ប្រវត្តិចូលរបស់អ្នកប្រើ" : "ប្រវត្តិចូល")}</h1><p className="mt-1 text-sm text-slate-500">{label("Review recorded authentication attempts.", "ពិនិត្យមើលការប៉ុនប៉ងផ្ទៀងផ្ទាត់ដែលបានកត់ត្រា។")}</p></div>
      <div className="flex flex-wrap gap-2">
        {admin && <input aria-label={label("Filter by user UUID", "ត្រងតាម UUID អ្នកប្រើ")} className="h-9 w-64 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm" onChange={event => { setUserUuid(event.target.value); setPageNumber(0); }} placeholder={label("User UUID", "UUID អ្នកប្រើ")} value={userUuid} />}
        <select aria-label={label("Filter by result", "ត្រងតាមលទ្ធផល")} className="h-9 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm" onChange={event => { setOutcome(event.target.value as "ALL" | LoginEventRow["outcome"]); setPageNumber(0); }} value={outcome}>
          <option value="ALL">{label("All results", "លទ្ធផលទាំងអស់")}</option><option value="SUCCESS">{label("Success", "ជោគជ័យ")}</option><option value="FAILURE">{label("Failure", "បរាជ័យ")}</option>
        </select>
      </div>
    </header>
    <div className="overflow-x-auto rounded-xl border border-[#E5E7EB] bg-white">
      {page.isLoading ? <p className="p-8 text-center text-sm text-slate-500">{label("Loading login history…", "កំពុងផ្ទុកប្រវត្តិចូល…")}</p>
        : page.isError ? <p className="p-8 text-center text-sm text-rose-700" role="alert">{label("Could not load login history. Check your access and try again.", "មិនអាចផ្ទុកប្រវត្តិចូលបានទេ។ សូមពិនិត្យសិទ្ធិ ហើយព្យាយាមម្ដងទៀត។")}</p>
          : !page.data?.content.length ? <p className="p-8 text-center text-sm text-slate-500">{label("No login events recorded.", "មិនមានព្រឹត្តិការណ៍ចូលដែលបានកត់ត្រាទេ។")}</p>
            : <table className="w-full min-w-[760px] text-left text-sm"><thead className="bg-slate-50 text-xs uppercase tracking-wide text-slate-500"><tr>{admin && <th className="px-4 py-3">{label("User UUID", "UUID អ្នកប្រើ")}</th>}<th className="px-4 py-3">{label("Result", "លទ្ធផល")}</th><th className="px-4 py-3">{label("Time", "ពេលវេលា")}</th><th className="px-4 py-3">{label("IP address", "អាសយដ្ឋាន IP")}</th><th className="px-4 py-3">{label("Failure category", "ប្រភេទបរាជ័យ")}</th><th className="px-4 py-3">{label("Browser / device", "កម្មវិធីរុករក / ឧបករណ៍")}</th></tr></thead><tbody className="divide-y divide-slate-100">{page.data.content.map(event => <tr key={event.id}>
              {admin && <td className="max-w-48 truncate px-4 py-3 font-mono text-xs">{event.userUuid ?? label("Unknown account", "គណនីមិនស្គាល់")}</td>}
              <td className="px-4 py-3"><span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${event.outcome === "SUCCESS" ? "bg-emerald-50 text-emerald-700" : "bg-rose-50 text-rose-700"}`}>{event.outcome === "SUCCESS" ? label("Success", "ជោគជ័យ") : label("Failure", "បរាជ័យ")}</span></td>
              <td className="whitespace-nowrap px-4 py-3">{new Intl.DateTimeFormat(isKm ? "km-KH" : "en-US", { dateStyle: "medium", timeStyle: "short" }).format(new Date(event.occurredAt))}</td>
              <td className="px-4 py-3">{event.ipAddress || "—"}</td><td className="px-4 py-3">{event.failureCategory || "—"}</td><td className="max-w-64 truncate px-4 py-3" title={event.userAgent ?? undefined}>{event.userAgent || "—"}</td>
            </tr>)}</tbody></table>}
    </div>
    {metadata && metadata.totalPages > 1 && <nav aria-label={label("Login history pages", "ទំព័រប្រវត្តិចូល")} className="flex flex-wrap items-center justify-between gap-3"><p className="text-xs text-slate-500">{label("Page", "ទំព័រ")} {metadata.number + 1} {label("of", "ក្នុងចំណោម")} {metadata.totalPages} · {metadata.totalElements} {label("events", "ព្រឹត្តិការណ៍")}</p><div className="flex gap-2"><Button disabled={metadata.first || page.isFetching} onClick={() => setPageNumber(value => Math.max(0, value - 1))} variant="outline" size="sm">{label("Previous", "មុន")}</Button><Button disabled={metadata.last || page.isFetching} onClick={() => setPageNumber(value => value + 1)} variant="outline" size="sm">{label("Next", "បន្ទាប់")}</Button></div></nav>}
  </section>;
}

function AccountSessionsPage({ locale }: { locale: Locale }) {
  const [pageNumber, setPageNumber] = useState(0);
  const queryClient = useQueryClient();
  const label = (en: string, km: string) => locale === "km" ? km : en;
  const query = useQuery({ queryKey: ["my-sessions", pageNumber], queryFn: async () =>
    (await apiClient.get<Page<SessionRecord>>(`/me/sessions?page=${pageNumber}&size=10`)).data });
  const revoke = useMutation({ mutationFn: async (id: string) => apiClient.delete(`/me/sessions/${id}`),
    onSuccess: (_, id) => { const isCurrent = query.data?.content.some(session => session.id === id && session.current); void queryClient.invalidateQueries({ queryKey: ["my-sessions"] }); if (isCurrent) void queryClient.invalidateQueries({ queryKey: ["me"] }); } });
  const revokeOthers = useMutation({ mutationFn: async () => apiClient.delete("/me/sessions/others"),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["my-sessions"] }) });
  const metadata = query.data?.page;
  return <section className="space-y-5">
    <header className="flex flex-wrap items-center justify-between gap-3"><div><h1 className="text-xl font-semibold">{label("Active sessions", "សម័យសកម្ម")}</h1><p className="mt-1 text-sm text-slate-500">{label("Review and revoke sessions connected to your account.", "ពិនិត្យ និងបញ្ចប់សម័យដែលភ្ជាប់នឹងគណនីរបស់អ្នក។")}</p></div>
      <Button variant="outline" disabled={revokeOthers.isPending || !query.data?.content.some(session => !session.current)} onClick={() => window.confirm(label("Revoke all other sessions? This session will remain active.", "បញ្ចប់សម័យផ្សេងទៀតទាំងអស់ឬ? សម័យនេះនឹងនៅសកម្ម។")) && revokeOthers.mutate()}>{label("Revoke other sessions", "បញ្ចប់សម័យផ្សេងទៀត")}</Button></header>
    {query.isLoading && <p role="status" className="rounded-xl border bg-white p-5 text-sm text-slate-500">{label("Loading sessions…", "កំពុងផ្ទុកសម័យ…")}</p>}
    {(query.isError || revoke.isError || revokeOthers.isError) && <p role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">{label("Unable to load or update sessions. Check your access and try again.", "មិនអាចផ្ទុក ឬកែប្រែសម័យបានទេ។ សូមពិនិត្យសិទ្ធិ។")}</p>}
    {!query.isLoading && query.data?.content.length === 0 && <p className="rounded-xl border bg-white p-6 text-sm text-slate-500">{label("No active sessions found.", "រកមិនឃើញសម័យសកម្មទេ។")}</p>}
    <div className="grid gap-3">{query.data?.content.map(session => <SessionCard key={session.id} session={session} locale={locale} busy={revoke.isPending} onRevoke={() => window.confirm(label("Revoke this session? You may be signed out of this browser.", "បញ្ចប់សម័យនេះឬ? អ្នកអាចត្រូវចាកចេញពីកម្មវិធីរុករកនេះ។")) && revoke.mutate(session.id)} />)}</div>
    {metadata && metadata.totalPages > 1 && <div className="flex items-center justify-between"><span className="text-xs text-slate-500">{label("Page", "ទំព័រ")} {metadata.number + 1} / {metadata.totalPages} · {metadata.totalElements}</span><div className="flex gap-2"><Button variant="outline" size="sm" disabled={metadata.first || query.isFetching} onClick={() => setPageNumber(n => Math.max(0, n - 1))}>{label("Previous", "មុន")}</Button><Button variant="outline" size="sm" disabled={metadata.last || query.isFetching} onClick={() => setPageNumber(n => n + 1)}>{label("Next", "បន្ទាប់")}</Button></div></div>}
  </section>;
}

function AdminSessionsPage({ locale, canRevoke }: { locale: Locale; canRevoke: boolean }) {
  const [userUuid, setUserUuid] = useState("");
  const [selectedUser, setSelectedUser] = useState("");
  const [pageNumber, setPageNumber] = useState(0);
  const queryClient = useQueryClient();
  const label = (en: string, km: string) => locale === "km" ? km : en;
  const query = useQuery({ queryKey: ["admin-user-sessions", selectedUser, pageNumber], enabled: !!selectedUser,
    queryFn: async () => (await apiClient.get<Page<SessionRecord>>(`/admin/users/${selectedUser}/sessions?page=${pageNumber}&size=10`)).data });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["admin-user-sessions", selectedUser] });
  const revoke = useMutation({ mutationFn: async (id: string) => apiClient.delete(`/admin/users/${selectedUser}/sessions/${id}`), onSuccess: invalidate });
  const revokeAll = useMutation({ mutationFn: async () => apiClient.delete(`/admin/users/${selectedUser}/sessions`), onSuccess: invalidate });
  const metadata = query.data?.page;
  return <section className="space-y-5">
    <header><h1 className="text-xl font-semibold">{label("User session administration", "ការគ្រប់គ្រងសម័យអ្នកប្រើ")}</h1><p className="mt-1 text-sm text-slate-500">{label("Enter a user's UUID to inspect or terminate their active sessions.", "បញ្ចូល UUID អ្នកប្រើ ដើម្បីពិនិត្យ ឬបញ្ចប់សម័យសកម្ម។")}</p></header>
    <form className="flex flex-col gap-2 rounded-xl border bg-white p-4 sm:flex-row" onSubmit={event => { event.preventDefault(); setPageNumber(0); setSelectedUser(userUuid.trim()); }}><input aria-label="User UUID" className="h-10 min-w-0 flex-1 rounded-lg border border-[#E5E7EB] px-3 text-sm" value={userUuid} onChange={event => setUserUuid(event.target.value)} placeholder="User UUID" required/><Button type="submit">{label("View sessions", "មើលសម័យ")}</Button>{selectedUser && canRevoke && <Button type="button" variant="outline" className="border-rose-200 text-rose-700 hover:bg-rose-50" disabled={revokeAll.isPending || !query.data?.content.length} onClick={() => window.confirm(label("Revoke every active session for this user?", "បញ្ចប់សម័យសកម្មទាំងអស់របស់អ្នកប្រើនេះឬ?")) && revokeAll.mutate()}>{label("Revoke all", "បញ្ចប់ទាំងអស់")}</Button>}</form>
    {(query.isLoading || (query.isFetching && !!selectedUser)) && <p role="status" className="rounded-xl border bg-white p-5 text-sm text-slate-500">{label("Loading sessions…", "កំពុងផ្ទុកសម័យ…")}</p>}
    {(query.isError || revoke.isError || revokeAll.isError) && <p role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">{label("Unable to load or update sessions. Check your session.read and session.revoke permissions.", "មិនអាចផ្ទុក ឬកែប្រែសម័យបានទេ។ សូមពិនិត្យសិទ្ធិ session.read និង session.revoke។")}</p>}
    {!selectedUser && <p className="rounded-xl border bg-white p-6 text-sm text-slate-500">{label("Select a user to view session records.", "ជ្រើសរើសអ្នកប្រើ ដើម្បីមើលកំណត់ត្រាសម័យ។")}</p>}
    {selectedUser && !query.isLoading && query.data?.content.length === 0 && <p className="rounded-xl border bg-white p-6 text-sm text-slate-500">{label("No active sessions found for this user.", "រកមិនឃើញសម័យសកម្មរបស់អ្នកប្រើនេះទេ។")}</p>}
    <div className="grid gap-3">{query.data?.content.map(session => <SessionCard key={session.id} session={session} locale={locale} busy={revoke.isPending || !canRevoke} onRevoke={() => window.confirm(label("Revoke this user's session?", "បញ្ចប់សម័យរបស់អ្នកប្រើនេះឬ?")) && revoke.mutate(session.id)} />)}</div>
    {metadata && metadata.totalPages > 1 && <div className="flex items-center justify-between"><span className="text-xs text-slate-500">{label("Page", "ទំព័រ")} {metadata.number + 1} / {metadata.totalPages} · {metadata.totalElements}</span><div className="flex gap-2"><Button variant="outline" size="sm" disabled={metadata.first || query.isFetching} onClick={() => setPageNumber(n => Math.max(0, n - 1))}>{label("Previous", "មុន")}</Button><Button variant="outline" size="sm" disabled={metadata.last || query.isFetching} onClick={() => setPageNumber(n => n + 1)}>{label("Next", "បន្ទាប់")}</Button></div></div>}
  </section>;
}

function SessionCard({ session, locale, busy, onRevoke }: { session: SessionRecord; locale: Locale; busy: boolean; onRevoke: () => void }) {
  const label = (en: string, km: string) => locale === "km" ? km : en;
  const date = (value?: string) => value ? new Intl.DateTimeFormat(locale === "km" ? "km-KH" : "en-US", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : label("No expiry", "មិនផុតកំណត់");
  return <article className="rounded-xl border border-[#E5E7EB] bg-white p-4 sm:p-5"><div className="flex flex-wrap items-start justify-between gap-3"><div className="flex min-w-0 items-start gap-3"><span className="grid size-10 shrink-0 place-items-center rounded-lg bg-indigo-50 text-[#534AB7]"><FiMonitor aria-hidden="true"/></span><div className="min-w-0"><div className="flex flex-wrap items-center gap-2"><h2 className="font-semibold">{session.credentialType === "JWT" ? label("IAM access token", "IAM access token") : label("Browser session", "សម័យកម្មវិធីរុករក")}</h2>{session.current && <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700">{label("This session", "សម័យនេះ")}</span>}</div><p className="mt-1 break-all text-xs text-slate-500">{session.userAgent || label("Device details unavailable", "មិនមានព័ត៌មានឧបករណ៍")}</p></div></div><Button variant="outline" size="sm" disabled={busy} onClick={onRevoke}>{label("Revoke", "បញ្ចប់")}</Button></div><dl className="mt-4 grid gap-3 border-t pt-4 text-sm sm:grid-cols-2 lg:grid-cols-3"><SessionField icon={<FiShield/>} label={label("Authentication", "ការផ្ទៀងផ្ទាត់")} value={`${session.authenticationType}${session.provider ? ` · ${session.provider}` : ""}`}/><SessionField icon={<FiMapPin/>} label={label("IP address", "អាសយដ្ឋាន IP")} value={session.ipAddress || "—"}/><SessionField icon={<FiClock/>} label={label("Created", "បានបង្កើត")} value={date(session.createdAt)}/><SessionField icon={<FiClock/>} label={label("Last activity", "សកម្មភាពចុងក្រោយ")} value={date(session.lastActivityAt)}/><SessionField icon={<FiClock/>} label={label("Expires", "ផុតកំណត់")} value={date(session.expiresAt)}/></dl></article>;
}

function SessionField({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return <div><dt className="flex items-center gap-1.5 text-xs text-slate-500">{icon}{label}</dt><dd className="mt-1 break-words font-medium text-slate-800">{value}</dd></div>;
}

type AccountActivityEvent = {
  id: string;
  eventType: string;
  description: string;
  occurredAt: string;
  ipAddress?: string;
  userAgent?: string;
};

function AccountActivityPage({ locale }: { locale: Locale }) {
  const isKm = locale === "km";
  const [page, setPage] = useState(0);
  const query = useQuery({
    queryKey: ["me-activity", page],
    queryFn: async () => (await apiClient.get<Page<AccountActivityEvent>>("/me/activity", { params: { page, size: 20 } })).data,
  });
  const label = (en: string, km: string) => isKm ? km : en;
  const date = (value: string) => new Intl.DateTimeFormat(isKm ? "km-KH" : "en-US", {
    dateStyle: "medium", timeStyle: "short",
  }).format(new Date(value));
  const eventDescription = (event: AccountActivityEvent) => {
    const descriptions: Record<string, [string, string]> = {
      PASSWORD_CHANGED: ["Password changed", "បានប្តូរពាក្យសម្ងាត់"],
      PROFILE_UPDATED: ["Profile information updated", "បានធ្វើបច្ចុប្បន្នភាពព័ត៌មានប្រវត្តិរូប"],
      EMAIL_CHANGED: ["Email address changed", "បានប្តូរអាសយដ្ឋានអ៊ីមែល"],
      SOCIAL_ACCOUNT_LINKED: ["A social account was linked", "បានភ្ជាប់គណនីបណ្តាញសង្គម"],
      SOCIAL_ACCOUNT_UNLINKED: ["A social account was unlinked", "បានផ្តាច់គណនីបណ្តាញសង្គម"],
      SESSION_REVOKED: ["An account session was revoked", "បានបញ្ចប់សម័យគណនីមួយ"],
      CURRENT_SESSION_REVOKED: ["Current session revoked", "បានបញ្ចប់សម័យបច្ចុប្បន្ន"],
      OTHER_SESSIONS_REVOKED: ["Other active sessions revoked", "បានបញ្ចប់សម័យសកម្មផ្សេងទៀត"],
    };
    return descriptions[event.eventType]?.[isKm ? 1 : 0] ?? event.description;
  };
  return <section className="max-w-4xl space-y-5">
    <div><p className="text-xs font-semibold uppercase tracking-[.14em] text-[#534AB7]">{label("ACCOUNT SECURITY", "សុវត្ថិភាពគណនី")}</p><h1 className="mt-1 text-xl font-semibold">{label("Account activity", "សកម្មភាពគណនី")}</h1><p className="mt-1 text-sm text-slate-500">{label("Review security and profile changes made to your account.", "ពិនិត្យមើលការផ្លាស់ប្តូរសុវត្ថិភាព និងប្រវត្តិរូបគណនីរបស់អ្នក។")}</p></div>
    {query.isLoading && <div className="rounded-xl border border-[#E5E7EB] bg-white p-5 text-sm text-slate-500">{label("Loading account activity…", "កំពុងផ្ទុកសកម្មភាពគណនី…")}</div>}
    {query.isError && <div role="alert" className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-800">{label("Unable to load account activity. Check your access and try again.", "មិនអាចផ្ទុកសកម្មភាពគណនីបានទេ។ សូមពិនិត្យសិទ្ធិ និងព្យាយាមម្តងទៀត។")}</div>}
    {query.data?.content.length === 0 && <div className="rounded-xl border border-dashed border-[#CBD5E1] bg-white p-8 text-center"><span className="mx-auto grid size-10 place-items-center rounded-full bg-[#EEECFF] text-[#534AB7]"><FiActivity aria-hidden="true"/></span><h2 className="mt-3 text-sm font-semibold">{label("No account activity yet", "មិនទាន់មានសកម្មភាពគណនីទេ")}</h2><p className="mt-1 text-sm text-slate-500">{label("Security and profile events will appear here when they occur.", "ព្រឹត្តិការណ៍សុវត្ថិភាព និងប្រវត្តិរូបនឹងបង្ហាញនៅទីនេះនៅពេលកើតឡើង។")}</p></div>}
    {!!query.data?.content.length && <div className="relative space-y-3 before:absolute before:bottom-5 before:left-[19px] before:top-5 before:w-px before:bg-slate-200">{query.data.content.map(event => <article key={event.id} className="relative rounded-xl border border-[#E5E7EB] bg-white p-4 pl-12 sm:p-5 sm:pl-14"><span className="absolute left-2.5 top-4 grid size-5 place-items-center rounded-full bg-[#EEECFF] text-[#534AB7] ring-4 ring-white"><FiActivity size={12} aria-hidden="true"/></span><div className="flex flex-wrap items-start justify-between gap-2"><h2 className="text-sm font-semibold text-slate-900">{eventDescription(event)}</h2><time className="text-xs text-slate-500" dateTime={event.occurredAt}>{date(event.occurredAt)}</time></div><div className="mt-3 flex flex-wrap gap-x-5 gap-y-1 text-xs text-slate-500">{event.ipAddress && <span>{label("IP", "អាសយដ្ឋាន IP")}: {event.ipAddress}</span>}{event.userAgent && <span className="max-w-full break-words">{event.userAgent}</span>}</div></article>)}</div>}
    {query.data?.page && query.data.page.totalPages > 1 && <nav aria-label={label("Account activity pages", "ទំព័រសកម្មភាពគណនី")} className="flex items-center justify-between gap-3"><p className="text-xs text-slate-500">{label("Page", "ទំព័រ")} {query.data.page.number + 1} {label("of", "ក្នុងចំណោម")} {query.data.page.totalPages}</p><div className="flex gap-2"><button disabled={query.data.page.first || query.isFetching} onClick={() => setPage(value => Math.max(0, value - 1))} className="h-9 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm text-slate-700 disabled:opacity-50">{label("Previous", "មុន")}</button><button disabled={query.data.page.last || query.isFetching} onClick={() => setPage(value => value + 1)} className="h-9 rounded-lg border border-[#E5E7EB] bg-white px-3 text-sm text-slate-700 disabled:opacity-50">{label("Next", "បន្ទាប់")}</button></div></nav>}
  </section>;
}

function UnavailableFeature({ locale, feature, onLogout, user }: { locale: Locale; feature: "linked" | "apps" | "sessions" | "history" | "activity" | "deactivate" | "clients" | "adminSessions" | "adminHistory" | "auditLogs" | "systemConfig"; onLogout?: () => void; user?: Me }) {
  const labels: Record<Locale, Record<typeof feature, string>> = {
    en: { linked: "Linked accounts", apps: "Connected applications", sessions: "Active sessions", history: "Login history", activity: "Account activity", deactivate: "Account deactivation", clients: "OAuth2 / OIDC clients", adminSessions: "User session administration", adminHistory: "User login history", auditLogs: "Audit logs", systemConfig: "System configuration" },
    km: { linked: "គណនីដែលបានភ្ជាប់", apps: "កម្មវិធីដែលបានភ្ជាប់", sessions: "សម័យសកម្ម", history: "ប្រវត្តិចូល", activity: "សកម្មភាពគណនី", deactivate: "ការបិទគណនី", clients: "កម្មវិធី OAuth2 / OIDC", adminSessions: "ការគ្រប់គ្រងសម័យអ្នកប្រើ", adminHistory: "ប្រវត្តិចូលរបស់អ្នកប្រើ", auditLogs: "កំណត់ហេតុសវនកម្ម", systemConfig: "ការកំណត់ប្រព័ន្ធ" },
  };
  const sessionPage = feature === "sessions";
  const sessionExpiry = sessionPage ? readAccessTokenExpiry() : null;
  const activityPage = feature === "activity";
  const formatAccountDate = (value?: string) => value ? new Intl.DateTimeFormat(locale === "km" ? "km-KH" : "en-US", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value)) : (locale === "km" ? "មិនមានទិន្នន័យ" : "Not available");
  return <section className="max-w-3xl rounded-xl border border-[#E5E7EB] bg-white p-6"><p className="text-xs font-semibold uppercase tracking-[.14em] text-[#534AB7]">Northstar IAM</p><h1 className="mt-2 text-xl font-semibold">{labels[locale][feature]}</h1>{activityPage && <div className="mt-5 rounded-lg border border-[#E5E7EB] p-4"><h2 className="text-sm font-semibold">{locale === "km" ? "ព័ត៌មានពេលវេលាគណនី" : "Account timestamps"}</h2><dl className="mt-3 grid gap-4 sm:grid-cols-2"><div><dt className="text-xs text-slate-500">{locale === "km" ? "បានបង្កើតគណនី" : "Account created"}</dt><dd className="mt-1 text-sm font-medium text-slate-800">{formatAccountDate(user?.createdAt)}</dd></div><div><dt className="text-xs text-slate-500">{locale === "km" ? "កំណត់ត្រាគណនីបានធ្វើបច្ចុប្បន្នភាព" : "Account record last updated"}</dt><dd className="mt-1 text-sm font-medium text-slate-800">{formatAccountDate(user?.updatedAt)}</dd></div></dl><p className="mt-3 text-xs leading-5 text-slate-500">{locale === "km" ? "កាលបរិច្ឆេទទាំងនេះមិនមែនជាកំណត់ហេតុសកម្មភាពទេ។" : "These timestamps are account metadata, not an activity event log."}</p></div>}{sessionPage && <div className="mt-5 rounded-lg border border-[#E5E7EB] p-4"><div className="flex flex-wrap items-start justify-between gap-4"><div><h2 className="text-sm font-semibold">{locale === "km" ? "ការចូលប្រើនៅកម្មវិធីរុករកនេះ" : "Sign-in in this browser"}</h2><p className="mt-1 break-all text-xs leading-5 text-slate-500">{navigator.userAgent}</p>{sessionExpiry ? <p className="mt-2 text-xs text-slate-600">{locale === "km" ? "Token ផុតកំណត់៖ " : "Access token expires: "}{new Intl.DateTimeFormat(locale === "km" ? "km-KH" : "en-US", { dateStyle: "medium", timeStyle: "short" }).format(sessionExpiry)}</p> : <p className="mt-2 text-xs text-slate-500">{locale === "km" ? "មិនមានពេលផុតកំណត់ token ដែលអាចអានបានទេ។" : "No readable token expiry is available."}</p>}</div>{onLogout && <button className="h-9 shrink-0 rounded-lg border border-[#E5E7EB] px-3 text-sm font-medium text-slate-700 hover:border-rose-200 hover:bg-rose-50 hover:text-rose-700" onClick={onLogout} type="button">{locale === "km" ? "ចាកចេញពីកម្មវិធីរុករកនេះ" : "Sign out this browser"}</button>}</div><p className="mt-3 text-xs leading-5 text-slate-500">{locale === "km" ? "ព័ត៌មាននេះមកពី token ដែលរក្សាទុកក្នុងកម្មវិធីរុករក។ វាមិនមែនជាបញ្ជី session ពីម៉ាស៊ីនមេទេ។" : "This reflects the token held by this browser. It is not a server-side session inventory."}</p></div>}<div className="mt-5 rounded-lg border border-amber-200 bg-amber-50 p-4"><p className="text-sm font-medium text-amber-950">{locale === "km" ? "មិនទាន់មាន API ខាងក្រោយសម្រាប់មុខងារនេះទេ។" : "This feature is not available from the current backend API."}</p><p className="mt-1 text-sm leading-6 text-amber-900/80">{sessionPage ? (locale === "km" ? "ការមើល ឬបញ្ចប់ session នៅឧបករណ៍ផ្សេងទៀត ត្រូវការការគាំទ្រពី API។" : "Listing or terminating sessions on other devices requires backend API support.") : activityPage ? (locale === "km" ? "ប្រវត្តិសកម្មភាពសុវត្ថិភាពត្រូវការកំណត់ហេតុព្រឹត្តិការណ៍ពី backend។" : "Security activity history requires backend event records.") : (locale === "km" ? "ទំព័រនេះមិនបង្ហាញទិន្នន័យក្លែងក្លាយ ឬសកម្មភាពដែលមិនអាចប្រើបានទេ។" : "No sample records or nonfunctional actions are shown here. Add the corresponding backend API before enabling this workflow.")}</p></div></section>;
}

function readAccessTokenExpiry(): Date | null {
  try {
    const token = window.sessionStorage.getItem("northstar.iam.accessToken");
    const payload = token?.split(".")[1];
    if (!payload) return null;
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
    const claims = JSON.parse(atob(padded)) as { exp?: unknown };
    return typeof claims.exp === "number" && Number.isFinite(claims.exp) ? new Date(claims.exp * 1000) : null;
  } catch {
    return null;
  }
}

export function AppRouter() { return <BrowserRouter><App/></BrowserRouter>; }
