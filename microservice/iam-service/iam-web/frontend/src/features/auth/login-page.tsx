import { useRef, useState } from "react";
import { FiArrowRight, FiEye, FiEyeOff, FiGitBranch, FiLock, FiShield } from "react-icons/fi";
import { Button } from "@/components/ui/button";
import { useSignInForm } from "@/features/auth/use-sign-in-form";

type Provider = "google" | "github" | "facebook" | "telegram";

export interface LoginPageConfig {
  csrfToken: string;
  csrfParameter: string;
  error: boolean;
  logout: boolean;
  registered: boolean;
  locale: "en" | "km";
  providers: Record<Provider, boolean>;
  messages: {
    brand: string;
    language: string;
    english: string;
    khmer: string;
    title: string;
    subtitle: string;
    username: string;
    password: string;
    submit: string;
    loginError: string;
    logoutMessage: string;
    registeredMessage: string;
    usernameRequired: string;
    passwordRequired: string;
    showPassword: string;
    hidePassword: string;
    continueWith: string;
    googleLabel: string;
    githubLabel: string;
    facebookLabel: string;
    telegramLabel: string;
    socialUnavailable: string;
    noAccount: string;
    register: string;
    forgotPassword: string;
    footer: string;
  };
}

const providerDetails: Array<{
  id: Provider;
  label: keyof LoginPageConfig["messages"];
  initials: string;
}> = [
  { id: "google", label: "googleLabel", initials: "G" },
  { id: "github", label: "githubLabel", initials: "GH" },
  { id: "facebook", label: "facebookLabel", initials: "f" },
  { id: "telegram", label: "telegramLabel", initials: "↗" },
];

