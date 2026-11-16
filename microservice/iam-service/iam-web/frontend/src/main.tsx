import React from "react";
import ReactDOM from "react-dom/client";
import { AppRouter } from "@/app/app";
import { Providers } from "@/app/providers";
import "@/app/globals.css";

const rootElement = document.getElementById("root");

if (!rootElement) {
  throw new Error("Missing React root element");
}

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <Providers>
      <AppRouter />
    </Providers>
  </React.StrictMode>,
);
