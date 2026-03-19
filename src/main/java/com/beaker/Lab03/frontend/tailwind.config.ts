import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        macaron: {
          pink: "#f8c8dc",
          peach: "#ffd8be",
          butter: "#fff1a8",
          mint: "#c9f2d0",
          sky: "#cde7ff",
          lavender: "#ddd6fe"
        },
        apricot: {
          300: "#f8cfb8",
          400: "#f3b18d",
          500: "#ea936d"
        },
        honey: {
          300: "#f9e7a7",
          400: "#f2d870",
          500: "#e7bf4f"
        }
      },
      boxShadow: {
        glass: "0 24px 80px rgba(200, 152, 170, 0.22)",
        glow: "0 0 0 1px rgba(255, 188, 214, 0.9), 0 0 28px rgba(255, 206, 132, 0.45)"
      },
      backgroundImage: {
        "space-grid":
          "radial-gradient(circle at top, rgba(248, 200, 220, 0.7), transparent 26%), radial-gradient(circle at 18% 22%, rgba(201, 242, 208, 0.55), transparent 24%), radial-gradient(circle at 82% 18%, rgba(205, 231, 255, 0.5), transparent 22%), linear-gradient(135deg, #fff7fb 0%, #fff0dc 34%, #eefae9 66%, #eef5ff 100%)"
      }
    }
  },
  plugins: []
} satisfies Config;
