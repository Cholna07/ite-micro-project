import axios from "axios";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

export const apiClient = axios.create({
  baseURL: apiBaseUrl || "/api",
  headers: { "Content-Type": "application/json" },
  timeout: 15_000,
  withCredentials: true,
});

apiClient.interceptors.request.use((request) => {
  const csrf = document.cookie.split(";").map((part) => part.trim()).find((part) => part.startsWith("XSRF-TOKEN="))?.slice("XSRF-TOKEN=".length);
  if (csrf) request.headers["X-XSRF-TOKEN"] = decodeURIComponent(csrf);
  return request;
});
