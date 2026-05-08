/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#005ea4',
        'primary-dark': '#1565c0',
        'primary-light': '#e3f2fd',
        surface: '#f8f9fb',
        success: '#4caf50',
        danger: '#ba1a1a',
        warning: '#f57c00',
      },
      fontFamily: {
        sans: ['Source Sans 3', 'Microsoft YaHei', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        'l1': '0 2px 4px rgba(0,0,0,0.1)',
        'l2': '0 4px 12px rgba(0,0,0,0.15)',
        'l3': '0 8px 24px rgba(0,0,0,0.2)',
      },
    },
  },
  plugins: [],
}