export function LoginPage({ config }: { config: LoginPageConfig }) {
  const formRef = useRef<HTMLFormElement>(null);
  const [showPassword, setShowPassword] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useSignInForm({
    usernameRequired: config.messages.usernameRequired,
    passwordRequired: config.messages.passwordRequired,
  });
  const providers = providerDetails.filter(({ id }) => config.providers[id]);

  return (
    <main className="relative flex min-h-screen items-center justify-center overflow-hidden bg-slate-950 px-4 py-10 text-slate-900 sm:px-6">
      <div aria-hidden="true" className="pointer-events-none absolute inset-0 overflow-hidden">
        <div className="absolute -left-32 -top-40 size-[28rem] rounded-full bg-indigo-500/20 blur-3xl" />
        <div className="absolute -bottom-48 -right-28 size-[34rem] rounded-full bg-cyan-500/10 blur-3xl" />
      </div>

      <section className="relative grid w-full max-w-5xl overflow-hidden rounded-3xl bg-white shadow-2xl shadow-black/30 lg:min-h-[690px] lg:grid-cols-[0.9fr_1.1fr]">
        <aside className="relative hidden flex-col justify-between overflow-hidden bg-gradient-to-br from-indigo-700 via-indigo-800 to-slate-950 p-10 text-white lg:flex xl:p-14">
          <div aria-hidden="true" className="absolute -right-24 top-40 size-72 rounded-full border border-white/10" />
          <div aria-hidden="true" className="absolute -right-12 top-52 size-48 rounded-full border border-white/10" />
          <a href="/login" className="relative flex w-fit items-center gap-3 font-semibold tracking-tight text-white">
            <span className="grid size-11 place-items-center rounded-xl border border-white/15 bg-white/10">
              <FiLock aria-hidden="true" size={22} />
            </span>
            <span>{config.messages.brand}</span>
          </a>

          <div className="relative max-w-sm pb-4">
            <div className="mb-6 grid size-12 place-items-center rounded-2xl border border-white/15 bg-white/10">
              <FiShield aria-hidden="true" size={23} />
            </div>
            <h1 className="text-3xl font-semibold leading-tight tracking-tight xl:text-4xl">
              {config.messages.title}
            </h1>
            <p className="mt-4 text-base leading-7 text-indigo-100/80">{config.messages.subtitle}</p>
          </div>

          <p className="relative text-sm leading-6 text-indigo-100/65">{config.messages.footer}</p>
        </aside>

        <div className="flex flex-col px-6 py-7 sm:px-10 sm:py-10 xl:px-14 xl:py-12">
          <div className="flex items-center justify-between gap-4">
            <a href="/login" className="flex items-center gap-2 font-semibold tracking-tight text-slate-900 lg:hidden">
              <span className="grid size-9 place-items-center rounded-xl bg-indigo-600 text-white">
                <FiLock aria-hidden="true" size={19} />
              </span>
              {config.messages.brand}
            </a>
            <span className="hidden text-sm font-medium text-slate-500 sm:inline">{config.messages.language}</span>
            <nav aria-label={config.messages.language} className="ml-auto flex items-center rounded-full border border-slate-200 bg-slate-50 p-1 text-xs font-semibold">
              <a
                aria-current={config.locale === "en" ? "page" : undefined}
                className={`rounded-full px-3 py-1.5 transition ${config.locale === "en" ? "bg-white text-indigo-700 shadow-sm" : "text-slate-500 hover:text-slate-900"}`}
                href="/login?lang=en"
              >
                {config.messages.english}
              </a>
              <a
                aria-current={config.locale === "km" ? "page" : undefined}
                className={`rounded-full px-3 py-1.5 transition ${config.locale === "km" ? "bg-white text-indigo-700 shadow-sm" : "text-slate-500 hover:text-slate-900"}`}
                href="/login?lang=km"
              >
                {config.messages.khmer}
              </a>
            </nav>
          </div>

          <div className="mx-auto w-full max-w-md flex-1 py-9 sm:py-12 lg:py-16">
            <p className="mb-2 text-sm font-semibold uppercase tracking-[0.16em] text-indigo-600">{config.messages.brand}</p>
            <h2 className="text-3xl font-semibold tracking-tight text-slate-950">{config.messages.title}</h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">{config.messages.subtitle}</p>

            {config.error && (
              <div className="mt-6 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800" role="alert">
                {config.messages.loginError}
              </div>
            )}
            {config.logout && (
              <div className="mt-6 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800" role="status">
                {config.messages.logoutMessage}
              </div>
            )}
            {config.registered && (
              <div className="mt-6 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800" role="status">
                {config.messages.registeredMessage}
              </div>
            )}

            <form
              ref={formRef}
              action="/login"
              method="post"
              noValidate
              onSubmit={handleSubmit(() => formRef.current?.submit())}
              className="mt-7 space-y-5"
            >
              <input type="hidden" name={config.csrfParameter} value={config.csrfToken} />
              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-800" htmlFor="username">{config.messages.username}</label>
                <input
                  {...register("username")}
                  autoComplete="username"
                  autoFocus
                  id="username"
                  aria-invalid={Boolean(errors.username)}
                  aria-describedby={errors.username ? "username-error" : undefined}
                  className="h-12 w-full rounded-xl border border-slate-200 bg-white px-4 text-sm text-slate-950 outline-none transition placeholder:text-slate-400 hover:border-slate-300 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 aria-invalid:border-rose-400 aria-invalid:focus:ring-rose-500/10"
                />
                {errors.username && <p className="text-sm text-rose-700" id="username-error" role="alert">{errors.username.message}</p>}
              </div>

              <div className="space-y-2">
                <label className="text-sm font-medium text-slate-800" htmlFor="password">{config.messages.password}</label>
                <div className="relative">
                  <input
                    {...register("password")}
                    autoComplete="current-password"
                    id="password"
                    type={showPassword ? "text" : "password"}
                    aria-invalid={Boolean(errors.password)}
                    aria-describedby={errors.password ? "password-error" : undefined}
                    className="h-12 w-full rounded-xl border border-slate-200 bg-white px-4 pr-12 text-sm text-slate-950 outline-none transition placeholder:text-slate-400 hover:border-slate-300 focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 aria-invalid:border-rose-400 aria-invalid:focus:ring-rose-500/10"
                  />
                  <button
                    aria-label={showPassword ? config.messages.hidePassword : config.messages.showPassword}
                    aria-pressed={showPassword}
                    className="absolute inset-y-0 right-0 grid w-12 place-items-center rounded-r-xl text-slate-500 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-indigo-500"
                    onClick={() => setShowPassword((visible) => !visible)}
                    type="button"
                  >
                    {showPassword ? <FiEyeOff aria-hidden="true" size={18} /> : <FiEye aria-hidden="true" size={18} />}
                  </button>
                </div>
                {errors.password && <p className="text-sm text-rose-700" id="password-error" role="alert">{errors.password.message}</p>}
              </div>

              <Button className="h-12 w-full rounded-xl bg-indigo-600 text-white hover:bg-indigo-700" type="submit">
                {config.messages.submit}<FiArrowRight aria-hidden="true" size={17} />
              </Button>
            </form>

            {providers.length > 0 ? (
              <section className="mt-7" aria-label={config.messages.continueWith}>
                <div className="relative mb-5 text-center">
                  <span className="relative z-10 bg-white px-3 text-xs font-medium text-slate-400">{config.messages.continueWith}</span>
                  <span aria-hidden="true" className="absolute inset-x-0 top-1/2 border-t border-slate-200" />
                </div>
                <div className="grid gap-2 sm:grid-cols-2">
                  {providers.map(({ id, label, initials }) => (
                    <Button asChild className="h-10 justify-start rounded-xl border-slate-200 px-3 text-slate-700" key={id} variant="outline">
                      <a href={`/oauth2/authorization/${id}`}>
                        <span aria-hidden="true" className="grid size-6 place-items-center rounded-md bg-slate-100 text-xs font-bold">{id === "github" ? <FiGitBranch size={15} /> : initials}</span>
                        <span className="truncate">{config.messages[label]}</span>
                      </a>
                    </Button>
                  ))}
                </div>
              </section>
            ) : (
              <p className="mt-6 text-center text-sm text-slate-500">{config.messages.socialUnavailable}</p>
            )}

            <div className="mt-8 flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-5 text-sm">
              <p className="text-slate-500">{config.messages.noAccount} <a className="font-semibold text-indigo-700 hover:text-indigo-900" href="/register">{config.messages.register}</a></p>
              <span className="text-right text-xs leading-5 text-slate-500">{config.messages.forgotPassword}</span>
            </div>
          </div>

          <p className="text-center text-xs leading-5 text-slate-400 lg:hidden">{config.messages.footer}</p>
        </div>
      </section>
    </main>
  );
}
