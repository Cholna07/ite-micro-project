import { useEffect, useState, type ReactNode } from "react";
import { Link } from "react-router-dom";
import { apiClient } from "@/lib/api-client";
import {
  FiChevronDown,
  FiChevronsLeft,
  FiChevronsRight,
  FiCompass,
  FiGlobe,
  FiActivity,
  FiClock,
  FiHome,
  FiLayers,
  FiLink2,
  FiLogOut,
  FiMenu,
  FiShield,
  FiSettings,
  FiServer,
  FiUser,
  FiUsers,
  FiLock,
  FiX,
} from "react-icons/fi";
import type { IconType } from "react-icons";
import type { DashboardPageConfig } from "@/features/profile/dashboard-page";

interface AppLayoutProps {
  config: DashboardPageConfig;
  title: string;
  children: ReactNode;
  onLogout?: () => void;
  logoUrl?: string;
}

type AdminNavItem = { hash: string; label: string; icon: IconType };

export function AppLayout({ config, title, children, onLogout, logoUrl }: AppLayoutProps) {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const t = (key: string) => config.messages[key] ?? key;
  const displayName = [config.user.givenName, config.user.familyName].filter(Boolean).join(" ") || config.user.username;
  const initial = Array.from(displayName.trim())[0]?.toLocaleUpperCase(config.locale === "km" ? "km-KH" : "en-US") ?? "N";
  const localeLabel = config.locale === "km" ? "ខ្មែរ" : "EN";
  const adminLinks: AdminNavItem[] = [
    ...(config.permissions?.includes("user.read") ? [{ hash: "#/users", label: config.locale === "km" ? "អ្នកប្រើប្រាស់" : "Users", icon: FiUsers }] : []),
    ...(config.permissions?.includes("role.read") ? [{ hash: "#/roles", label: config.locale === "km" ? "តួនាទី" : "Roles", icon: FiShield }] : []),
    ...(config.permissions?.includes("permission.read") ? [{ hash: "#/permissions", label: config.locale === "km" ? "សិទ្ធិ" : "Permissions", icon: FiLock }] : []),
    ...(config.permissions?.some(permission => ["client.create", "client.read", "client.manage"].includes(permission)) ? [{ hash: "#/clients", label: config.locale === "km" ? "កម្មវិធី OAuth" : "OAuth clients", icon: FiLayers }] : []),
    ...(config.permissions?.includes("session.read") ? [{ hash: "#/admin/sessions", label: config.locale === "km" ? "សម័យអ្នកប្រើ" : "User sessions", icon: FiClock }] : []),
    ...(config.permissions?.includes("user.read") ? [{ hash: "#/admin/history", label: config.locale === "km" ? "ប្រវត្តិចូល" : "Login history", icon: FiActivity }] : []),
    ...(config.permissions?.includes("audit.read") ? [{ hash: "#/audit-logs", label: config.locale === "km" ? "កំណត់ហេតុសវនកម្ម" : "Audit logs", icon: FiServer }] : []),
    ...(config.permissions?.includes("system.config.read") ? [{ hash: "#/system-config", label: config.locale === "km" ? "ការកំណត់ប្រព័ន្ធ" : "System configuration", icon: FiSettings }] : []),
  ];

  const switchLanguage = (locale: "en" | "km") => {
    const query = new URLSearchParams(window.location.search);
    query.set("lang", locale);
    window.location.assign(`${window.location.pathname}?${query.toString()}${window.location.hash}`);
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] text-[#111827]">
      {mobileOpen && <button aria-label={t("dashboard.closeMenu")} className="fixed inset-0 z-40 bg-slate-950/25 backdrop-blur-[1px] lg:hidden" onClick={() => setMobileOpen(false)} />}
      <Sidebar
        adminLinks={adminLinks}
        collapsed={collapsed}
        config={config}
        logoUrl={logoUrl}
        mobileOpen={mobileOpen}
        onClose={() => setMobileOpen(false)}
        onToggle={() => setCollapsed((value) => !value)}
        t={t}
      />
      <main className={`min-h-screen min-w-0 transition-[margin] duration-200 ${collapsed ? "lg:ml-[76px]" : "lg:ml-[232px]"}`}>
        <Topbar
          config={config}
          displayName={displayName}
          initial={initial}
          localeLabel={localeLabel}
          onLanguageChange={switchLanguage}
          onLogout={onLogout}
          onMenu={() => setMobileOpen(true)}
          title={title}
          t={t}
        />
        <div className="mx-auto max-w-[1440px] space-y-6 px-4 py-6 sm:px-7 sm:py-8 lg:px-9">{children}</div>
      </main>
    </div>
  );
}

