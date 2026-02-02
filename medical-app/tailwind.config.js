module.exports = {
  content: ['./src/**/*.{html,ts}'],
  safelist: ['bg-blue-400', 'bg-green-400', 'bg-red-400'],
  theme: {
    extend: {
      colors: {
        'light-blue': {
          100: '#d6eaff',
          200: '#aad4ff',
          300: '#7fbfff',
          400: '#55aaff',
          500: '#2b94ff',
          600: '#0077cc',
          700: '#005b99',
          800: '#003f66',
          900: '#002233',
        },
        'blue-gray': {
          50: '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a',
        },
      },
      fontFamily: {
        sans: ['Nunito', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
