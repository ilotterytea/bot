import { nextui } from '@nextui-org/react'
import type { Config } from 'tailwindcss'

const config: Config = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
    "./node_modules/@nextui-org/theme/dist/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        "anta": ["Anta", "sans-serif"],
        "inter": ["Inter", "sans-serif"],
        "manrope": ["Manrope", "sans-serif"]
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-conic':
          'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))',
      },
      animation: {
        'mainscreen': 'mainscreen 10s ease infinite'
      },
      keyframes: {
        'mainscreen': {
          '0%, 100%': {
            'background-size':'200% 200%',
             'background-position': 'top center'
          },
          '50%': {
              'background-size':'200% 200%',
              'background-position': 'bottom center'
          }
        }
      }
    },
  },
  darkMode: "media",
  plugins: [nextui()],
}
export default config
