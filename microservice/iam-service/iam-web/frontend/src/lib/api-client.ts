import axios from "axios";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;
const accessTokenKey = "northstar.iam.accessToken";
const permissionListKey = "northstar.iam.permissions";

export const apiClient = axios.create({
  baseURL: apiBaseUrl || "/api",
  headers: { "Content-Type": "application/json" },
  timeout: 15_000,
  withCredentials: true,
});

apiClient.interceptors.request.use((request) => {
  const token = window.sessionStorage.getItem(accessTokenKey);
  if (token) request.headers.Authorization = `Bearer ${token}`;
  const csrf = document.cookie.split(";").map((part) => part.trim()).find((part) => part.startsWith("XSRF-TOKEN="))?.slice("XSRF-TOKEN=".length);
  if (csrf) request.headers["X-XSRF-TOKEN"] = decodeURIComponent(csrf);
  return request;
});

export function storeAccessToken(token: string) {
  window.sessionStorage.setItem(accessTokenKey, token);
}

export function clearAccessToken() {
  window.sessionStorage.removeItem(accessTokenKey);
  window.sessionStorage.removeItem(permissionListKey);
}

export function hasAccessToken() {
  return Boolean(window.sessionStorage.getItem(accessTokenKey));
}

export function storeAccessPermissions(permissions: string[]) {
  window.sessionStorage.setItem(permissionListKey, JSON.stringify(permissions));
}

export function getAccessPermissions(): string[] {
  try {
    const value: unknown = JSON.parse(window.sessionStorage.getItem(permissionListKey) ?? "[]");
    return Array.isArray(value) && value.every((item) => typeof item === "string") ? value : [];
  } catch {
    return [];
  }
}
