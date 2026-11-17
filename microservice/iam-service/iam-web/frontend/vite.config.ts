import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import path from "node:path";
import { fileURLToPath } from "node:url";

const currentDir = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  base: "/frontend/",
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      "/api": "http://localhost:9191",
      "/connect": "http://localhost:9191",
      "/oauth2": "http://localhost:9191",
      "/login/oauth2": "http://localhost:9191",
      "/login": {
        target: "http://localhost:9191",
        bypass: request => request.headers["x-requested-with"] === "XMLHttpRequest" ? undefined : "/index.html",
      },
      "/logout": "http://localhost:9191",
    },
  },
  resolve: {
    alias: {
      "@": path.resolve(currentDir, "src"),
    },
  },
  build: {
    outDir: "../src/main/resources/static/frontend",
    emptyOutDir: true,
    rolldownOptions: {
      output: {
        entryFileNames: "assets/iam-frontend.js",
        chunkFileNames: "assets/chunks/[name].js",
        assetFileNames: "assets/[name][extname]",
      },
    },
  },
});