function Sidebar({
  adminLinks,
  collapsed,
  config,
  logoUrl,
  mobileOpen,
  onClose,
  onToggle,
  t,
}: {
  adminLinks: AdminNavItem[];
  collapsed: boolean;
  config: DashboardPageConfig;
  logoUrl?: string;
  mobileOpen: boolean;
  onClose: () => void;
  onToggle: () => void;
  t: (key: string) => string;
}) {
  const isConsole = true;
  const sections = isConsole
    ? [
        { title: config.locale === "km" ? "ទិដ្ឋភាពទូទៅ" : "OVERVIEW", items: adminLinks.length > 0 ? [{ href: "/", label: t("dashboard.overview"), icon: FiHome }] : [] },
        { title: config.locale === "km" ? "គណនី" : "ACCOUNT", items: [
          { href: "/account/profile", label: config.locale === "km" ? "ប្រវត្តិរូប" : "My profile", icon: FiUser },
          { href: "/account/password", label: config.locale === "km" ? "សុវត្ថិភាព និងពាក្យសម្ងាត់" : "Security & Password", icon: FiShield },
          { href: "/account/linked", label: t("dashboard.linkedAccounts"), icon: FiLink2 },
          { href: "/account/sessions", label: config.locale === "km" ? "គ្រប់គ្រងសម័យ" : "Session management", icon: FiLock },
          { href: "/account/activity", label: config.locale === "km" ? "សកម្មភាពគណនី" : "Account activity", icon: FiActivity },
          { href: "/account/history", label: config.locale === "km" ? "ប្រវត្តិចូល" : "Login history", icon: FiCompass },
          { href: "/account/deactivate", label: config.locale === "km" ? "បិទគណនី" : "Deactivate account", icon: FiUser },
        ] },
      ]
    : config.locale === "km"
    ? [
        { title: "ទិដ្ឋភាពទូទៅ", items: [{ href: "/", label: t("dashboard.overview"), icon: FiHome }] },
        { title: "គណនី", items: [
          { href: "/profile/edit", label: t("dashboard.myProfile"), icon: FiUser },
          { href: "/profile/accounts", label: t("dashboard.linkedAccounts"), icon: FiLink2 },
        ] },
        { title: "សុវត្ថិភាព", items: [{ href: "/profile/password", label: t("profile.changePassword"), icon: FiShield }] },
      ]
    : [
        { title: "OVERVIEW", items: [{ href: "/", label: t("dashboard.overview"), icon: FiHome }] },
        { title: "ACCOUNT", items: [
          { href: "/profile/edit", label: t("dashboard.myProfile"), icon: FiUser },
          { href: "/profile/accounts", label: t("dashboard.linkedAccounts"), icon: FiLink2 },
        ] },
        { title: "SECURITY", items: [{ href: "/profile/password", label: t("profile.changePassword"), icon: FiShield }] },
      ];

  return (
    <aside className={`fixed inset-y-0 left-0 z-50 flex flex-col border-r border-[#E5E7EB] bg-white text-[#111827] transition-[width,transform] duration-200 ${collapsed ? "lg:w-[76px]" : "lg:w-[232px]"} ${mobileOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"} w-[270px]`}>
      <div className={`flex h-[68px] shrink-0 items-center border-b border-[#E5E7EB] px-4 ${collapsed ? "lg:justify-center lg:px-0" : "justify-between"}`}>
        {isConsole ? <Link className="flex min-w-0 items-center gap-3" to="/" onClick={onClose}>
          {logoUrl ? <img src={logoUrl} alt="ISTAD IAM" className="size-9 shrink-0 rounded-lg object-contain" referrerPolicy="no-referrer" /> : <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-[#EEECFF] text-[#534AB7]"><FiCompass aria-hidden="true" size={18} /></span>}
          <span className={collapsed ? "lg:hidden" : ""}>
            <span className="block whitespace-nowrap text-sm font-semibold tracking-tight">ISTAD IAM</span>
            <span className="mt-0.5 block text-[10px] font-medium uppercase tracking-[0.14em] text-slate-500">{t("dashboard.identityWorkspace")}</span>
          </span>
        </Link> : <a className="flex min-w-0 items-center gap-3" href="/" onClick={onClose}>
          {logoUrl ? <img src={logoUrl} alt="ISTAD IAM" className="size-9 shrink-0 rounded-lg object-contain" referrerPolicy="no-referrer" /> : <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-[#EEECFF] text-[#534AB7]"><FiCompass aria-hidden="true" size={18} /></span>}
          <span className={collapsed ? "lg:hidden" : ""}><span className="block whitespace-nowrap text-sm font-semibold tracking-tight">ISTAD IAM</span><span className="mt-0.5 block text-[10px] font-medium uppercase tracking-[0.14em] text-slate-500">{t("dashboard.identityWorkspace")}</span></span>
        </a>}
        <button aria-label={t("dashboard.closeMenu")} className="grid size-8 place-items-center rounded-lg text-slate-500 hover:bg-slate-100 hover:text-slate-900 lg:hidden" onClick={onClose} type="button"><FiX aria-hidden="true" size={17} /></button>
      </div>
      <nav aria-label={t("dashboard.menu")} className="flex-1 overflow-y-auto px-3 py-5">
        {sections.filter(section => section.items.length > 0).map((section, sectionIndex) => (
          <section className={sectionIndex ? "mt-7" : ""} key={section.title}>
            <p className={`mb-2 px-3 text-[10px] font-semibold uppercase tracking-[0.14em] text-slate-400 ${collapsed ? "lg:hidden" : ""}`}>{section.title}</p>
            <div className="space-y-1">
              {section.items.map(({ href, label, icon: Icon }) => {
                const prefix = "";
                const active = isConsole ? window.location.pathname === `${prefix}${href}` || (href !== "/" && window.location.pathname.startsWith(`${prefix}${href}/`)) : window.location.pathname === href;
                return <Link aria-current={active ? "page" : undefined} className={`group flex min-h-10 items-center gap-3 rounded-lg px-3 text-sm transition-colors ${active ? "bg-[#EEECFF] font-semibold text-[#534AB7]" : "text-slate-600 hover:bg-slate-50 hover:text-slate-950"} ${collapsed && !mobileOpen ? "lg:justify-center lg:px-0" : ""}`} to={href} key={href} onClick={onClose} title={collapsed && !mobileOpen ? label : undefined}>
                  <Icon aria-hidden="true" className={`shrink-0 ${active ? "text-[#534AB7]" : "text-slate-400 group-hover:text-slate-600"}`} size={18} />
                  <span className={collapsed && !mobileOpen ? "lg:hidden" : ""}>{label}</span>
                </Link>;
              })}
            </div>
          </section>
        ))}
        {adminLinks.length > 0 && (
          <section className="mt-7">
            <p className={`mb-2 px-3 text-[10px] font-semibold uppercase tracking-[0.14em] text-slate-400 ${collapsed ? "lg:hidden" : ""}`}>{config.locale === "km" ? "ការគ្រប់គ្រង" : "ADMINISTRATION"}</p>
            <div className="space-y-1">
              {adminLinks.map(item => <ConsoleNavItem key={item.hash} hash={item.hash} label={item.label} icon={item.icon} collapsed={collapsed} />)}
            </div>
          </section>
        )}
      </nav>
      <div className="border-t border-[#E5E7EB] p-2">
        <button aria-label={collapsed ? t("dashboard.expandSidebar") : t("dashboard.collapseSidebar")} className="hidden min-h-9 w-full items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-50 hover:text-slate-900 lg:flex" onClick={onToggle} type="button">
          {collapsed ? <FiChevronsRight aria-hidden="true" size={17} /> : <><FiChevronsLeft aria-hidden="true" size={17} /><span className="ml-2 text-xs">{t("dashboard.collapseSidebar")}</span></>}
        </button>
      </div>
    </aside>
  );
}

function ConsoleNavItem({ hash, label, icon: Icon, collapsed }: { hash: string; label: string; icon: IconType; collapsed: boolean }) {
  const path = hash.slice(1);
  const prefix = "";
  const active = window.location.pathname === `${prefix}${path}` || (path !== "/" && window.location.pathname.startsWith(`${prefix}${path}/`));
  return <Link aria-current={active ? "page" : undefined} className={`group flex min-h-10 items-center gap-3 rounded-lg px-3 text-sm transition-colors ${active ? "bg-[#EEECFF] font-semibold text-[#534AB7]" : "text-slate-600 hover:bg-slate-50 hover:text-slate-950"} ${collapsed ? "lg:justify-center lg:px-0" : ""}`} to={hash.slice(1)} title={collapsed ? label : undefined}>
    <Icon aria-hidden="true" className={`shrink-0 ${active ? "text-[#534AB7]" : "text-slate-400 group-hover:text-slate-600"}`} size={18} />
    <span className={collapsed ? "lg:hidden" : ""}>{label}</span>
  </Link>;
}

function Topbar({
  config,
  displayName,
  initial,
  localeLabel,
  onLanguageChange,
  onLogout,
  onMenu,
  title,
  t,
}: {
  config: DashboardPageConfig;
  displayName: string;
  initial: string;
  localeLabel: string;
  onLanguageChange: (locale: "en" | "km") => void;
  onLogout?: () => void;
  onMenu: () => void;
  title: string;
  t: (key: string) => string;
}) {
  const [managedPictureUrl, setManagedPictureUrl] = useState("");
  useEffect(() => {
    if (!config.user.pictureUrl.startsWith("profile-picture:")) {
      setManagedPictureUrl("");
      return;
    }
    let cancelled = false;
    let objectUrl = "";
    setManagedPictureUrl("");
    apiClient.get<Blob>("/me/profile-picture", { responseType: "blob" }).then(response => {
      objectUrl = URL.createObjectURL(response.data);
      if (cancelled) URL.revokeObjectURL(objectUrl);
      else setManagedPictureUrl(objectUrl);
    }).catch(() => setManagedPictureUrl(""));
    return () => { cancelled = true; if (objectUrl) URL.revokeObjectURL(objectUrl); };
  }, [config.user.pictureUrl]);
  const avatarUrl = config.user.pictureUrl.startsWith("profile-picture:") ? managedPictureUrl : config.user.pictureUrl;
  return (
    <header className="sticky top-0 z-30 border-b border-[#E5E7EB] bg-white/95 backdrop-blur">
      <div className="flex h-[64px] items-center justify-between gap-3 px-4 sm:px-7 lg:px-9">
        <div className="flex min-w-0 items-center gap-3">
          <button aria-label={t("dashboard.menu")} className="grid size-9 shrink-0 place-items-center rounded-lg border border-[#E5E7EB] bg-white text-slate-600 hover:bg-slate-50 lg:hidden" onClick={onMenu} type="button"><FiMenu aria-hidden="true" size={18} /></button>
          <div className="min-w-0">
        <div className="hidden items-center gap-2 text-xs text-slate-400 sm:flex"><Link className="hover:text-slate-700" to="/">ISTAD IAM</Link><span>/</span><span className="text-slate-500">{title}</span></div>
            <p className="truncate text-sm font-semibold text-slate-900 sm:mt-1">{title}</p>
          </div>
        </div>
        <div className="flex shrink-0 items-center gap-2 sm:gap-4">
          <details className="group relative">
            <summary aria-label={t("common.language")} className="flex h-9 cursor-pointer list-none items-center gap-2 rounded-lg border border-[#E5E7EB] bg-white px-2.5 text-xs font-semibold text-slate-700 hover:bg-slate-50 [&::-webkit-details-marker]:hidden">
              <FiGlobe aria-hidden="true" className="text-[#534AB7]" size={15} /><span>{localeLabel}</span><FiChevronDown aria-hidden="true" className="text-slate-400 transition-transform group-open:rotate-180" size={13} />
            </summary>
            <div className="absolute right-0 top-full z-50 mt-2 w-36 rounded-lg border border-[#E5E7EB] bg-white p-1 shadow-lg shadow-slate-900/10">
              <button aria-current={config.locale === "en" ? "true" : undefined} className={`flex h-9 w-full items-center justify-between rounded-md px-2.5 text-left text-sm hover:bg-slate-50 ${config.locale === "en" ? "font-semibold text-[#534AB7]" : "text-slate-700"}`} onClick={() => onLanguageChange("en")} type="button">English{config.locale === "en" && <span aria-hidden="true">✓</span>}</button>
              <button aria-current={config.locale === "km" ? "true" : undefined} className={`flex h-9 w-full items-center justify-between rounded-md px-2.5 text-left text-sm hover:bg-slate-50 ${config.locale === "km" ? "font-semibold text-[#534AB7]" : "text-slate-700"}`} onClick={() => onLanguageChange("km")} type="button">ខ្មែរ{config.locale === "km" && <span aria-hidden="true">✓</span>}</button>
            </div>
          </details>
          <details className="group relative">
            <summary className="flex h-9 cursor-pointer list-none items-center gap-2 rounded-lg px-1.5 text-sm hover:bg-slate-50 [&::-webkit-details-marker]:hidden">
              {avatarUrl ? <img alt="" className="size-8 rounded-full border border-[#E5E7EB] object-cover" referrerPolicy="no-referrer" src={avatarUrl} /> : <span className="grid size-8 place-items-center rounded-full bg-[#EEECFF] text-xs font-semibold text-[#534AB7]">{initial}</span>}
              <span className="hidden max-w-36 truncate font-medium text-slate-700 sm:block">{displayName}</span>
              <FiChevronDown aria-hidden="true" className="hidden text-slate-400 transition-transform group-open:rotate-180 sm:block" size={14} />
            </summary>
            <div className="absolute right-0 top-full z-50 mt-2 w-56 rounded-lg border border-[#E5E7EB] bg-white p-1 shadow-lg shadow-slate-900/10">
              <div className="border-b border-slate-100 px-3 py-2.5">
                <p className="truncate text-sm font-semibold text-slate-900">{displayName}</p>
                <p className="truncate text-xs text-slate-500">{config.user.email || config.user.username}</p>
              </div>
              <Link className="flex h-9 items-center gap-2 rounded-md px-2.5 text-sm text-slate-700 hover:bg-slate-50" to="/account/profile"><FiUser aria-hidden="true" size={15} />{t("profile.edit")}</Link>
              {onLogout ? (
                <button className="flex h-9 w-full items-center gap-2 rounded-md px-2.5 text-left text-sm text-slate-700 hover:bg-rose-50 hover:text-rose-700" onClick={onLogout} type="button"><FiLogOut aria-hidden="true" size={15} />{t("common.logout")}</button>
              ) : (
                <form action="/logout" method="post">
                  <input name={config.csrfParameter} type="hidden" value={config.csrfToken} />
                  <button className="flex h-9 w-full items-center gap-2 rounded-md px-2.5 text-left text-sm text-slate-700 hover:bg-rose-50 hover:text-rose-700" type="submit"><FiLogOut aria-hidden="true" size={15} />{t("common.logout")}</button>
                </form>
              )}
            </div>
          </details>
        </div>
      </div>
    </header>
  );
}
