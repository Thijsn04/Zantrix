/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'Roboto', 'sans-serif'],
      },
      colors: {
        zantrix: {
          blue: '#0369a1', // Sky 700
          background: '#f8fafc', // Slate 50
          text: '#0f172a', // Slate 900
        },
        semantic: {
          critical: '#dc2626', // Red 600
          warning: '#d97706', // Amber 600
          success: '#16a34a', // Green 600
        }
      }
    },
  },
  plugins: [],
}
