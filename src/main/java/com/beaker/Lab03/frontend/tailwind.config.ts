import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        abyss: {
          950: "#02131d",
          900: "#062737",
          800: "#0b4255"
        },
        aurora: {
          400: "#5eead4",
          500: "#2dd4bf"
        }
      },
      boxShadow: {
        glass: "0 24px 80px rgba(3, 15, 28, 0.45)",
        glow: "0 0 0 1px rgba(167, 243, 208, 0.55), 0 0 24px rgba(45, 212, 191, 0.45)"
      },
      backgroundImage: {
        "space-grid":
          "radial-gradient(circle at top, rgba(94, 234, 212, 0.14), transparent 30%), radial-gradient(circle at 20% 20%, rgba(56, 189, 248, 0.16), transparent 25%), linear-gradient(135deg, #02131d 0%, #062737 45%, #0b4255 100%)"
      }
    }
  },
  plugins: []
} satisfies Config;
