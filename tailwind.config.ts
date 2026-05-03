import type { Config } from 'tailwindcss';

export default {
  content: ['./src/**/*.{html,js,svelte,ts}'],
  theme: {
    extend: {
      colors: {
        ink: '#14213d',
        paper: '#f8fafc',
        metro: '#0055c8'
      }
    }
  },
  plugins: []
} satisfies Config;
