import type { IconType } from "react-icons";
import {
  FiAlertCircle,
  FiArrowUpRight,
  FiCheck,
  FiCheckCircle,
  FiChevronDown,
  FiCompass,
  FiGrid,
  FiKey,
  FiLink2,
  FiMail,
  FiPhone,
  FiShield,
  FiUser,
} from "react-icons/fi";
import { Button } from "@/components/ui/button";
import { AppLayout } from "@/components/layouts/app-layout";

type IconComponent = IconType;

interface DashboardUser {
  uuid: string;
  username: string;
  email: string;
  pictureUrl: string;
  givenName: string;
  familyName: string;
  middleName: string;
  nickname: string;
  gender: string;
  phoneNumber: string;
  emailVerified: boolean;
  phoneNumberVerified: boolean;
  enabled: boolean;
  accountLocked: boolean;
  createdAt: string;
}

interface DashboardClaim {
  key: string;
  value: string;
}

export interface DashboardPageConfig {
  locale: "en" | "km";
  csrfToken: string;
  csrfParameter: string;
  user: DashboardUser;
  roles: string[];
  permissions: string[];
  claims: DashboardClaim[];
  messages: Record<string, string>;
}

function formatDate(value: string, locale: DashboardPageConfig["locale"], fallback: string) {
  if (!value) return fallback;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(locale === "km" ? "km-KH" : "en-US", {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date);
}

function StatusPill({ label, good }: { label: string; good: boolean }) {
  const Icon = good ? FiCheck : FiAlertCircle;
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium ${good ? "bg-emerald-50 text-emerald-700" : "bg-amber-50 text-amber-800"}`}>
      <Icon aria-hidden="true" size={13} />{label}
    </span>
  );
}

export function DashboardPage({ config }: { config: DashboardPageConfig }) {
  const t = (key: string) => config.messages[key] ?? key;
  const displayName = [config.user.givenName, config.user.familyName].filter(Boolean).join(" ") || config.user.username;
  const avatarInitial = Array.from(displayName.trim())[0]?.toLocaleUpperCase(config.locale === "km" ? "km-KH" : "en-US") ?? "N";
  const accountStatus = config.user.accountLocked
    ? t("dashboard.locked")
    : config.user.enabled
      ? t("dashboard.enabled")
      : t("dashboard.disabled");
  const accountStatusGood = config.user.enabled && !config.user.accountLocked;
  const formattedCreatedAt = formatDate(config.user.createdAt, config.locale, t("dashboard.noValue"));
  return (
    <AppLayout config={config} title={t("dashboard.overview")}>
          <section className="overflow-hidden rounded-xl border border-[#E5E7EB] bg-white shadow-sm shadow-slate-900/[0.025]">
            <div className="relative flex flex-col justify-between gap-6 p-5 sm:p-7 lg:flex-row lg:items-center lg:px-9 lg:py-8">
              <div className="flex min-w-0 items-center gap-4 sm:gap-5">
                {config.user.pictureUrl ? <img alt={displayName} className="size-14 shrink-0 rounded-xl border border-[#E5E7EB] object-cover sm:size-16" referrerPolicy="no-referrer" src={config.user.pictureUrl} /> : <span className="grid size-14 shrink-0 place-items-center rounded-xl border border-[#E5E7EB] bg-[#EEECFF] text-lg font-semibold text-[#534AB7] sm:size-16 sm:text-xl">{avatarInitial}</span>}
                <div className="min-w-0">
                  <p className="text-xs font-semibold uppercase tracking-[0.14em] text-indigo-700">{t("dashboard.welcomeBack")}</p>
                  <h1 className="mt-1 truncate text-xl font-semibold tracking-tight text-slate-950 sm:text-2xl">{displayName}</h1>
                  <p className="mt-1 max-w-2xl text-sm leading-6 text-slate-500">{t("dashboard.profileSummary")}</p>
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-2 lg:justify-end">
                <StatusPill good={accountStatusGood} label={accountStatus} />
                <StatusPill good={config.user.emailVerified} label={`${t("dashboard.emailVerification")}: ${config.user.emailVerified ? t("dashboard.verified") : t("dashboard.notVerified")}`} />
              </div>
            </div>
            <div className="relative grid border-t border-slate-100 bg-slate-50/70 sm:grid-cols-3">
              <div className="flex items-center gap-3 border-b border-slate-100 px-5 py-3.5 sm:border-b-0 sm:px-8">
                <span className="grid size-8 place-items-center rounded-lg bg-violet-50 text-violet-700"><FiUser aria-hidden="true" size={16} /></span>
                <span><span className="block text-[11px] text-slate-500">{t("dashboard.roleAssignments")}</span><span className="text-sm font-semibold text-slate-900">{config.roles.length} <span className="font-normal text-slate-500">{t("dashboard.roleCount")}</span></span></span>
              </div>
              <div className="flex items-center gap-3 border-b border-slate-100 px-5 py-3.5 sm:border-b-0 sm:border-l sm:px-8">
                <span className="grid size-8 place-items-center rounded-lg bg-[#EEECFF] text-[#534AB7]"><FiKey aria-hidden="true" size={16} /></span>
                <span><span className="block text-[11px] text-slate-500">{t("dashboard.permissionAssignments")}</span><span className="text-sm font-semibold text-slate-900">{config.permissions.length} <span className="font-normal text-slate-500">{t("dashboard.permissionCount")}</span></span></span>
              </div>
              <div className="flex items-center gap-3 px-5 py-3.5 sm:border-l sm:border-slate-100 sm:px-8">
                <span className="grid size-8 place-items-center rounded-lg bg-emerald-50 text-emerald-700"><FiCheckCircle aria-hidden="true" size={16} /></span>
                <span><span className="block text-[11px] text-slate-500">{t("dashboard.memberSince")}</span><span className="text-sm font-semibold text-slate-900">{formattedCreatedAt}</span></span>
              </div>
            </div>
          </section>

          <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1.55fr)_minmax(310px,0.85fr)]">
            <div className="space-y-6">
              <section className="overflow-hidden rounded-xl border border-[#E5E7EB] bg-white shadow-sm shadow-slate-900/[0.025]">
                <div className="flex items-center justify-between gap-3 border-b border-slate-100 px-5 py-4 sm:px-6">
                  <div><p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-indigo-700">{t("dashboard.accountOverview")}</p><h2 className="mt-1 text-base font-semibold text-slate-900">{t("dashboard.profileDetails")}</h2></div>
                  <Button asChild className="h-9 rounded-lg border-slate-200 px-3 text-xs" variant="outline"><a href="/profile/edit">{t("profile.edit")}<FiArrowUpRight aria-hidden="true" size={14} /></a></Button>
                </div>
                <div className="grid gap-x-8 sm:grid-cols-2 sm:px-6 sm:py-2">
                  <ProfileField label={t("register.username")} value={config.user.username} primary />
                  <ProfileField label={t("register.email")} value={config.user.email || t("profile.emailUnavailable")} icon={FiMail} />
                  <ProfileField label={t("profile.givenName")} value={config.user.givenName || t("dashboard.noValue")} />
                  <ProfileField label={t("profile.middleName")} value={config.user.middleName || t("dashboard.noValue")} />
                  <ProfileField label={t("profile.familyName")} value={config.user.familyName || t("dashboard.noValue")} />
                  <ProfileField label={t("profile.nickname")} value={config.user.nickname || t("dashboard.noValue")} />
                  <ProfileField label={t("profile.gender")} value={config.user.gender || t("dashboard.noValue")} />
                  <ProfileField label={t("profile.phone")} value={config.user.phoneNumber || t("dashboard.noValue")} icon={FiPhone} />
                </div>
              </section>

              <section className="grid gap-4 md:grid-cols-2">
                <AccessPanel
                  accent="violet"
                  emptyMessage={t("dashboard.noRoles")}
                  icon={FiUser}
                  items={config.roles}
                  label={t("dashboard.roleAssignments")}
                />
                <AccessPanel
                  accent="blue"
                  emptyMessage={t("dashboard.noPermissions")}
                  icon={FiKey}
                  items={config.permissions}
                  label={t("dashboard.permissionAssignments")}
                />
              </section>

              <section className="rounded-xl border border-[#E5E7EB] bg-white p-5 shadow-sm shadow-slate-900/[0.025] sm:p-6">
                <div className="flex items-start gap-3">
                  <span className="grid size-9 shrink-0 place-items-center rounded-lg bg-[#EEECFF] text-[#534AB7]"><FiCompass aria-hidden="true" size={17} /></span>
                  <div>
                    <h2 className="text-sm font-semibold text-slate-900">{t("dashboard.claims")}</h2>
                    <p className="mt-1 text-xs leading-5 text-slate-500">{t("dashboard.claimsDescription")}</p>
                  </div>
                </div>
                <div className="mt-5 grid gap-3 sm:grid-cols-2">
                  <ClaimSummary label={t("register.username")} value={config.user.username} />
                  <ClaimSummary label={t("register.email")} value={config.user.email || t("dashboard.noValue")} />
                </div>
                <details className="group mt-5 border-t border-slate-100 pt-4">
                  <summary className="flex cursor-pointer list-none items-center justify-between gap-3 text-xs font-medium text-slate-600 hover:text-indigo-700 [&::-webkit-details-marker]:hidden">
                    {t("dashboard.technicalDetails")}
                    <FiChevronDown aria-hidden="true" className="transition-transform group-open:rotate-180" size={15} />
                  </summary>
                  <dl className="mt-3 divide-y divide-slate-100 rounded-lg border border-slate-100 bg-slate-50 px-3">
                    {config.claims.map(({ key, value }) => (
                      <div className="grid gap-1 py-2.5 text-xs sm:grid-cols-[minmax(130px,0.7fr)_minmax(0,1.3fr)]" key={key}>
                        <dt className="font-mono text-slate-500">{key}</dt>
                        <dd className="break-all text-slate-800">{value || "—"}</dd>
                      </div>
                    ))}
                  </dl>
                </details>
              </section>
            </div>

            <aside className="space-y-5">
              <section className="overflow-hidden rounded-xl border border-[#E5E7EB] bg-white text-slate-900 shadow-sm shadow-slate-900/[0.025]">
                <div className="flex items-center gap-3 border-b border-slate-100 px-5 py-4">
                  <span className="grid size-9 place-items-center rounded-lg bg-[#EEECFF] text-[#534AB7]"><FiShield aria-hidden="true" size={18} /></span>
                  <div><h2 className="text-sm font-semibold">{t("dashboard.securityOverview")}</h2><p className="mt-0.5 text-[11px] text-slate-500">{t("dashboard.security")}</p></div>
                </div>
                <div className="divide-y divide-slate-100 px-5">
                  <SecurityRow label={t("dashboard.accountStatus")} value={accountStatus} good={accountStatusGood} />
                  <SecurityRow label={t("dashboard.emailVerification")} value={config.user.emailVerified ? t("dashboard.verified") : t("dashboard.notVerified")} good={config.user.emailVerified} />
                  <SecurityRow label={t("dashboard.phoneVerification")} value={config.user.phoneNumberVerified ? t("dashboard.verified") : t("dashboard.notVerified")} good={config.user.phoneNumberVerified} />
                </div>
                <div className="border-t border-slate-100 bg-slate-50 px-5 py-3 text-xs leading-5 text-slate-500">{t("dashboard.securityNote")}</div>
              </section>

              <section className="rounded-xl border border-[#E5E7EB] bg-white p-5 shadow-sm shadow-slate-900/[0.025]">
                <h2 className="text-sm font-semibold text-slate-900">{t("dashboard.quickActions")}</h2>
                <div className="mt-3 space-y-2">
                  <ActionLink href="/profile/edit" icon={FiUser} label={t("profile.edit")} />
                  <ActionLink href="/profile/password" icon={FiKey} label={t("profile.changePassword")} />
                  <ActionLink href="/profile/accounts" icon={FiLink2} label={t("profile.linkedAccounts")} />
                  <ActionLink href="/profile/apps" icon={FiGrid} label={t("profile.connectedApps")} />
                </div>
              </section>

              <section className="rounded-xl border border-[#E5E7EB] bg-white p-5">
                <div className="flex gap-3">
                  <span className="grid size-8 shrink-0 place-items-center rounded-lg bg-white text-[#534AB7]"><FiAlertCircle aria-hidden="true" size={16} /></span>
                  <div><h2 className="text-xs font-semibold text-slate-800">{t("dashboard.activityNote")}</h2><p className="mt-1 text-xs leading-5 text-slate-600">{t("dashboard.securityNote")}</p></div>
                </div>
              </section>
            </aside>
          </div>

          <footer className="flex flex-col gap-1 border-t border-slate-200/80 pt-4 text-[11px] text-slate-400 sm:flex-row sm:items-center sm:justify-between">
            <span>ISTAD IAM</span>
            <span>{t("dashboard.primaryIdentity")} · {config.user.username}</span>
          </footer>
        </AppLayout>
  );
}

function ProfileField({ label, value, icon: Icon, primary = false }: { label: string; value: string; icon?: IconComponent; primary?: boolean }) {
  return (
    <div className="flex min-w-0 items-start gap-3 border-b border-slate-100 px-5 py-3.5 sm:px-0">
      {Icon && <span className="mt-0.5 text-slate-400"><Icon aria-hidden="true" size={15} /></span>}
      <div className="min-w-0 flex-1">
        <dt className="text-[11px] text-slate-500">{label}</dt>
        <dd className={`mt-1 truncate text-sm ${primary ? "font-semibold text-slate-900" : "text-slate-700"}`} title={value}>{value}</dd>
      </div>
    </div>
  );
}

function AccessPanel({
  accent,
  emptyMessage,
  icon: Icon,
  items,
  label,
}: {
  accent: "violet" | "blue";
  emptyMessage: string;
  icon: IconComponent;
  items: string[];
  label: string;
}) {
  const color = accent === "violet"
    ? { icon: "bg-violet-100 text-violet-700", badge: "border-violet-100 bg-violet-50 text-violet-800", count: "text-violet-700" }
    : { icon: "bg-indigo-100 text-indigo-700", badge: "border-indigo-100 bg-indigo-50 text-indigo-800", count: "text-indigo-700" };
  return (
    <section className="rounded-xl border border-[#E5E7EB] bg-white p-5 shadow-sm shadow-slate-900/[0.025]">
      <div className="flex items-center gap-3">
        <span className={`grid size-9 place-items-center rounded-lg ${color.icon}`}><Icon aria-hidden="true" size={17} /></span>
        <div className="min-w-0 flex-1"><h2 className="truncate text-sm font-semibold text-slate-900">{label}</h2><p className={`mt-0.5 text-xs ${color.count}`}>{items.length}</p></div>
      </div>
      {items.length ? (
        <ul className="mt-4 flex flex-wrap gap-2">
          {items.map((item) => <li className={`max-w-full truncate rounded-md border px-2.5 py-1.5 text-xs font-medium ${color.badge}`} key={item} title={item}>{item}</li>)}
        </ul>
      ) : <p className="mt-4 rounded-lg bg-slate-50 px-3 py-2.5 text-xs leading-5 text-slate-500">{emptyMessage}</p>}
    </section>
  );
}

function SecurityRow({ label, value, good }: { label: string; value: string; good: boolean }) {
  return (
    <div className="flex items-center justify-between gap-3 py-3.5">
      <span className="text-xs text-slate-600">{label}</span>
      <span className={`inline-flex items-center gap-1.5 text-xs font-medium ${good ? "text-emerald-700" : "text-amber-700"}`}>
        {good ? <FiCheck aria-hidden="true" size={13} /> : <FiAlertCircle aria-hidden="true" size={13} />}{value}
      </span>
    </div>
  );
}

function ActionLink({ href, icon: Icon, label }: { href: string; icon: IconComponent; label: string }) {
  return (
    <Button asChild className="h-10 w-full justify-start rounded-lg border-slate-200 px-3 text-slate-700 hover:border-indigo-200 hover:bg-indigo-50/70 hover:text-indigo-800" variant="outline">
      <a href={href}><Icon aria-hidden="true" className="text-slate-400" size={16} /><span className="flex-1 text-left">{label}</span><FiArrowUpRight aria-hidden="true" className="text-slate-400" size={14} /></a>
    </Button>
  );
}

function ClaimSummary({ label, value }: { label: string; value: string }) {
  return <div className="min-w-0 rounded-lg border border-slate-100 bg-slate-50 px-3 py-2.5"><p className="text-[10px] font-semibold uppercase tracking-[0.1em] text-slate-500">{label}</p><p className="mt-1 truncate text-xs font-medium text-slate-800" title={value}>{value}</p></div>;
}
